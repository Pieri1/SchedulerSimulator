package model;

import java.util.List;

public class SRTF implements Scheduler {
	private boolean lastRandom = false;

	@Override
	public Process nextProcess(List<Process> processes, int currentTime, Process currentRunning) {
		// SRTF: Retorna o processo com o menor tempo restante ainda não concluído.
		if (processes == null || processes.isEmpty()) {
			return null;
		}

		// Coleta elegíveis e encontra menor tempo restante
		java.util.List<Process> elig = new java.util.ArrayList<>();
		int bestRemaining = Integer.MAX_VALUE;
		for (Process p : processes) {
			if (p == null || p.isCompleted()) continue;
			if (p.getStartTime() > currentTime) continue;
			int remaining = p.getDuration() - p.getRunTime();
			if (remaining < 0) remaining = 0;
			elig.add(p);
			if (remaining < bestRemaining) bestRemaining = remaining;
		}
		if (elig.isEmpty()) { lastRandom = false; return null; }

		// Filtra por menor restante
		java.util.List<Process> cand = new java.util.ArrayList<>();
		for (Process p : elig) {
			int r = p.getDuration() - p.getRunTime();
			if (r < 0) r = 0;
			if (r == bestRemaining) cand.add(p);
		}

		// (1) manter atual
		if (currentRunning != null && cand.contains(currentRunning)) { lastRandom = false; return currentRunning; }

		// (2) menor start
		int minStart = Integer.MAX_VALUE;
		for (Process p : cand) if (p.getStartTime() < minStart) minStart = p.getStartTime();
		java.util.List<Process> cand2 = new java.util.ArrayList<>();
		for (Process p : cand) if (p.getStartTime() == minStart) cand2.add(p);

		// (3) menor duração total
		int minDur = Integer.MAX_VALUE;
		for (Process p : cand2) if (p.getDuration() < minDur) minDur = p.getDuration();
		java.util.List<Process> cand3 = new java.util.ArrayList<>();
		for (Process p : cand2) if (p.getDuration() == minDur) cand3.add(p);

		// (4) sorteio
		if (cand3.size() > 1) {
			lastRandom = true;
			return cand3.get(new java.util.Random().nextInt(cand3.size()));
		} else {
			lastRandom = false;
			return cand3.get(0);
		}
	}

	@Override
	public String getName() {
		return "SRTF";
	}

	@Override
	public boolean wasRandomTieBreak() { return lastRandom; }
}
