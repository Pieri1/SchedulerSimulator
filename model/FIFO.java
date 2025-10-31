package model;

import java.util.List;

public class FIFO implements Scheduler {

    @Override
    public Process nextProcess(List<Process> processes, int currentTime) {
        // FIFO: Retorna o primeiro processo que chegou e ainda não foi concluído, ou seja, o de menor startTime.
        if (processes == null || processes.isEmpty()) {
			return null;
		}
        // Guarda o processo com menor startTime encontrado até agora.
        Process chosen = null;
        int earliestStart = Integer.MAX_VALUE;

        for (Process p : processes) {
            // Ignora processos nulos, já concluídos ou que ainda não iniciaram.
            if (p == null) continue;
            if (p.isCompleted()) continue;
            int st = p.getStartTime();
            if (st > currentTime) continue;

            // Seleciona o processo com o menor startTime visto até agora.
            if (st < earliestStart) {
                earliestStart = st;
                chosen = p;
            // No caso de empate, escolhe o processo com menor ID.
            } else if (st == earliestStart) {
                if (chosen == null) {
                    chosen = p;
                } else {
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
        return "FIFO";
    }
}
