package model;

import java.io.*;
import java.util.*;

/**
 * GanttChart - Gera gráficos de Gantt em SVG para visualização do escalonamento de processos
 */
public class GanttChart {
    private List<GanttEvent> events;
    private Map<String, String> processColors;
    
    public GanttChart() {
        this.events = new ArrayList<>();
        this.processColors = new HashMap<>();
    }
    
    /**
     * Registra a execução de um processo
     */
    public void recordExecution(String processId, int startTime, int endTime) {
        if (endTime > startTime && processId != null && !processId.equals("IDLE")) {
            GanttEvent event = new GanttEvent(processId, startTime, endTime, "running");
            events.add(event);
            System.out.println("Gantt Event recorded: " + event);
        } else {
            System.out.println("Gantt Event skipped - invalid: " + processId + " from " + startTime + " to " + endTime);
        }
    }
    
    /**
     * Gera o gráfico SVG
     */
    public void generateChart(String filename) {
        System.out.println("Generating Gantt Chart with " + events.size() + " events");
        
        if (events.isEmpty()) {
            System.out.println("AVISO: Nenhum evento para gerar Gantt Chart");
            // Vamos criar alguns eventos de exemplo para debug
            createDebugEvents();
        }
        
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            generateSVG(out);
            System.out.println("Gantt Chart gerado: " + filename);
        } catch (IOException e) {
            System.err.println("Erro ao gerar gráfico: " + e.getMessage());
        }
    }
    
    /**
     * Cria eventos de debug para testar o gráfico
     */
    private void createDebugEvents() {
        System.out.println("Criando eventos de debug...");
        recordExecution("P1", 0, 3);
        recordExecution("P2", 3, 6);
        recordExecution("P1", 6, 8);
        recordExecution("P3", 8, 10);
    }
    
    private void generateSVG(PrintWriter out) {
        // Encontra o tempo máximo
        int maxTime = events.stream()
                .mapToInt(e -> (int) e.endTime)
                .max()
                .orElse(10);
        
        // Coleta processos únicos
        Set<String> processes = new TreeSet<>();
        for (GanttEvent event : events) {
            processes.add(event.processId);
        }
        
        System.out.println("Processos no Gantt: " + processes);
        System.out.println("Tempo máximo: " + maxTime);
        
        // Configurações do gráfico
        int width = 1000;
        int height = 400;
        int margin = 80;
        int chartWidth = width - 2 * margin;
        int rowHeight = 30;
        int rowSpacing = 10;
        
        // Cabeçalho SVG
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.printf("<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\">\n", width, height);
        
        // Fundo
        out.println("<rect width=\"100%\" height=\"100%\" fill=\"white\"/>");
        
        // Título
        out.printf("<text x=\"%d\" y=\"%d\" text-anchor=\"middle\" font-size=\"18\" font-weight=\"bold\">Gráfico de Gantt - Escalonamento</text>\n", 
                  width/2, 30);
        
        // Mapeia processos para linhas
        Map<String, Integer> processRows = new HashMap<>();
        int row = 0;
        for (String process : processes) {
            processRows.put(process, row);
            // Label do processo
            int y = margin + row * (rowHeight + rowSpacing) + rowHeight / 2;
            out.printf("<text x=\"%d\" y=\"%d\" text-anchor=\"end\" font-size=\"12\">%s</text>\n", 
                      margin - 10, y + 4, process);
            row++;
        }
        
        // Grade de tempo
        for (int t = 0; t <= maxTime; t++) {
            int x = margin + (t * chartWidth) / Math.max(1, maxTime);
            out.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#ccc\" stroke-width=\"1\"/>\n", 
                      x, margin, x, margin + processes.size() * (rowHeight + rowSpacing));
            out.printf("<text x=\"%d\" y=\"%d\" text-anchor=\"middle\" font-size=\"10\">%d</text>\n", 
                      x, margin - 10, t);
        }
        
        // Eixos
        out.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"black\" stroke-width=\"2\"/>\n", 
                  margin, margin, margin + chartWidth, margin);
        out.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"black\" stroke-width=\"2\"/>\n", 
                  margin, margin, margin, margin + processes.size() * (rowHeight + rowSpacing));
        
        // Barras do Gantt - versão simplificada sem lambda problemático
        String[] colors = {"#4ECDC4", "#FF6B6B", "#45B7D1", "#96CEB4", "#FFEAA7", "#DDA0DD"};
        int colorCounter = 0;

        for (GanttEvent event : events) {
            // Atribui cores de forma simples
            if (!processColors.containsKey(event.processId)) {
                processColors.put(event.processId, colors[colorCounter % colors.length]);
                colorCounter++;
            }
            String color = processColors.get(event.processId);
            
            int startX = margin + (int)((event.startTime * chartWidth) / Math.max(1, maxTime));
            int endX = margin + (int)((event.endTime * chartWidth) / Math.max(1, maxTime));
            int barWidth = Math.max(2, endX - startX);
            
            int y = margin + processRows.get(event.processId) * (rowHeight + rowSpacing);
            
            // Barra
            out.printf("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" stroke=\"black\" stroke-width=\"1\"/>\n",
                      startX, y, barWidth, rowHeight, color);
            
            // Label de duração (se couber)
            if (barWidth > 25) {
                out.printf("<text x=\"%d\" y=\"%d\" text-anchor=\"middle\" font-size=\"10\" fill=\"white\">%.1f</text>\n",
                          startX + barWidth/2, y + rowHeight/2 + 3, event.endTime - event.startTime);
            }
            
            // Tooltip
            out.printf("<title>%s: %.1f-%.1f (duraçao: %.1f)</title>\n",
                      event.processId, event.startTime, event.endTime, event.endTime - event.startTime);
        }
        
        // Rodapé
        out.printf("<text x=\"%d\" y=\"%d\" text-anchor=\"middle\" font-size=\"12\" fill=\"#666\">Tempo total: %d | Processos: %d | Eventos: %d</text>\n",
                  width/2, height - 20, maxTime, processes.size(), events.size());
        
        out.println("</svg>");
    }
    
    public void clear() {
        events.clear();
        processColors.clear();
    }
    
    public List<GanttEvent> getEvents() {
        return new ArrayList<>(events);
    }
    
    // Classe interna para eventos
    public static class GanttEvent {
        public String processId;
        public double startTime;
        public double endTime;
        public String state;
        
        public GanttEvent(String processId, double startTime, double endTime, String state) {
            this.processId = processId;
            this.startTime = startTime;
            this.endTime = endTime;
            this.state = state;
        }
        
        @Override
        public String toString() {
            return String.format("%s: %.1f-%.1f (dur: %.1f)", 
                               processId, startTime, endTime, endTime - startTime);
        }
    }
}