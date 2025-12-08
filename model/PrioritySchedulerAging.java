package model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Escalonador Preemptivo por Prioridades com Envelhecimento (PRIOP + Aging)
 */
public class PrioritySchedulerAging {
    
    // Configurações do escalonador
    private int agingInterval;
    private int maxPriority;
    private int minPriority;
    private boolean preemptive;
    
    // Estados dos processos
    private List<Process> readyQueue;
    private Process currentProcess;
    private List<Process> finishedProcesses;
    private List<Process> allProcesses;
    
    // Estatísticas
    private int currentTime;
    private int agingCounter;
    private List<ProcessEvent> timeline;
    
    /**
     * Classe Processo com suporte a prioridades dinâmicas
     */
    public class Process {
        private String id;
        private int arrivalTime;
        private int burstTime;
        private int remainingTime;
        private int originalPriority;
        private int currentPriority;
        private int waitingTime;
        private int executionTime;
        private int turnaroundTime;
        private List<ExecutionSlot> executionSlots;
        
        // Para envelhecimento
        private int timeInReadyQueue;
        private int lastAgingTime;
        
        public Process(String id, int arrivalTime, int burstTime, int priority) {
            this.id = id;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.remainingTime = burstTime;
            this.originalPriority = priority;
            this.currentPriority = priority;
            this.waitingTime = 0;
            this.executionTime = 0;
            this.turnaroundTime = 0;
            this.executionSlots = new ArrayList<>();
            this.timeInReadyQueue = 0;
            this.lastAgingTime = arrivalTime;
        }
        
        // Getters e Setters
        public String getId() { return id; }
        public int getArrivalTime() { return arrivalTime; }
        public int getBurstTime() { return burstTime; }
        public int getRemainingTime() { return remainingTime; }
        public int getOriginalPriority() { return originalPriority; }
        public int getCurrentPriority() { return currentPriority; }
        public int getWaitingTime() { return waitingTime; }
        public int getExecutionTime() { return executionTime; }
        public int getTurnaroundTime() { return turnaroundTime; }
        public List<ExecutionSlot> getExecutionSlots() { return executionSlots; }
        
        public void setCurrentPriority(int priority) { 
            this.currentPriority = priority; 
        }
        
        public void execute(int time) {
            this.remainingTime -= time;
            this.executionTime += time;
        }
        
        public void wait(int time) {
            this.waitingTime += time;
            this.timeInReadyQueue += time;
        }
        
        public void addExecutionSlot(int start, int end) {
            executionSlots.add(new ExecutionSlot(start, end));
        }
        
        public void finish(int completionTime) {
            this.turnaroundTime = completionTime - arrivalTime;
        }
        
        public void applyAging(int currentTime) {
            // Aumenta prioridade baseado no tempo na fila de pronto
            int timeSinceLastAging = currentTime - lastAgingTime;
            if (timeSinceLastAging > 0) {
                // A cada unidade de tempo, aumenta 1 ponto de prioridade
                // (em prioridades, números menores = maior prioridade)
                int agingBonus = timeSinceLastAging;
                // CORREÇÃO: Usar variáveis da classe externa
                currentPriority = Math.max(PrioritySchedulerAging.this.minPriority, 
                                          currentPriority - agingBonus);
                lastAgingTime = currentTime;
                timeInReadyQueue = 0;
            }
        }
        
        public void applyPriorityDecay() {
            // Diminui prioridade com execução (para evitar monopolização)
            if (executionTime > 0 && executionTime % 5 == 0) {
                // CORREÇÃO: Usar variáveis da classe externa
                currentPriority = Math.min(PrioritySchedulerAging.this.maxPriority, 
                                          currentPriority + 1);
            }
        }
        
        @Override
        public String toString() {
            return String.format("Processo %s: Chegada=%d, Burst=%d, Prio=%d(%d), Restante=%d", 
                    id, arrivalTime, burstTime, originalPriority, currentPriority, remainingTime);
        }
    }
    
    /**
     * Slot de execução
     */
    public class ExecutionSlot {
        private int startTime;
        private int endTime;
        
        public ExecutionSlot(int startTime, int endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        public int getStartTime() { return startTime; }
        public int getEndTime() { return endTime; }
        public int getDuration() { return endTime - startTime; }
    }
    
    /**
     * Evento para timeline do GanttChart
     */
    public class ProcessEvent {
        private String processId;
        private int startTime;
        private int endTime;
        private String state;
        
        public ProcessEvent(String processId, int startTime, int endTime, String state) {
            this.processId = processId;
            this.startTime = startTime;
            this.endTime = endTime;
            this.state = state;
        }
        
        public String getProcessId() { return processId; }
        public int getStartTime() { return startTime; }
        public int getEndTime() { return endTime; }
        public String getState() { return state; }
    }
    
