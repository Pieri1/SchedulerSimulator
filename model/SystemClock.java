package model;

import java.util.ArrayList;
import java.util.List;

public class SystemClock {
    private volatile int currentTime;
    private volatile boolean running;
    private final long tickIntervalMs;
    private final List<Runnable> listeners;
    private Thread tickThread;

    public SystemClock() {
        this(100L);
    }

    public SystemClock(long tickIntervalMs) {
        this.currentTime = 0;
        this.running = false;
        this.tickIntervalMs = Math.max(1L, tickIntervalMs);
        this.listeners = new ArrayList<>();
    }

    public synchronized void start(boolean realtime) {
        if (running) {
            return;
        }
        running = true;
        if (realtime) {
            tickThread = new Thread(() -> {
                while (running) {
                    try {
                        Thread.sleep(tickIntervalMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    if (!running) break;
                    tick();
                }
            }, "Clock-Tick-Thread");
            tickThread.setDaemon(true);
            tickThread.start();
        }
    }

    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
        if (tickThread != null) {
            tickThread.interrupt();
            try {
                tickThread.join(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                tickThread = null;
            }
        }
    }

    public void tick() {
        currentTime++;

        List<Runnable> snapshot;
        synchronized (listeners) {
            snapshot = new ArrayList<>(listeners);
        }

        for (Runnable listener : snapshot) {
            try {
                listener.run();
            } catch (Throwable t) {
            }
        }
    }

    public synchronized void reset() {
        currentTime = 0;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public void addListener(Runnable listener) {
        if (listener == null) return;
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(Runnable listener) {
        if (listener == null) return;
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public boolean isRunning() {
        return running;
    }
}