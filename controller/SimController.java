package controller;

import model.*;
import java.util.Locale;

public class SimController {
    private final SystemClock clock;
    private final SimulationConfig config;
    private final Scheduler scheduler;

    private model.Process currentProcess;

    public SimController(SystemClock clock, SimulationConfig config) {
        this.clock = clock;
        this.config = config;

        // Select scheduler dynamically based on config
        String algorithm = config.getAlgorithmName().toUpperCase(Locale.ROOT);
        switch (algorithm) {
            case "FIFO":
                scheduler = new FIFO();
                break;
            case "PRIOP":
                scheduler = new PRIOP(); // create later
                break;
            case "SRTF":
                scheduler = new SRTF(); // create later
                break;
            default:
                System.out.println("Unknown scheduler: " + algorithm + " (defaulting to FIFO)");
                scheduler = new FIFO();
                break;
        }

        // Subscribe to clock ticks
        clock.addListener(this::onTick);
    }

    private void onTick() {
        int time = clock.getCurrentTime();
        currentProcess = scheduler.nextProcess(config.getProcessList(), time);

        if (currentProcess != null) {
            currentProcess.executeTick();
            System.out.printf("[t=%d] Running %s (runtime=%d/%d)%n",
                    time, currentProcess.getId(),
                    currentProcess.getRunTime(), currentProcess.getDuration());
        } else {
            System.out.printf("[t=%d] CPU Idle%n", time);
        }
    }

    public void start() {
        System.out.println("Simulation starting using " + scheduler.getName() + " scheduler...");
        clock.start(true);
    }

    public void stop() {
        clock.stop();
        System.out.println("Simulation stopped at t=" + clock.getCurrentTime());
    }

    public SystemClock getClock() {
        return clock;
    }

    public int getCurrentTime() {
        return clock.getCurrentTime();
    }

    public static void main(String[] args) throws Exception {
        ConfigParser parser = new ConfigParser();
        SimulationConfig config = parser.parse("config/test.txt");

        SimController controller = new SimController(new SystemClock(500), config);
        controller.start();
        Thread.sleep(4000);
        controller.stop();
    }
}
