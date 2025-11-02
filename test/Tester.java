package test;

import model.*;
import controller.SimController;

public class Tester {
    public static void main(String[] args) throws Exception {
        System.out.println("=== BASIC CLOCK TEST ===");
        SystemClock clock = new SystemClock(300); // 300ms between ticks
        clock.addListener(() -> System.out.println("Tick: " + clock.getCurrentTime()));
        clock.start();
        Thread.sleep(1500);
        clock.stop();
        System.out.println("Clock stopped at: " + clock.getCurrentTime());

        System.out.println("\n=== CLOCK RESET TEST ===");
        clock.reset();
        System.out.println("After reset, time = " + clock.getCurrentTime());

        System.out.println("\n=== CONFIG PARSER TEST ===");
        ConfigParser parser = new ConfigParser();
        SimulationConfig config = parser.parse("config/test.txt");

        System.out.println("Algorithm: " + config.getAlgorithmName());
        System.out.println("Quantum: " + config.getQuantum());
        System.out.println("Processes loaded:");
        for (model.Process p : config.getProcessList()) {
            System.out.println(" - " + p);
        }

        System.out.println("\n=== CLOCK + CONTROLLER TEST ===");
        SimController controller = new SimController(new SystemClock(500), config);
        controller.start();
        Thread.sleep(3000);
        controller.stop();
        System.out.println("Final clock time: " + controller.getCurrentTime());

        System.out.println("\n=== BASIC SIMULATION STEP TEST ===");
        // simulate some fake ticks manually to test Process behavior
        for (model.Process p : config.getProcessList()) {
            System.out.println("Simulating process: " + p.getId());
            for (int i = 0; i < p.getDuration(); i++) {
                p.executeTick();
                System.out.println("Tick " + (i+1) + " -> " + p);
            }
            System.out.println("Finished: " + p.isCompleted());
        }

        System.out.println("\n=== FIFO SCHEDULER TEST ===");
        config.setAlgorithmName("FIFO");
        SimController FIFO = new SimController(new SystemClock(300), config);
        FIFO.start();
        Thread.sleep(3000);
        FIFO.stop();

        System.out.println("\n=== ALL TESTS COMPLETED ===");
    }
}
