package model;

import java.util.List;

public class FIFO implements Scheduler {
    private boolean lastRandom = false;

    @Override
    public Process nextProcess(List<Process> processes, int currentTime, Process currentRunning) {
        // FIFO: Retorna o primeiro processo que chegou e ainda não foi concluído, ou seja, o de menor startTime.
        if (processes == null || processes.isEmpty()) {
			return null;
		}
        // Coleta elegíveis e encontra menor startTime (primeiro a chegar)
        java.util.List<Process> elig = new java.util.ArrayList<>();
        int bestStart = Integer.MAX_VALUE;
        for (Process p : processes) {
            if (p == null || p.isCompleted()) continue;
            if (p.getStartTime() > currentTime) continue;
            elig.add(p);
            if (p.getStartTime() < bestStart) bestStart = p.getStartTime();
        }
        if (elig.isEmpty()) { lastRandom = false; return null; }
        // Filtra por menor startTime (critério principal)
        java.util.List<Process> cand = new java.util.ArrayList<>();
        for (Process p : elig) if (p.getStartTime() == bestStart) cand.add(p);

        // Desempates: (1) manter a atual, se presente
        if (currentRunning != null && cand.contains(currentRunning)) { lastRandom = false; return currentRunning; }

        // (2) menor instante de ingresso (já aplicamos bestStart), mas pode haver empates com mesmo startTime
        // (3) menor duração total
        int minDur = Integer.MAX_VALUE;
        for (Process p : cand) if (p.getDuration() < minDur) minDur = p.getDuration();
        java.util.List<Process> cand2 = new java.util.ArrayList<>();
        for (Process p : cand) if (p.getDuration() == minDur) cand2.add(p);

        // (4) sorteio se ainda houver empate
        if (cand2.size() > 1) {
            lastRandom = true;
            return cand2.get(new java.util.Random().nextInt(cand2.size()));
        } else {
            lastRandom = false;
            return cand2.get(0);
        }
    }

    @Override
    public String getName() {
        return "FIFO";
    }

    @Override
    public boolean wasRandomTieBreak() { return lastRandom; }
}
