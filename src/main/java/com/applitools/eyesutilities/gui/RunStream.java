package com.applitools.eyesutilities.gui;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Fans SSE events out to every open page. Each client gets its own queue and drainer thread so
 * a slow or dead connection never blocks the run, and a replay buffer so a tab opened mid-run
 * still sees the log so far.
 */
final class RunStream {

    private static final int CLIENT_QUEUE_CAPACITY = 16384;
    private static final int REPLAY_CAPACITY = 16384;
    private static final long POLL_MS = 250;
    // Comment frames keep idle connections from being reaped and surface a closed tab promptly.
    private static final long HEARTBEAT_MS = 15_000;
    private static final String HEARTBEAT_FRAME = ": keep-alive\n\n";

    private final List<Client> clients_ = new CopyOnWriteArrayList<>();
    private final ObjectMapper json_ = new ObjectMapper();
    private final Deque<SseEvent> replay_ = new ArrayDeque<>();

    /** Streams to {@code writer} until the client goes away; {@code onClose} runs once, after. */
    void addClient(Writer writer, Runnable onClose) {
        List<SseEvent> snapshot;
        synchronized (replay_) { snapshot = new ArrayList<>(replay_); }
        Client c = new Client(writer, onClose, snapshot);
        clients_.add(c);
        c.start();
    }

    int activeClientCount() { return clients_.size(); }

    void emit(SseEvent event) {
        synchronized (replay_) {
            replay_.addLast(event);
            while (replay_.size() > REPLAY_CAPACITY) replay_.removeFirst();
        }
        for (Client c : clients_) c.offer(event);
    }

    /** Call at the start of a run so a fresh tab doesn't replay the previous run's tail. */
    void resetReplay() {
        synchronized (replay_) { replay_.clear(); }
    }

    void close() {
        for (Client c : clients_) c.stop();
        clients_.clear();
    }

    private final class Client {
        private final Writer writer_;
        private final Runnable onClose_;
        private final List<SseEvent> replay_;
        private final BlockingQueue<SseEvent> queue_ = new ArrayBlockingQueue<>(CLIENT_QUEUE_CAPACITY);
        private volatile boolean alive_ = true;
        private Thread drainer_;

        Client(Writer writer, Runnable onClose, List<SseEvent> replay) {
            this.writer_ = writer;
            this.onClose_ = onClose;
            this.replay_ = replay;
        }

        void start() {
            drainer_ = new Thread(this::drain, "RunStream-drainer");
            drainer_.setDaemon(true);
            drainer_.start();
        }

        private void drain() {
            try {
                for (SseEvent ev : replay_) write(ev);
                writer_.flush();
                long lastWriteMs = System.currentTimeMillis();
                while (alive_) {
                    SseEvent ev = queue_.poll(POLL_MS, TimeUnit.MILLISECONDS);
                    if (ev != null) {
                        write(ev);
                    } else if (System.currentTimeMillis() - lastWriteMs < HEARTBEAT_MS) {
                        continue;
                    } else {
                        writer_.write(HEARTBEAT_FRAME);
                    }
                    writer_.flush();
                    lastWriteMs = System.currentTimeMillis();
                }
            } catch (IOException | InterruptedException e) {
                // The tab closed or the server is stopping — either way this client is done.
            } finally {
                alive_ = false;
                clients_.remove(this);
                onClose_.run();
            }
        }

        private void write(SseEvent ev) throws IOException {
            writer_.write("data: " + json_.writeValueAsString(ev) + "\n\n");
        }

        void offer(SseEvent ev) {
            // A client too far behind to keep up loses this event rather than stalling the run.
            if (alive_) queue_.offer(ev);
        }

        void stop() {
            alive_ = false;
            if (drainer_ != null) drainer_.interrupt();
        }
    }
}
