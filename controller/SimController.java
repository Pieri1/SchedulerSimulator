package controller;

import model.*;
import java.util.Locale;

public class SimController {
    private final SystemClock clock;
    private final SimulationConfig config;
    private final Scheduler scheduler;
    private final GanttChart ganttChart;

    private model.Process currentProcess;
    private String lastProcessId;
    private int lastStartTime;

    public SimController(SystemClock clock, SimulationConfig config) {
        this.clock = clock;
        this.config = config;
        this.ganttChart = new GanttChart();
        this.lastProcessId = null;
        this.lastStartTime = 0;

        // Select scheduler dynamically based on config
        String algorithm = config.getAlgorithmName().toUpperCase(Locale.ROOT);
        switch (algorithm) {
            case "FIFO":
                scheduler = new FIFO();
                break;
            case "PRIOP":
                scheduler = new PRIOP();
                break;
            case "SRTF":
                scheduler = new SRTF();
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
        model.Process previousProcess = currentProcess;
        currentProcess = scheduler.nextProcess(config.getProcessList(), time);

        // Get process IDs for Gantt chart
        String previousProcessId = (previousProcess != null) ? previousProcess.getId() : null;
        String currentProcessId = (currentProcess != null) ? currentProcess.getId() : null;

        // Detect process change and record in Gantt chart
        if (previousProcessId != null && !previousProcessId.equals(currentProcessId)) {
            // Process changed, record the previous process execution
            ganttChart.addCustomEvent(previousProcessId, lastStartTime, time, "running");
            lastStartTime = time;
        } else if (previousProcessId == null && currentProcessId != null) {
            // Started first process
            lastStartTime = time;
        }

        // Execute current process
        if (currentProcess != null) {
            currentProcess.executeTick();
            System.out.printf("[t=%d] Running %s (runtime=%d/%d)%n",
                    time, currentProcess.getId(),
                    currentProcess.getRunTime(), currentProcess.getDuration());
            
            // Check if process completed
            if (currentProcess.isCompleted()) {
                ganttChart.addCustomEvent(currentProcessId, lastStartTime, time + 1, "terminated");
                lastStartTime = time + 1;
                currentProcess = null;
            }
        } else {
            System.out.printf("[t=%d] CPU Idle%n", time);
        }

        lastProcessId = currentProcessId;
    }

    public void start() {
        System.out.println("Simulation starting using " + scheduler.getName() + " scheduler...");
        lastStartTime = 0;
        clock.start(true);
    }

    public void stop() {
        int finalTime = clock.getCurrentTime();
        
        // Record any remaining process execution
        if (lastProcessId != null && currentProcess != null && !currentProcess.isCompleted()) {
            ganttChart.addCustomEvent(lastProcessId, lastStartTime, finalTime, "running");
        }
        
        clock.stop();
        
        // Generate Gantt chart
        ganttChart.generateGanttChart("simulation_gantt.svg");
        
        System.out.println("Simulation stopped at t=" + finalTime);
        System.out.println("Gantt chart generated: simulation_gantt.svg");
    }

    public SystemClock getClock() {
        return clock;
    }

    public int getCurrentTime() {
        return clock.getCurrentTime();
    }

    public GanttChart getGanttChart() {
        return ganttChart;
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