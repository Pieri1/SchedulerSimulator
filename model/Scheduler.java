package model;

import java.util.List;

public interface Scheduler {
    // Serve apenas de interface para os algoritmos.
    Process nextProcess(List<Process> processes, int currentTime);
    String getName();
}
