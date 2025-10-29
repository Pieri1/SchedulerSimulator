package model;

import java.util.List;

public interface Scheduler {
    Process nextProcess(List<Process> processes, int currentTime);
    String getName();
}
