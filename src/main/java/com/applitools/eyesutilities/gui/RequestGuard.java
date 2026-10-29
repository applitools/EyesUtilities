package com.applitools.eyesutilities.gui;

import java.util.Arrays;
import java.util.List;

/**
 * Gate for every /api request: a localhost Host (defeats DNS rebinding), a matching Origin
 * when one is sent (defeats other web pages), and the session token (defeats other local
 * processes). Never relax this for dev convenience.
 */
final class RequestGuard {

    static final int ALLOWED = 0;
    static final int FORBIDDEN = 403;
    static final int UNAUTHORIZED = 401;

    private static final String BEARER = "Bearer ";

    private final GuiToken token_;
    private final List<String> allowedHosts_;
    private final List<String> allowedOrigins_;

    RequestGuard(GuiToken token, int port) {
        this.token_ = token;
        this.allowedHosts_ = Arrays.asList("localhost:" + port, "127.0.0.1:" + port);
        this.allowedOrigins_ = Arrays.asList("http://localhost:" + port, "http://127.0.0.1:" + port);
    }

    /** @return {@link #ALLOWED}, or the HTTP status to reject with. */
    int check(String host, String origin, String authorization, String tokenParam) {
        if (host == null || !allowedHosts_.contains(host)) return FORBIDDEN;
        if (origin != null && !allowedOrigins_.contains(origin)) return FORBIDDEN;
        String candidate = authorization != null && authorization.startsWith(BEARER)
                ? authorization.substring(BEARER.length()) : tokenParam;
        return token_.verify(candidate) ? ALLOWED : UNAUTHORIZED;
    }
}
