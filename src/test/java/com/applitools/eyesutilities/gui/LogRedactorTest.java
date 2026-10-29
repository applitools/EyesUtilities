package com.applitools.eyesutilities.gui;

import org.junit.Test;

import static org.junit.Assert.*;

public class LogRedactorTest {

    @Test
    public void shouldMaskTheKeyInARequestUrl() {
        String line = "Server returned 401 for URL: https://eyes.applitools.com/api/x?ApiKey=abc123&format=json";
        assertEquals("Server returned 401 for URL: https://eyes.applitools.com/api/x?ApiKey=***&format=json",
                LogRedactor.redact(line, "abc123"));
    }

    @Test
    public void shouldMaskProxyCredentials() {
        assertEquals("via http://***:***@proxy:8080", LogRedactor.redact("via http://bob:pw@proxy:8080", null));
    }

    @Test
    public void shouldTreatKeyRegexCharactersLiterally() {
        assertEquals("k=***", LogRedactor.redact("k=a.b*c", "a.b*c"));
    }
}
