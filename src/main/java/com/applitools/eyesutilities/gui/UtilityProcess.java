package com.applitools.eyesutilities.gui;

import com.applitools.eyesutilities.EyesUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Runs one EyesUtilities command in a child JVM. A separate process — not an in-process call —
 * because the commands report via System.out (global to the GUI server), -dv flips JVM-wide SSL
 * validation, and they have no cancellation hooks: killing the child is the only clean stop.
 */
final class UtilityProcess {

    /** Starts the child for an argsfile; injectable so tests can substitute a stand-in main class. */
    interface Launcher {
        Process launch(Path argsFile, File workDir) throws IOException;
    }

    interface LineListener {
        void onLine(String line);
    }

    interface StartListener {
        void onStarted(Process process);
    }

    private static final String ARGS_FILE_PREFIX = "eyesutilities-";
    private static final String ARGS_FILE_SUFFIX = ".args";
    private static final String OWNER_ONLY = "rw-------";

    private UtilityProcess() {}

    static Launcher production() {
        return new Launcher() {
            @Override
            public Process launch(Path argsFile, File workDir) throws IOException {
                List<String> command = Arrays.asList(
                        javaExecutable().toString(),
                        // GIF commands use ImageIO; headless keeps macOS from showing a Dock icon for the child.
                        "-Djava.awt.headless=true",
                        "-cp", absoluteClassPath(System.getProperty("java.class.path")),
                        EyesUtilities.class.getName(),
                        "@" + argsFile);
                return new ProcessBuilder(command).directory(workDir).redirectErrorStream(true).start();
            }
        };
    }

    /**
     * Runs to completion, feeding each output line (stdout + stderr) to the listener. The
     * argsfile holds the key, so it is owner-readable only and deleted once the child exits.
     */
    static int run(Launcher launcher, List<String> args, File workDir,
                   StartListener onStarted, LineListener onLine) throws IOException, InterruptedException {
        Path argsFile = createArgsFile();
        try {
            Files.write(argsFile, args, StandardCharsets.UTF_8);
            Process process = launcher.launch(argsFile, workDir);
            // Nothing can answer a prompt from the GUI; EOF makes any prompt decline instead of hanging.
            process.getOutputStream().close();
            onStarted.onStarted(process);
            try (BufferedReader out = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = out.readLine()) != null) onLine.onLine(line);
            }
            return process.waitFor();
        } finally {
            Files.deleteIfExists(argsFile);
        }
    }

    /** `java -jar jars/x.jar` leaves a relative class path, which breaks once the child's cwd moves. */
    static String absoluteClassPath(String classPath) {
        List<String> entries = new ArrayList<>();
        for (String entry : classPath.split(File.pathSeparator)) {
            entries.add(Paths.get(entry).toAbsolutePath().toString());
        }
        return String.join(File.pathSeparator, entries);
    }

    private static Path createArgsFile() throws IOException {
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            return Files.createTempFile(ARGS_FILE_PREFIX, ARGS_FILE_SUFFIX,
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(OWNER_ONLY)));
        }
        // Windows: %TEMP% is already per-user and ACL'd to the owner.
        return Files.createTempFile(ARGS_FILE_PREFIX, ARGS_FILE_SUFFIX);
    }

    private static Path javaExecutable() throws IOException {
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        Path java = Paths.get(System.getProperty("java.home"), "bin", windows ? "java.exe" : "java");
        if (!Files.isExecutable(java)) {
            throw new IOException("Can't run EyesUtilities: no Java launcher at " + java + ".");
        }
        return java;
    }
}
