package com.applitools.eyesutilities.gui;

import com.applitools.eyesutilities.EyesUtilities;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Local web GUI for EyesUtilities: binds 127.0.0.1 on a random free port and serves the bundled
 * React page plus a small token-gated JSON/SSE API. Uses the JDK's built-in HTTP server so the
 * library jar (which ImageTester also consumes) gains no web-server dependencies.
 */
public final class GuiServer {

    private static final int ANY_FREE_PORT = 0;
    private static final int DEFAULT_BACKLOG = 0;
    private static final int STOP_DELAY_SECONDS = 0;
    private static final int MAX_BODY_BYTES = 64 * 1024;
    private static final int NO_BODY = -1;
    private static final int STREAMING_BODY = 0;
    private static final String WEB_ROOT = "/web";
    private static final String API_PREFIX = "/api/";
    private static final String ASSETS_PREFIX = "/assets/";
    private static final String TOKEN_PLACEHOLDER = "__GUI_TOKEN__";
    private static final String VERSION_PLACEHOLDER = "__GUI_VERSION__";
    private static final String JSON = "application/json";

    private final HttpServer server_;
    private final ExecutorService executor_;
    private final RunController controller_;
    private final GuiToken token_;
    private final RequestGuard guard_;
    private final byte[] indexHtml_;
    private final ObjectMapper json_ = new ObjectMapper();

    private GuiServer(HttpServer server, ExecutorService executor, RunController controller, GuiToken token) {
        this.server_ = server;
        this.executor_ = executor;
        this.controller_ = controller;
        this.token_ = token;
        this.guard_ = new RequestGuard(token, server.getAddress().getPort());
        this.indexHtml_ = loadResource(WEB_ROOT + "/index.html");
    }

    /** Entry point for `--gui`: serves until the process is stopped (Ctrl+C / window close). */
    public static void launch() throws IOException {
        GuiServer gui = start(new RunController(new RunStream()));
        String url = "http://localhost:" + gui.port();
        System.out.println("EyesUtilities GUI running at " + url + " — press Ctrl+C to stop.");
        GuiLauncher.open(url);
    }

