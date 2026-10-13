
package it.unibo.cus.model.classes;

/**
 * A non-blocking timer that executes a callback when its duration expires.
 */
public class Timer {

    private final String name;
    private final long durationMs;
    private final Runnable callback;

    private long startTime;
    private boolean running;
    private boolean completed;

    public Timer(String name, long durationMs, Runnable callback) {
        if (durationMs < 0) {
            throw new IllegalArgumentException(
                "Timer duration cannot be negative"
            );
        }

        this.name = name;
        this.durationMs = durationMs;
        this.callback = callback;
        this.running = false;
        this.completed = false;
    }

    public void start() {
        if (running || completed) {
            return;
        }

        startTime = System.nanoTime();
        running = true;
    }

    public void stop() {
        running = false;
    }

    public void restart() {
        completed = false;
        startTime = System.nanoTime();
        running = true;
    }

    public void update() {
        if (!running || completed) {
            return;
        }

        long elapsedMs =
            (System.nanoTime() - startTime) / 1_000_000L;

        if (elapsedMs >= durationMs) {
            running = false;
            completed = true;

            if (callback != null) {
                callback.run();
            }
        }
    }

    public void reset() {
        running = false;
        completed = false;
        startTime = 0;
    }

    public String getName() {
        return name;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isCompleted() {
        return completed;
    }
}
