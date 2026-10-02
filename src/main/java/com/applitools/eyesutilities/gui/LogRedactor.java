package com.applitools.eyesutilities.gui;

import java.util.regex.Pattern;

/** EyesUtilities logs request URLs with the key in the query string; mask before it reaches the browser. */
final class LogRedactor {

    private static final String MASK = "***";
    private static final Pattern PROXY_CREDENTIALS = Pattern.compile("(https?://)([^/:@\\s]+):([^/@\\s]+)@");

    private LogRedactor() {}

    static String redact(String line, String secret) {
        String out = line;
        if (secret != null && !secret.trim().isEmpty()) {
            // Literal replace, not regex — keys may contain regex metacharacters.
            out = out.replace(secret.trim(), MASK);
        }
        return PROXY_CREDENTIALS.matcher(out).replaceAll("$1" + MASK + ":" + MASK + "@");
    }
}
