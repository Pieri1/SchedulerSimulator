package model;

import java.util.List;

public class PRIOPEnv implements Scheduler {

	// Fator de envelhecimento (alpha > 0)
	private int alpha = 1;
    private boolean lastRandom = false;

	public void setAlpha(int alpha) {
		this.alpha = (alpha > 0) ? alpha : 1;
	}

	public int getAlpha() { return alpha; }

	@Override
    public Process nextProcess(List<Process> processes, int currentTime, Process currentRunning) {
		if (processes == null || processes.isEmpty()) {
			return null;
		}

		// Calcula prioridade efetiva (maior melhor) e coleta elegíveis
        java.util.List<Process> elig = new java.util.ArrayList<>();
        int bestEff = Integer.MIN_VALUE;
        for (Process p : processes) {
            if (p == null || p.isCompleted()) continue;
            if (p.getStartTime() > currentTime) continue;
            int effective = p.getPriority() + alpha * (currentTime - p.getStartTime());
            elig.add(p);
            if (effective > bestEff) bestEff = effective;
        }
        if (elig.isEmpty()) { lastRandom = false; return null; }

        // Filtra por melhor efetiva
        java.util.List<Process> cand = new java.util.ArrayList<>();
        for (Process p : elig) {
            int eff = p.getPriority() + alpha * (currentTime - p.getStartTime());
            if (eff == bestEff) cand.add(p);
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
		return "PRIOPEnv";
	}

    @Override
    public boolean wasRandomTieBreak() { return lastRandom; }
}