    static GuiServer start(RunController controller) throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress(InetAddress.getLoopbackAddress(), ANY_FREE_PORT), DEFAULT_BACKLOG);
        // Each open SSE stream parks a request thread, so the default single-thread executor would deadlock.
        ExecutorService executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "GuiServer-http");
            t.setDaemon(true);
            return t;
        });
        server.setExecutor(executor);
        GuiServer gui = new GuiServer(server, executor, controller, GuiToken.generate());
        server.createContext("/", gui::handle);
        server.start();
        return gui;
    }

    int port() { return server_.getAddress().getPort(); }
    GuiToken token() { return token_; }

    void stop() {
        controller_.stream().close();
        server_.stop(STOP_DELAY_SECONDS);
        executor_.shutdownNow();
    }

    private void handle(HttpExchange exchange) throws IOException {
        setSecurityHeaders(exchange);
        String path = exchange.getRequestURI().getPath();
        try {
            if (path.startsWith(API_PREFIX)) handleApi(exchange, path);
            else if (path.equals("/") || path.equals("/index.html")) serveIndex(exchange);
            else if (path.startsWith(ASSETS_PREFIX)) serveAsset(exchange, path);
            else sendStatus(exchange, 404);
        } catch (InvalidRequestException e) {
            sendJson(exchange, 400, error(e.getMessage()));
        } catch (RunController.RunInProgressException e) {
            sendJson(exchange, 409, error(e.getMessage()));
        } catch (JsonProcessingException e) {
            sendJson(exchange, 400, error("Malformed request body."));
        } catch (RuntimeException e) {
            // Never echo internals (stack traces, paths) to the page.
            sendJson(exchange, 500, error("Unexpected server error."));
        }
    }

    private void handleApi(HttpExchange exchange, String path) throws IOException {
        int verdict = guard_.check(
                exchange.getRequestHeaders().getFirst("Host"),
                exchange.getRequestHeaders().getFirst("Origin"),
                exchange.getRequestHeaders().getFirst("Authorization"),
                queryParam(exchange.getRequestURI(), "token"));
        if (verdict != RequestGuard.ALLOWED) { sendStatus(exchange, verdict); return; }

        String method = exchange.getRequestMethod();
        if (path.equals("/api/status") && method.equals("GET")) {
            sendJson(exchange, 200, controller_.snapshot().toJson());
        } else if (path.equals("/api/run") && method.equals("POST")) {
            handleRun(exchange);
        } else if (path.equals("/api/cancel") && method.equals("POST")) {
            controller_.cancel();
            sendStatus(exchange, 204);
        } else if (path.equals("/api/events") && method.equals("GET")) {
            openEventStream(exchange);
        } else {
            sendStatus(exchange, 404);
        }
    }

    private void handleRun(HttpExchange exchange) throws IOException {
        byte[] body = readBody(exchange);
        if (body == null) { sendJson(exchange, 413, error("Request body too large.")); return; }
        UtilityRequest req = json_.readValue(body, UtilityRequest.class);
        sendJson(exchange, 200, Collections.singletonMap("runId", controller_.start(req)));
    }

    private void openEventStream(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(200, STREAMING_BODY);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(exchange.getResponseBody(), StandardCharsets.UTF_8));
        controller_.stream().addClient(writer, exchange::close);
    }

    private void serveIndex(HttpExchange exchange) throws IOException {
        String html = new String(indexHtml_, StandardCharsets.UTF_8)
                .replace(TOKEN_PLACEHOLDER, token_.value())
                .replace(VERSION_PLACEHOLDER, EyesUtilities.version());
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        sendBytes(exchange, 200, "text/html; charset=utf-8", html.getBytes(StandardCharsets.UTF_8));
    }

    private void serveAsset(HttpExchange exchange, String path) throws IOException {
        if (path.contains("..")) { sendStatus(exchange, 404); return; }
        InputStream in = GuiServer.class.getResourceAsStream(WEB_ROOT + path);
        if (in == null) { sendStatus(exchange, 404); return; }
        byte[] bytes;
        try { bytes = IOUtils.toByteArray(in); } finally { in.close(); }
        // Vite fingerprints asset file names, so they can be cached forever.
        exchange.getResponseHeaders().set("Cache-Control", "public, max-age=31536000, immutable");
        sendBytes(exchange, 200, contentType(path), bytes);
    }

    private static String contentType(String path) {
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    /** Browsers ignore HSTS over plain HTTP; it's here for scanners. nosniff and frame-deny are real hardening. */
    private static void setSecurityHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Strict-Transport-Security", "max-age=31536000");
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("X-Frame-Options", "DENY");
    }

    /** @return the body, or null when it exceeds {@link #MAX_BODY_BYTES}. */
    private static byte[] readBody(HttpExchange exchange) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        try (InputStream in = exchange.getRequestBody()) {
            int n;
            while ((n = in.read(buffer)) != -1) {
                if (out.size() + n > MAX_BODY_BYTES) return null;
                out.write(buffer, 0, n);
            }
        }
        return out.toByteArray();
    }

    private static String queryParam(URI uri, String name) {
        String query = uri.getRawQuery();
        if (query == null) return null;
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals(name)) {
                try {
                    return URLDecoder.decode(pair.substring(eq + 1), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return null;
    }

    private static Map<String, Object> error(String message) {
        Map<String, Object> m = new HashMap<>();
        m.put("error", message);
        return m;
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        sendBytes(exchange, status, JSON, json_.writeValueAsBytes(body));
    }

    private static void sendBytes(HttpExchange exchange, int status, String contentType, byte[] bytes) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) { out.write(bytes); }
    }

    private static void sendStatus(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, NO_BODY);
        exchange.close();
    }

    private static byte[] loadResource(String path) {
        try (InputStream in = GuiServer.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException(path + " is not on the classpath — build the GUI with `mvn package`.");
            }
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + path + ": " + e.getMessage(), e);
        }
    }
}
