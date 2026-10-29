package com.applitools.eyesutilities.gui;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** Owns the single in-flight utility run: start, cancel, and the state the page polls. */
final class RunController {

    static final class RunInProgressException extends RuntimeException {
        RunInProgressException() { super("A utility is already running."); }
    }

    interface OutputDirProvider {
        Path get();
    }

    private static final int LAUNCH_FAILED_EXIT_CODE = -1;
    private static final String OUTPUT_FOLDER = "EyesUtilities";

    private final RunStream stream_;
    private final UtilityProcess.Launcher launcher_;
    private final OutputDirProvider outputDir_;
    private final AtomicReference<RunState> state_ = new AtomicReference<RunState>(RunState.Idle.INSTANCE);
    private final AtomicReference<Process> currentProcess_ = new AtomicReference<>();
    // Per-run flag, so a cancel that lands before the child process exists still stops it.
    private final AtomicReference<AtomicBoolean> currentCancel_ = new AtomicReference<>();
    private final ExecutorService runExecutor_ = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "RunController-run");
        t.setDaemon(true);
        return t;
    });

    RunController(RunStream stream) {
        this(stream, UtilityProcess.production(), () -> DownloadsFolder.resolve().resolve(OUTPUT_FOLDER));
    }

    RunController(RunStream stream, UtilityProcess.Launcher launcher, OutputDirProvider outputDir) {
        this.stream_ = stream;
        this.launcher_ = launcher;
        this.outputDir_ = outputDir;
    }

    RunState snapshot() { return state_.get(); }
    RunStream stream() { return stream_; }

    String start(UtilityRequest req) {
        UtilityCommand command = UtilityCommand.fromId(req.command);
        List<String> args = UtilityRequestTranslator.toArgs(command, req);

        RunState.Running running = new RunState.Running(command.id);
        // Race-safe transition: only one caller can win; a stale Done is replaced atomically.
        while (true) {
            RunState current = state_.get();
            if (current instanceof RunState.Running) throw new RunInProgressException();
            if (state_.compareAndSet(current, running)) break;
        }
        AtomicBoolean cancelled = new AtomicBoolean(false);
        currentCancel_.set(cancelled);
        stream_.resetReplay();
        // Emitted before the HTTP response returns, so the page can enter "running" from the stream.
        stream_.emit(new SseEvent.RunStarted(running.runId, command.id));
        runExecutor_.submit(() -> execute(command, args, req.key, running, cancelled));
        return running.runId;
    }

    /** Stops the child. A merge/copy/delete it already sent to the server is not rolled back. */
    void cancel() {
        AtomicBoolean cancelled = currentCancel_.get();
        if (cancelled != null) cancelled.set(true);
        Process process = currentProcess_.get();
        if (process != null) process.destroy();
    }

    private void execute(UtilityCommand command, List<String> args, String key,
                         RunState.Running running, AtomicBoolean cancelled) {
        long startedNs = System.nanoTime();
        Path outputDir = outputDir_.get();
        UtilityProcess.LineListener emitLine = line -> stream_.emit(
                new SseEvent.LogLine(LogRedactor.redact(line, key) + System.lineSeparator()));
        int exitCode = LAUNCH_FAILED_EXIT_CODE;
        try {
            Files.createDirectories(outputDir);
            emitLine.onLine("Output folder: " + outputDir);
            File workDir = outputDir.toFile();
            exitCode = UtilityProcess.run(launcher_, args, workDir, process -> {
                currentProcess_.set(process);
                if (cancelled.get()) process.destroy();
            }, emitLine);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            emitLine.onLine("Could not run EyesUtilities: " + e.getMessage());
        } finally {
            currentProcess_.set(null);
            currentCancel_.set(null);
            if (cancelled.get()) emitLine.onLine("Run cancelled");
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNs);
            state_.set(new RunState.Done(running.runId, command.id, exitCode, cancelled.get(), durationMs, outputDir.toString()));
            stream_.emit(new SseEvent.RunFinished(command.id, exitCode, cancelled.get(), durationMs, outputDir.toString()));
        }
    }
}
