package com.applitools.eyesutilities.gui;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.*;

public class RunControllerTest {

    private static final String VIEW_KEY = "vk_secret_value";
    private static final String RESULT_URL = "https://eyes.applitools.com/app/batches/111/222";
    private static final long LONG_SLEEP_MS = 60_000;
    private static final long DONE_TIMEOUT_MS = 20_000;
    private static final long POLL_MS = 50;

    @Rule public TemporaryFolder tmp = new TemporaryFolder();

    private final StringWriter sse = new StringWriter();

    @Test
    public void shouldRedactTheKeyFromUtilityOutput() throws Exception {
        RunController c = controller(standIn(null));
        c.start(diffsRequest());
        awaitDone(c);
        // The stand-in echoes the "-k" line's value; it must arrive masked, not missing.
        assertTrue(sse.toString().contains("arg: ***"));
    }

    @Test
    public void shouldStreamUtilityOutputToTheLog() throws Exception {
        RunController c = controller(standIn(null));
        c.start(diffsRequest());
        awaitDone(c);
        assertTrue(sse.toString().contains("arg: " + RESULT_URL));
    }

    @Test
    public void shouldReportTheChildExitCode() throws Exception {
        RunController c = controller(standIn(null));
        c.start(diffsRequest());
        assertEquals(0, awaitDone(c).exitCode);
    }

    @Test
    public void shouldRejectSecondRunWhileOneIsRunning() throws Exception {
        RunController c = controller(standIn(LONG_SLEEP_MS));
        c.start(diffsRequest());
        try {
            assertThrows(RunController.RunInProgressException.class, () -> c.start(diffsRequest()));
        } finally {
            c.cancel();
            awaitDone(c);
        }
    }

    @Test
    public void shouldMarkCancelledRun() throws Exception {
        RunController c = controller(standIn(LONG_SLEEP_MS));
        c.start(diffsRequest());
        c.cancel();
        assertTrue(awaitDone(c).cancelled);
    }

    @Test
    public void shouldRunInTheOutputFolder() throws Exception {
        List<File> dirs = new CopyOnWriteArrayList<>();
        RunController c = controller((argsFile, dir) -> {
            dirs.add(dir);
            return standIn(null).launch(argsFile, dir);
        });
        c.start(diffsRequest());
        awaitDone(c);
        assertEquals(tmp.getRoot().toPath().resolve("out").toFile(), dirs.get(0));
    }

    @Test
    public void shouldDeleteTheArgsFileAfterTheChildExits() throws Exception {
        List<Path> seen = new CopyOnWriteArrayList<>();
        RunController c = controller((argsFile, dir) -> {
            seen.add(argsFile);
            return standIn(null).launch(argsFile, dir);
        });
        c.start(diffsRequest());
        awaitDone(c);
        assertFalse(Files.exists(seen.get(0)));
    }

    @Test
    public void shouldRejectInvalidRequestBeforeStarting() {
        RunController c = controller(standIn(null));
        UtilityRequest r = diffsRequest();
        r.urls = Collections.emptyList();
        assertThrows(InvalidRequestException.class, () -> c.start(r));
    }

    private RunController controller(UtilityProcess.Launcher launcher) {
        RunStream stream = new RunStream();
        stream.addClient(sse, () -> { });
        return new RunController(stream, launcher, () -> tmp.getRoot().toPath().resolve("out"));
    }

    private static UtilityRequest diffsRequest() {
        UtilityRequest r = new UtilityRequest();
        r.command = "diffs";
        r.values = new HashMap<>();
        r.urls = Collections.singletonList(RESULT_URL);
        r.key = VIEW_KEY;
        return r;
    }

    /** Runs UtilityStandInMain in a real child JVM, as the production launcher runs EyesUtilities. */
    private static UtilityProcess.Launcher standIn(Long sleepMs) {
        return (argsFile, workDir) -> {
            String java = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
            List<String> cmd = new ArrayList<>(Arrays.asList(java, "-cp",
                    UtilityProcess.absoluteClassPath(System.getProperty("java.class.path")),
                    UtilityStandInMain.class.getName(), argsFile.toString()));
            if (sleepMs != null) cmd.add(String.valueOf(sleepMs));
            return new ProcessBuilder(cmd).directory(workDir).redirectErrorStream(true).start();
        };
    }

    private RunState.Done awaitDone(RunController c) throws InterruptedException {
        long deadline = System.currentTimeMillis() + DONE_TIMEOUT_MS;
        while (!(c.snapshot() instanceof RunState.Done) && System.currentTimeMillis() < deadline) {
            Thread.sleep(POLL_MS);
        }
        assertTrue("run did not finish in time", c.snapshot() instanceof RunState.Done);
        // run-finished is emitted just after the state flips; give the SSE drainer a beat to write it.
        Thread.sleep(POLL_MS * 4);
        return (RunState.Done) c.snapshot();
    }
}
