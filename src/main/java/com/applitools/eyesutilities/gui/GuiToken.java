package com.applitools.eyesutilities.gui;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Per-launch secret the page must present on every /api call. */
final class GuiToken {

    private static final int TOKEN_BYTES = 32;

    private final String value_;
    private final byte[] bytes_;

    private GuiToken(String value) {
        this.value_ = value;
        this.bytes_ = value.getBytes(StandardCharsets.UTF_8);
    }

    static GuiToken generate() {
        byte[] raw = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(raw);
        return new GuiToken(Base64.getUrlEncoder().withoutPadding().encodeToString(raw));
    }

    String value() { return value_; }

    /** Constant-time, so response timing can't be used to guess the token byte by byte. */
    boolean verify(String candidate) {
        if (candidate == null || candidate.isEmpty()) return false;
        return MessageDigest.isEqual(bytes_, candidate.getBytes(StandardCharsets.UTF_8));
    }
}