    /**
     * Construtor do escalonador
     */
    public PrioritySchedulerAging(int agingInterval, int maxPriority, int minPriority, boolean preemptive) {
        this.agingInterval = agingInterval;
        this.maxPriority = maxPriority;
        this.minPriority = minPriority;
        this.preemptive = preemptive;
        this.readyQueue = new ArrayList<>();
        this.finishedProcesses = new ArrayList<>();
        this.allProcesses = new ArrayList<>();
        this.timeline = new ArrayList<>();
        this.currentTime = 0;
        this.agingCounter = 0;
    }
    
    /**
     * Construtor com valores padrão
     */
    public PrioritySchedulerAging() {
        this(5, 10, 1, true); // Valores padrão
    }
    
    /**
     * Adiciona processos ao escalonador
     */
    public void addProcess(Process process) {
        allProcesses.add(process);
    }
    
    /**
     * Adiciona múltiplos processos
     */
    public void addProcesses(List<Process> processes) {
        allProcesses.addAll(processes);
    }
    
    /**
     * Cria um novo processo (factory method)
     */
    public Process createProcess(String id, int arrivalTime, int burstTime, int priority) {
        return new Process(id, arrivalTime, burstTime, priority);
    }
    
    /**
     * Executa o escalonamento
     */
    public void execute() {
        System.out.println("\n=== INICIANDO ESCALONAMENTO PRIOP COM ENVELHECIMENTO ===");
        System.out.println("Configuração: Aging=" + agingInterval + 
                         ", Prioridade[" + minPriority + "-" + maxPriority + 
                         "], Preemptivo=" + preemptive);
        
        // Ordena processos por tempo de chegada
        allProcesses.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        // Processos ainda não chegados
        List<Process> pendingProcesses = new ArrayList<>(allProcesses);
        
        while (!pendingProcesses.isEmpty() || !readyQueue.isEmpty() || currentProcess != null) {
            // 1. Verifica se há processos que chegaram no tempo atual
            checkArrivals(pendingProcesses);
            
            // 2. Aplica envelhecimento periodicamente
            applyAging();
            
            // 3. Escolhe próximo processo para executar
            Process nextProcess = selectNextProcess();
            
            // 4. Se houver mudança de processo (preempção)
            if (shouldPreempt(nextProcess)) {
                preemptCurrentProcess();
                currentProcess = nextProcess;
                startProcessExecution(currentProcess);
            }
            
            // 5. Se não há processo executando, inicia um
            if (currentProcess == null && nextProcess != null) {
                currentProcess = nextProcess;
                startProcessExecution(currentProcess);
            }
            
            // 6. Executa uma unidade de tempo
            executeTimeUnit();
            
            // 7. Atualiza estatísticas
            updateStatistics();
        }
        
        // Calcula métricas finais
        calculateMetrics();
        
        System.out.println("\n=== ESCALONAMENTO CONCLUÍDO ===");
        printStatistics();
    }
    
    private void checkArrivals(List<Process> pendingProcesses) {
        Iterator<Process> iterator = pendingProcesses.iterator();
        while (iterator.hasNext()) {
            Process p = iterator.next();
            if (p.getArrivalTime() <= currentTime) {
                readyQueue.add(p);
                iterator.remove();
                System.out.printf("Tempo %d: Processo %s chegou (Prio=%d)%n", 
                        currentTime, p.getId(), p.getCurrentPriority());
            }
        }
    }
    
    private void applyAging() {
        agingCounter++;
        if (agingCounter >= agingInterval) {
            for (Process p : readyQueue) {
                p.applyAging(currentTime);
            }
            agingCounter = 0;
            System.out.printf("Tempo %d: Aplicado envelhecimento%n", currentTime);
        }
    }
    
    private Process selectNextProcess() {
        if (readyQueue.isEmpty()) {
            return null;
        }
        
        // Ordena por prioridade (menor número = maior prioridade)
        readyQueue.sort((p1, p2) -> {
            int prioCompare = Integer.compare(p1.getCurrentPriority(), p2.getCurrentPriority());
            if (prioCompare == 0) {
                return Integer.compare(p1.getArrivalTime(), p2.getArrivalTime());
            }
            return prioCompare;
        });
        
        return readyQueue.get(0);
    }
    
    private boolean shouldPreempt(Process nextProcess) {
        if (!preemptive || currentProcess == null || nextProcess == null) {
            return false;
        }
        
        boolean shouldPreempt = nextProcess.getCurrentPriority() < currentProcess.getCurrentPriority();
        
        if (shouldPreempt) {
            System.out.printf("Tempo %d: PREEMPÇÃO! %s (prio=%d) -> %s (prio=%d)%n",
                    currentTime, currentProcess.getId(), currentProcess.getCurrentPriority(),
                    nextProcess.getId(), nextProcess.getCurrentPriority());
        }
        
        return shouldPreempt;
    }
    
