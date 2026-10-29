package com.applitools.eyesutilities.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

/** Opens the GUI in the default browser, falling back to the OS opener when AWT can't. */
final class GuiLauncher {

    private GuiLauncher() {}

    static void open(String url) {
        if (tryDesktop(url)) return;
        tryShell(url);
    }

    private static boolean tryDesktop(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                return true;
            }
        } catch (Exception | LinkageError e) {
            // headless or unsupported desktop — try the shell instead
        }
        return false;
    }

    private static void tryShell(String url) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        try {
            if (os.contains("win")) new ProcessBuilder("cmd", "/c", "start", "", url).start();
            else if (os.contains("mac")) new ProcessBuilder("open", url).start();
            else new ProcessBuilder("xdg-open", url).start();
        } catch (IOException e) {
            System.out.println("Could not open a browser. Open " + url + " manually.");
        }
    }
}
