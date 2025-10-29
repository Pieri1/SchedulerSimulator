package model;

import java.util.List;

public class SRTF implements Scheduler {


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
		return "SRTF";
	}
}
