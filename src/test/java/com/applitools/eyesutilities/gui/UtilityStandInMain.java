package com.applitools.eyesutilities.gui;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Child-process stand-in for EyesUtilities in RunController tests: echoes the argsfile
 * (which carries the key, like EyesUtilities' own URL logging does) then optionally sleeps.
 * Usage: UtilityStandInMain <argsFile> [sleepMs]
 */
public final class UtilityStandInMain {
    public static void main(String[] args) throws Exception {
        for (String line : Files.readAllLines(Paths.get(args[0]), StandardCharsets.UTF_8)) {
            System.out.println("arg: " + line);
        }
        System.out.flush();
        if (args.length > 1) Thread.sleep(Long.parseLong(args[1]));
    }
}
