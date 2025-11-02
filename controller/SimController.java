package controller;

import model.*;
import java.util.Locale;

public class SimController {
    private final SystemClock clock;
    private final SimulationConfig config;
    private final Scheduler scheduler;
    private final GanttChart ganttChart;

    private model.Process currentProcess;
    private int currentStartTime;
    private String currentProcessId;
    private boolean wasIdle;

    public SimController(SystemClock clock, SimulationConfig config) {
        this.clock = clock;
        this.config = config;
        this.ganttChart = new GanttChart();
        this.currentProcess = null;
        this.currentStartTime = 0;
        this.currentProcessId = null;
        this.wasIdle = true; // Começa com CPU idle

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
        String previousProcessId = currentProcessId;
        
        // Get next process from scheduler
        currentProcess = scheduler.nextProcess(config.getProcessList(), time);
        currentProcessId = (currentProcess != null) ? currentProcess.getId() : "IDLE";

        System.out.printf("[t=%d] Scheduler selected: %s -> %s%n", 
                         time, previousProcessId, currentProcessId);

        // Detect process changes and record in Gantt chart
        boolean processChanged = !currentProcessId.equals(previousProcessId != null ? previousProcessId : "IDLE");
        
        if (processChanged && previousProcessId != null && !previousProcessId.equals("IDLE")) {
            // Process changed, record the previous process execution
            ganttChart.recordExecution(previousProcessId, currentStartTime, time);
            System.out.printf("  -> Process change: %s finished at %d%n", previousProcessId, time);
        }
        
        if (processChanged && !currentProcessId.equals("IDLE")) {
            // Start recording new process
            currentStartTime = time;
            System.out.printf("  -> Process start: %s at %d%n", currentProcessId, time);
        }

        // Execute current process
        if (currentProcess != null && !currentProcessId.equals("IDLE")) {
            currentProcess.executeTick();
            System.out.printf("[t=%d] Running %s (runtime=%d/%d, completed=%s)%n",
                    time, currentProcess.getId(),
                    currentProcess.getRunTime(), currentProcess.getDuration(),
                    currentProcess.isCompleted());
            
            // Check if process completed during this tick
            if (currentProcess.isCompleted()) {
                ganttChart.recordExecution(currentProcessId, currentStartTime, time + 1);
                System.out.printf("  -> Process completed: %s at %d%n", currentProcessId, time + 1);
                currentProcess = null;
                currentProcessId = "IDLE";
                currentStartTime = time + 1;
            }
        } else {
            System.out.printf("[t=%d] CPU Idle%n", time);
            currentProcessId = "IDLE";
        }
    }

    public void start() {
        System.out.println("Simulation starting using " + scheduler.getName() + " scheduler...");
        System.out.println("Processes in simulation:");
        for (model.Process p : config.getProcessList()) {
            System.out.println("  " + p);
        }
        
        currentStartTime = 0;
        clock.start(true);
    }

    public void stop() {
        int finalTime = clock.getCurrentTime();
        clock.stop();
        
        // Record any final execution
        if (currentProcessId != null && !currentProcessId.equals("IDLE")) {
            ganttChart.recordExecution(currentProcessId, currentStartTime, finalTime);
            System.out.printf("Final recording: %s from %d to %d%n", 
                            currentProcessId, currentStartTime, finalTime);
        }
        
        // Generate Gantt chart
        ganttChart.generateChart("simulation_gantt.svg");
        
        System.out.println("Simulation stopped at t=" + finalTime);
        System.out.println("Gantt chart generated: simulation_gantt.svg");
        
        // Show recorded events
        System.out.println("Events recorded:");
        for (GanttChart.GanttEvent event : ganttChart.getEvents()) {
            System.out.println("  " + event);
        }
        
        // Show process completion status
        System.out.println("Process completion status:");
        for (model.Process p : config.getProcessList()) {
            System.out.printf("  %s: runtime=%d/%d, completed=%s%n", 
                            p.getId(), p.getRunTime(), p.getDuration(), p.isCompleted());
        }
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
        view.UIConfigurator.main(args);

        /* 
        ConfigParser parser = new ConfigParser();
        SimulationConfig config = parser.parse("config/test.txt");

        SimController controller = new SimController(new SystemClock(500), config);
        controller.start();
        Thread.sleep(4000);
        controller.stop();
        */
    }
}