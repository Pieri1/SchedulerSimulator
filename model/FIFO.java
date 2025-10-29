package model;

import java.util.List;

public class FIFO implements Scheduler {

    @Override
    public Process nextProcess(List<Process> processes, int currentTime) {
        // FIFO: pick the first READY process that arrived
        for (Process p : processes) {
            if (!p.isCompleted() && p.getStartTime() <= currentTime) {
                return p;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return "FIFO";
    }
}