    private void preemptCurrentProcess() {
        if (currentProcess != null && currentProcess.getRemainingTime() > 0) {
            readyQueue.add(currentProcess);
            System.out.printf("Tempo %d: Processo %s preemptado%n", 
                    currentTime, currentProcess.getId());
        }
    }
    
    private void startProcessExecution(Process process) {
        readyQueue.remove(process);
        System.out.printf("Tempo %d: Processo %s iniciou execução (Prio=%d, Restante=%d)%n",
                currentTime, process.getId(), process.getCurrentPriority(), process.getRemainingTime());
    }
    
    private void executeTimeUnit() {
        if (currentProcess != null) {
            currentProcess.execute(1);
            
            addToTimeline(currentProcess.getId(), currentTime, currentTime + 1, "running");
            
            currentProcess.applyPriorityDecay();
            
            if (currentProcess.getRemainingTime() <= 0) {
                finishCurrentProcess();
            }
        } else {
            addToTimeline("IDLE", currentTime, currentTime + 1, "idle");
            System.out.printf("Tempo %d: CPU ociosa%n", currentTime);
        }
        
        currentTime++;
        
        for (Process p : readyQueue) {
            p.wait(1);
        }
    }
    
    private void finishCurrentProcess() {
        currentProcess.finish(currentTime);
        currentProcess.addExecutionSlot(findLastStartTime(currentProcess.getId()), currentTime);
        finishedProcesses.add(currentProcess);
        
        System.out.printf("Tempo %d: Processo %s FINALIZADO (Turnaround=%d)%n",
                currentTime, currentProcess.getId(), currentProcess.getTurnaroundTime());
        
        currentProcess = null;
    }
    
    private void updateStatistics() {
        // Já atualizado durante execução
    }
    
    private void addToTimeline(String processId, int start, int end, String state) {
        timeline.add(new ProcessEvent(processId, start, end, state));
    }
    
    private int findLastStartTime(String processId) {
        for (int i = timeline.size() - 1; i >= 0; i--) {
            ProcessEvent event = timeline.get(i);
            if (event.getProcessId().equals(processId) && event.getState().equals("running")) {
                return event.getStartTime();
            }
        }
        return currentTime - 1;
    }
    
    private void calculateMetrics() {
        // Já calculadas durante execução
    }
    
    public void printStatistics() {
        System.out.println("\n=== ESTATÍSTICAS DA SIMULAÇÃO ===");
        System.out.println("Tempo total de simulação: " + currentTime);
        System.out.println("Número de processos: " + allProcesses.size());
        System.out.println("Número de preempções: " + countPreemptions());
        
        System.out.println("\n=== MÉTRICAS POR PROCESSO ===");
        System.out.println("ID | Chegada | Burst | Prio | PrioFinal | Turnaround | Waiting | Slots");
        System.out.println("---|---------|-------|------|-----------|------------|---------|-------");
        
        for (Process p : finishedProcesses) {
            System.out.printf("%2s | %7d | %5d | %4d | %9d | %10d | %7d | %d slot(s)%n",
                    p.getId(),
                    p.getArrivalTime(),
                    p.getBurstTime(),
                    p.getOriginalPriority(),
                    p.getCurrentPriority(),
                    p.getTurnaroundTime(),
                    p.getWaitingTime(),
                    p.getExecutionSlots().size());
        }
        
        double avgTurnaround = finishedProcesses.stream()
                .mapToInt(Process::getTurnaroundTime)
                .average()
                .orElse(0);
        
        double avgWaiting = finishedProcesses.stream()
                .mapToInt(Process::getWaitingTime)
                .average()
                .orElse(0);
        
        System.out.println("\n=== MÉTRICAS GERAIS ===");
        System.out.printf("Turnaround Time médio: %.2f%n", avgTurnaround);
        System.out.printf("Waiting Time médio: %.2f%n", avgWaiting);
        System.out.printf("Throughput: %.2f processos/unidade de tempo%n", 
                (double) finishedProcesses.size() / currentTime);
    }
    
    private int countPreemptions() {
        int count = 0;
        for (Process p : finishedProcesses) {
            if (p.getExecutionSlots().size() > 1) {
                count += p.getExecutionSlots().size() - 1;
            }
        }
        return count;
    }
    
    public List<ProcessEvent> getTimeline() {
        return new ArrayList<>(timeline);
    }
    
    public List<Process> getProcesses() {
        return new ArrayList<>(allProcesses);
    }
    
    public int getTotalTime() {
        return currentTime;
    }
    
    // Getters para configurações
    public int getAgingInterval() { return agingInterval; }
    public int getMaxPriority() { return maxPriority; }
    public int getMinPriority() { return minPriority; }
    public boolean isPreemptive() { return preemptive; }
}