package com.applitools.eyesutilities.gui;

import org.junit.Test;

import static org.junit.Assert.*;

public class RequestGuardTest {

    private static final int PORT = 4242;
    private static final GuiToken TOKEN = GuiToken.generate();
    private static final RequestGuard GUARD = new RequestGuard(TOKEN, PORT);
    private static final String BEARER = "Bearer " + TOKEN.value();

    @Test
    public void shouldAllowLocalhostWithBearerToken() {
        assertEquals(RequestGuard.ALLOWED, GUARD.check("localhost:" + PORT, null, BEARER, null));
    }

    @Test
    public void shouldAllowTokenQueryParameterForEventStream() {
        assertEquals(RequestGuard.ALLOWED, GUARD.check("127.0.0.1:" + PORT, null, null, TOKEN.value()));
    }

    @Test
    public void shouldForbidForeignHost() {
        assertEquals(RequestGuard.FORBIDDEN, GUARD.check("evil.example:" + PORT, null, BEARER, null));
    }

    @Test
    public void shouldForbidForeignOrigin() {
        assertEquals(RequestGuard.FORBIDDEN, GUARD.check("localhost:" + PORT, "https://evil.example", BEARER, null));
    }

    @Test
    public void shouldRejectMissingToken() {
        assertEquals(RequestGuard.UNAUTHORIZED, GUARD.check("localhost:" + PORT, null, null, null));
    }

    @Test
    public void shouldRejectWrongToken() {
        assertEquals(RequestGuard.UNAUTHORIZED, GUARD.check("localhost:" + PORT, null, "Bearer nope", null));
    }
}
