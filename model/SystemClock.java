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

    public synchronized void start() {
        // Se já estiver rodando, ignora
        if (running) {
            return;
        }
        running = true;
        // cria thread para ticks periódicos
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

    public synchronized void stop() {
        // Se já estiver parado, ignora
        if (!running) {
            return;
        }
        running = false;
        // Interrompe a thread
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
        // Notifica os listeners com o tempo atual primeiro (permite que t=0 seja processado)
        List<Runnable> snapshot;
        synchronized (listeners) {
            snapshot = new ArrayList<>(listeners);
        }

        for (Runnable listener : snapshot) {
            try {
                listener.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        // Depois de notificar, incrementa o tempo para o próximo tick
        currentTime++;
    }

    public synchronized void reset() {
        // Reseta o clock
        currentTime = 0;
    }

    public int getCurrentTime() {
        // Getter do tempo atual
        return currentTime;
    }

    public void addListener(Runnable listener) {
        // Adiciona um listener pra lista
        if (listener == null) return;
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(Runnable listener) {
        // Remove um listener da lista
        if (listener == null) return;
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public boolean isRunning() {
        // Getter do estado
        return running;
    }
}