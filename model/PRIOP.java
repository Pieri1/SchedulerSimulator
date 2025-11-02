package model;

import java.util.List;

public class PRIOP implements Scheduler {

	@Override
	public Process nextProcess(List<Process> processes, int currentTime) {
		// PRIOP: Retorna o processo com maior prioridade disponível ainda não concluído.
		if (processes == null || processes.isEmpty()) {
			return null;
		}
		// Guarda o processo com maior prioridade encontrado até agora.
		Process chosen = null;
		int bestPriority = Integer.MIN_VALUE;
		int earliestStart = Integer.MAX_VALUE;

		for (Process p : processes) {
			// Ignora processos nulos, já concluídos ou que ainda não iniciaram.
			if (p == null) continue;
			if (p.isCompleted()) continue;
			if (p.getStartTime() > currentTime) continue;

			// Guarda prioridade e startTime para desempate.
			int prio = p.getPriority();
			int st = p.getStartTime();

			// Se vazio escolhe direto
			if (chosen == null) {
				chosen = p;
				bestPriority = prio;
				earliestStart = st;
				continue;
			}

			// Escolhe se prioridade maior
			if (prio > bestPriority) {
				chosen = p;
				bestPriority = prio;
				earliestStart = st;
			// Se for igual, desempata
			} else if (prio == bestPriority) {
				// Se tiver diferença de startTime escolhe o primeiro
				if (st < earliestStart) {
					chosen = p;
					earliestStart = st;
				// Se for igual desempata por ID
				} else if (st == earliestStart) {
					String id1 = p.getId();
					String id2 = chosen.getId();
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
		return "PRIOP";
	}
}
