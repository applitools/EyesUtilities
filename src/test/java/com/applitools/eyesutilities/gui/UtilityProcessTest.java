package com.applitools.eyesutilities.gui;

import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class UtilityProcessTest {

    @Test
    public void shouldMakeRelativeClassPathEntriesAbsolute() {
        String relative = "jars" + File.separator + "EyesUtilities.jar";
        assertEquals(Paths.get(relative).toAbsolutePath().toString(), UtilityProcess.absoluteClassPath(relative));
    }

    @Test
    public void shouldKeepEveryClassPathEntry() {
        String classPath = "a.jar" + File.pathSeparator + "b.jar";
        assertEquals(2, UtilityProcess.absoluteClassPath(classPath).split(File.pathSeparator).length);
    }
}
