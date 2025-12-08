package model;

import java.util.List;

public interface Scheduler {
    // Serve apenas de interface para os algoritmos.
    Process nextProcess(List<Process> processes, int currentTime, Process currentRunning);
    String getName();

    /**
     * Indica se a última seleção utilizou sorteio como critério de desempate.
     * Implementações podem sobrescrever. Por padrão, retorna false.
     */
    default boolean wasRandomTieBreak() { return false; }
}
