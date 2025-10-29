package model;

import java.util.List;

public class PRIOP implements Scheduler {

	@Override
	public Process nextProcess(List<Process> processes, int currentTime) {
		if (processes == null || processes.isEmpty()) {
			return null;
		}
		// simple default: return the first available process
		return processes.get(0);
	}

	@Override
	public String getName() {
		return "PRIOP";
	}
}
