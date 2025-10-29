package model;

import java.util.ArrayList;
import java.util.List;

public class Process {
    private String id;
    private int color;
    private int startTime;
    private int duration;
    private int priority;

    private boolean isCompleted;
    private int runTime;
    private int waitTime;

    private List<Event> eventList;
    private String state; // "NEW", "READY", "RUNNING", "WAITING", "TERMINATED"

    public Process() {
        this.eventList = new ArrayList<>();
        this.state = "NEW";
        this.isCompleted = false;
        this.runTime = 0;
        this.waitTime = 0;
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public int getStartTime() { return startTime; }
    public void setStartTime(int startTime) { this.startTime = startTime; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public int getRunTime() { return runTime; }
    public void setRunTime(int runTime) { this.runTime = runTime; }

    public int getWaitTime() { return waitTime; }
    public void setWaitTime(int waitTime) { this.waitTime = waitTime; }

    public List<Event> getEventList() { return eventList; }
    public void setEventList(List<Event> eventList) { this.eventList = eventList; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    // --- Behavioral methods ---
    public void executeTick() {
        if (!isCompleted) {
            runTime++;
            if (runTime >= duration) {
                isCompleted = true;
                state = "TERMINATED";
            } else {
                state = "RUNNING";
            }
        }
    }

    public void waitTick() {
        if (!isCompleted) {
            waitTime++;
            state = "WAITING";
        }
    }

    public void reset() {
        runTime = 0;
        waitTime = 0;
        isCompleted = false;
        state = "NEW";
    }

    @Override
    public String toString() {
        return String.format("Process %s [start=%d, duration=%d, priority=%d, state=%s]",
                id, startTime, duration, priority, state);
    }
}
