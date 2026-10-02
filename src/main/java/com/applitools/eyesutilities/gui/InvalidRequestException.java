package com.applitools.eyesutilities.gui;

/** A run request the user can fix (bad flag, missing field) — answered with 400 and this message. */
final class InvalidRequestException extends RuntimeException {
    InvalidRequestException(String message) { super(message); }
}
