package model;

import java.util.*;

/**
 * Demonstração do Escalonador PRIOP com Envelhecimento
 * (Versão sem GanttChartSVG para evitar dependências)
 */
public class PrioritySchedulerDemo {
    public static void main(String[] args) {
        System.out.println("=== DEMONSTRAÇÃO ESCALONADOR PRIOP COM ENVELHECIMENTO ===\n");
        
        // Cria escalonador
        PrioritySchedulerAging scheduler = new PrioritySchedulerAging(5, 10, 1, true);
        
        // Cria processos usando o factory method do scheduler
        List<PrioritySchedulerAging.Process> processes = new ArrayList<>();
        processes.add(scheduler.createProcess("P1", 0, 8, 3));
        processes.add(scheduler.createProcess("P2", 1, 4, 1));  // Alta prioridade
        processes.add(scheduler.createProcess("P3", 2, 9, 4));
        processes.add(scheduler.createProcess("P4", 3, 5, 2));
        processes.add(scheduler.createProcess("P5", 4, 2, 5));  // Baixa prioridade
        
        // Adiciona processos
        scheduler.addProcesses(processes);
        
        // Executa escalonamento
        scheduler.execute();
        
        // Imprime timeline (para possível uso futuro com GanttChart)
        printTimeline(scheduler);
    }
    
    private static void printTimeline(PrioritySchedulerAging scheduler) {
        System.out.println("\n=== TIMELINE PARA GANTTCHART ===");
        System.out.println("Formato: Processo, Início, Fim, Estado");
        System.out.println("--------------------------------------");
        
        for (PrioritySchedulerAging.ProcessEvent event : scheduler.getTimeline()) {
            System.out.printf("%s: %d - %d (%s)%n",
                event.getProcessId(),
                event.getStartTime(),
                event.getEndTime(),
                event.getState());
        }
        
        System.out.println("\nPara gerar gráfico SVG, use:");
        System.out.println("1. Certifique-se que GanttChartSVG.java está compilado");
        System.out.println("2. Use a classe GanttChartGenerator.java (se disponível)");
    }
    
    /**
     * Método auxiliar para converter timeline para formato GanttChartSVG
     * (Usar apenas se GanttChartSVG estiver disponível)
     */
    /*
    private static void generateGanttChart(PrioritySchedulerAging scheduler) {
        // Código comentado - usar apenas se GanttChartSVG.java existir
        try {
            List<GanttChartSVG.ProcessEvent> ganttEvents = new ArrayList<>();
            
            for (PrioritySchedulerAging.ProcessEvent event : scheduler.getTimeline()) {
                String state = mapState(event.getState());
                ganttEvents.add(new GanttChartSVG.ProcessEvent(
                    event.getProcessId(),
                    event.getStartTime(),
                    event.getEndTime(),
                    state
                ));
            }
            
            GanttChartSVG gantt = new GanttChartSVG(1200, 600);
            gantt.generateGanttChart(ganttEvents, "priority_scheduling.svg");
            System.out.println("✓ Gráfico de Gantt gerado: priority_scheduling.svg");
            
        } catch (Exception e) {
            System.err.println("✗ GanttChartSVG não disponível: " + e.getMessage());
        }
    }
    
    private static String mapState(String state) {
        switch (state.toLowerCase()) {
            case "running": return "running";
            case "idle": return "waiting";
            default: return "running";
        }
    }
    */
}