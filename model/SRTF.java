package model;

import java.util.List;

public class SRTF implements Scheduler {

	@Override
	public Process nextProcess(List<Process> processes, int currentTime) {
		// SRTF: Retorna o processo com o menor tempo restante ainda não concluído.
		if (processes == null || processes.isEmpty()) {
			return null;
		}
		// Guarda o processo menor tempo restante encontrado até agora, e startTime para desempate.
		Process chosen = null;
		int minRemaining = Integer.MAX_VALUE;
		int earliestStart = Integer.MAX_VALUE;

		for (Process p : processes) {
			// Ignora processos nulos, já concluídos ou que ainda não iniciaram.
			if (p == null) continue;
			if (p.isCompleted()) continue;
			if (p.getStartTime() > currentTime) continue; // not yet arrived

			// Calcula e guarda o tempo restante.
			int remaining = p.getDuration() - p.getRunTime();
			if (remaining < 0) remaining = 0;

			// Se menor tempo restante, escolhe direto
			if (remaining < minRemaining) {
				minRemaining = remaining;
				earliestStart = p.getStartTime();
				chosen = p;
			// Se for igual, desempata
			} else if (remaining == minRemaining) {
				int st = p.getStartTime();
				// Se startTime menor escolhe
				if (st < earliestStart) {
					earliestStart = st;
					chosen = p;
				// Se for igual desempata por ID
				} else if (st == earliestStart) {
					String id1 = p.getId();
					String id2 = chosen != null ? chosen.getId() : null;
					if (id1 != null && id2 != null && id1.compareTo(id2) < 0) {
						chosen = p;
					}
				}
			}
		}

		return chosen;
	}

	@Override
	public String getName() {
		return "SRTF";
	}
}
