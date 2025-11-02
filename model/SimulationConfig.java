package model;

import java.util.ArrayList;
import java.util.List;

public class SimulationConfig {
    // Configurações globais da simulação e lista de processos
    private String algorithmName;
    private int quantum;
    private int runMode = 1; // 0 = step-by-step, 1 = automático
    private final List<Process> processList;

    public SimulationConfig() {
        this.processList = new ArrayList<>();
    }

    // Getters e Setters
    public String getAlgorithmName() { return algorithmName; }
    public void setAlgorithmName(String algorithmName) { this.algorithmName = algorithmName; }

    public int getQuantum() { return quantum; }
    public void setQuantum(int quantum) { this.quantum = quantum; }

    public int getRunMode() { return runMode; }
    public void setRunMode(int runMode) { this.runMode = runMode; }

    public List<Process> getProcessList() { return processList; }
}
