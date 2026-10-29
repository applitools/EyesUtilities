package com.applitools.eyesutilities.gui;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/** Boots the real server on a random port. Needs the GUI bundle on the classpath (mvn package). */
public class GuiServerIT {

    private GuiServer server;

    @Before
    public void setUp() throws Exception {
        server = GuiServer.start(new RunController(new RunStream()));
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void shouldServeStatusWithToken() throws Exception {
        assertEquals(200, request("GET", "/api/status", "Bearer " + server.token().value(), null).getResponseCode());
    }

    @Test
    public void shouldRejectApiCallWithoutToken() throws Exception {
        assertEquals(401, request("GET", "/api/status", null, null).getResponseCode());
    }

    @Test
    public void shouldRejectUnknownCommandWith400() throws Exception {
        String body = "{\"command\":\"rm\",\"values\":{},\"key\":\"k\"}";
        assertEquals(400, request("POST", "/api/run", "Bearer " + server.token().value(), body).getResponseCode());
    }

    @Test
    public void shouldRejectMalformedJsonWith400() throws Exception {
        assertEquals(400, request("POST", "/api/run", "Bearer " + server.token().value(), "{nope").getResponseCode());
    }

    @Test
    public void shouldInjectTheTokenIntoThePage() throws Exception {
        HttpURLConnection c = request("GET", "/", null, null);
        String html = new String(IOUtils.toByteArray(c.getInputStream()), StandardCharsets.UTF_8);
        assertTrue(html.contains(server.token().value()));
    }

    @Test
    public void shouldDenyFraming() throws Exception {
        assertEquals("DENY", request("GET", "/", null, null).getHeaderField("X-Frame-Options"));
    }

    private HttpURLConnection request(String method, String path, String auth, String body) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL("http://127.0.0.1:" + server.port() + path).openConnection();
        c.setRequestMethod(method);
        if (auth != null) c.setRequestProperty("Authorization", auth);
        if (body != null) {
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/json");
            try (OutputStream out = c.getOutputStream()) { out.write(body.getBytes(StandardCharsets.UTF_8)); }
        }
        return c;
    }
}
