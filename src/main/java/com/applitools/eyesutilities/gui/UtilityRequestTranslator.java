package com.applitools.eyesutilities.gui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * GUI form → EyesUtilities argv. The argv reaches the child process as a JCommander
 * @argsfile (one argument per line) so the key never appears in the OS process list.
 */
final class UtilityRequestTranslator {

    // JCommander's @argsfile reader trims each line and silently drops blank lines and lines
    // starting with '#', which would shift every later argument into the wrong flag.
    private static final String ARGSFILE_COMMENT_PREFIX = "#";

    private UtilityRequestTranslator() {}

    static List<String> toArgs(UtilityCommand command, UtilityRequest req) {
        Map<String, Object> values = req.values == null ? Collections.<String, Object>emptyMap() : req.values;
        List<String> urls = req.urls == null ? Collections.<String>emptyList() : req.urls;
        String key = textValue(req.key);
        if (key.isEmpty()) {
            throw new InvalidRequestException(command.id + " requires "
                    + (command.keyKind == UtilityCommand.KeyKind.VIEW_KEY ? "an Applitools view key." : "an Applitools API key."));
        }
        rejectUnknownFlags(command, values);
        requireFlags(command, values);
        requireResultSource(command, values, urls);

        List<String> args = new ArrayList<>(command.commandTokens());
        for (String flag : command.valueFlags) {
            String value = textValue(values.get(flag));
            if (value.isEmpty()) continue;
            args.add("-" + flag);
            args.add(toArgsFileLine(flag, value));
        }
        for (String flag : command.booleanFlags) {
            if (Boolean.TRUE.equals(values.get(flag))) args.add("-" + flag);
        }
        args.add("-k");
        args.add(toArgsFileLine("k", key));
        for (String url : urls) {
            String trimmed = textValue(url);
            if (!trimmed.isEmpty()) args.add(validateResultUrl(trimmed));
        }
        return args;
    }

    private static void rejectUnknownFlags(UtilityCommand command, Map<String, Object> values) {
        for (String flag : values.keySet()) {
            if (!command.valueFlags.contains(flag) && !command.booleanFlags.contains(flag)) {
                throw new InvalidRequestException("-" + flag + " is not an option of " + command.id + ".");
            }
        }
    }

    private static void requireFlags(UtilityCommand command, Map<String, Object> values) {
        for (String flag : command.requiredFlags) {
            if (textValue(values.get(flag)).isEmpty()) {
                throw new InvalidRequestException(command.id + " requires -" + flag + ".");
            }
        }
    }

    private static void requireResultSource(UtilityCommand command, Map<String, Object> values, List<String> urls) {
        boolean hasUrl = false;
        for (String url : urls) hasUrl |= !textValue(url).isEmpty();
        if (!command.acceptsResultUrls) {
            if (hasUrl) throw new InvalidRequestException(command.id + " does not take result URLs.");
            return;
        }
        boolean hasAlternative = false;
        for (String flag : UtilityCommand.URL_ALTERNATIVE_FLAGS) hasAlternative |= !textValue(values.get(flag)).isEmpty();
        if (!hasUrl && !hasAlternative) {
            throw new InvalidRequestException(
                    command.id + " needs at least one result URL, or a date range (-dr) or query (-q).");
        }
    }

    private static String textValue(Object value) {
        if (value == null || value instanceof Boolean) return "";
        return value.toString().trim();
    }

    private static String toArgsFileLine(String flag, String value) {
        if (value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            throw new InvalidRequestException("-" + flag + " must be a single line.");
        }
        if (value.startsWith(ARGSFILE_COMMENT_PREFIX)) {
            throw new InvalidRequestException("-" + flag + " cannot start with '#'.");
        }
        return value;
    }

    /** Positional values that aren't real http(s) URLs could be parsed as flags by JCommander. */
    private static String validateResultUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme)) && uri.getHost() != null) {
                return url;
            }
        } catch (URISyntaxException e) {
            // fall through to the shared error below
        }
        throw new InvalidRequestException("Not a result URL: " + url);
    }
}
