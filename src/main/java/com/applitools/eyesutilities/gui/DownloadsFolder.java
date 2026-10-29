package com.applitools.eyesutilities.gui;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Resolves the user's Downloads folder. On Windows, Downloads can be redirected (OneDrive,
 * folder policy), so the honest answer lives in the User Shell Folders registry key; any
 * failure falls back to ~/Downloads.
 */
final class DownloadsFolder {

    /** Windows known-folder GUID for Downloads. */
    private static final String DOWNLOADS_GUID = "{374DE290-123F-4565-9164-39C4925E467B}";
    private static final String USER_SHELL_FOLDERS_KEY =
            "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\User Shell Folders";
    private static final String USER_PROFILE_TOKEN = "%USERPROFILE%";

    private DownloadsFolder() {}

    static Path resolve() {
        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            try {
                Path fromRegistry = parseRegOutput(
                        runCommand("reg", "query", USER_SHELL_FOLDERS_KEY, "/v", DOWNLOADS_GUID),
                        System.getenv("USERPROFILE"));
                if (fromRegistry != null) return fromRegistry;
            } catch (IOException | RuntimeException e) {
                // fall through to the home-dir default
            }
        }
        return Paths.get(System.getProperty("user.home"), "Downloads");
    }

    static Path parseRegOutput(String output, String userProfile) {
        for (String line : output.split("\\r?\\n")) {
            if (!line.contains(DOWNLOADS_GUID)) continue;
            String[] columns = line.trim().split("\\s{2,}", 3);
            if (columns.length < 3) return null;
            String value = columns[2];
            if (userProfile != null) value = value.replace(USER_PROFILE_TOKEN, userProfile);
            if (value.contains("%")) return null; // an env token we can't expand
            return Paths.get(value);
        }
        return null;
    }

    private static String runCommand(String... command) throws IOException {
        Process p = new ProcessBuilder(command).redirectErrorStream(true).start();
        try (InputStream in = p.getInputStream()) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        }
    }
}
