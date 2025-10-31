package model;

import java.io.*;
import java.util.*;

/**
 * GanttChart - Gera gráficos de Gantt em SVG para visualização do escalonamento de processos
 */
public class GanttChart {
    private int width = 1200;
    private int height = 600;
    private int margin = 80;
    private Map<String, String> processColors;
    private String[] colorPalette = {
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
        "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9",
        "#F8C471", "#82E0AA", "#F1948A", "#85C1E9", "#D7BDE2",
        "#A3E4D7", "#F9E79F", "#D2B4DE", "#A9CCE3", "#FAD7A0"
    };

    // Registro de eventos para construção do Gantt
    private List<GanttEvent> events;
    
    // Controle de execução atual
    private String currentProcessId;
    private int currentStartTime;
    
    public GanttChart() {
        this.processColors = new HashMap<>();
        this.events = new ArrayList<>();
        this.currentProcessId = null;
        this.currentStartTime = 0;
    }

    public GanttChart(int width, int height) {
        this();
        this.width = width;
        this.height = height;
    }

    /**
     * Registra uma mudança de processo no tempo atual
     */
    public void recordProcessChange(String newProcessId, int currentTime) {
        // Se havia um processo executando anteriormente, registra seu término
        if (currentProcessId != null && !currentProcessId.equals(newProcessId)) {
            events.add(new GanttEvent(currentProcessId, currentStartTime, currentTime, "running"));
        }
        
        // Inicia o registro do novo processo
        if (newProcessId != null) {
            currentProcessId = newProcessId;
            currentStartTime = currentTime;
        } else {
            currentProcessId = null;
        }
    }

    /**
     * Finaliza o registro no tempo atual (para quando a simulação termina)
     */
    public void finalizeRecording(int currentTime) {
        if (currentProcessId != null) {
            events.add(new GanttEvent(currentProcessId, currentStartTime, currentTime, "running"));
            currentProcessId = null;
        }
    }

    /**
     * Adiciona um evento manualmente (para estados especiais)
     */
    public void addCustomEvent(String processId, int startTime, int endTime, String state) {
        events.add(new GanttEvent(processId, startTime, endTime, state));
    }

    /**
     * Gera o gráfico de Gantt em SVG
     */
    public void generateGanttChart(String filename) {
        if (events.isEmpty()) {
            System.out.println("Nenhum evento para gerar gráfico de Gantt");
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            generateSVG(writer);
            System.out.println("Gráfico de Gantt gerado: " + filename);
        } catch (IOException e) {
            System.err.println("Erro ao gerar gráfico: " + e.getMessage());
        }
    }

    private void generateSVG(PrintWriter writer) {
        // Calcula tempo total da simulação
        double totalTime = events.stream()
                .mapToDouble(GanttEvent::getEndTime)
                .max()
                .orElse(1.0);

        // Ordena eventos por tempo de início
        events.sort(Comparator.comparingDouble(GanttEvent::getStartTime));

        // Processos únicos (para eixo Y)
        Set<String> uniqueProcesses = new TreeSet<>();
        for (GanttEvent event : events) {
            uniqueProcesses.add(event.getProcessId());
        }

        // Cabeçalho SVG
        writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.printf("<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\">\n", width, height);
        
        // Fundo
        writer.printf("<rect width=\"100%%\" height=\"100%%\" fill=\"#f8f9fa\"/>\n");
        
        // Título
        writer.printf("<text x=\"%d\" y=\"%d\" text-anchor=\"middle\" font-size=\"20\" font-weight=\"bold\" fill=\"#2c3e50\">Gráfico de Gantt - Escalonamento de Processos</text>\n",
                width / 2, margin / 2);

        // Dimensões da área do gráfico
        int chartWidth = width - 2 * margin;
        int chartHeight = height - 2 * margin;
        int barHeight = 30;
        int barSpacing = 10;
        int rowHeight = barHeight + barSpacing;

        // Eixo Y - Labels dos processos
        Map<String, Integer> processYPositions = new HashMap<>();
        int yIndex = 0;
        for (String process : uniqueProcesses) {
            int y = margin + yIndex * rowHeight + barHeight / 2;
            processYPositions.put(process, y);
            
            writer.printf("<text x=\"%d\" y=\"%d\" text-anchor=\"end\" font-size=\"12\" font-weight=\"bold\" fill=\"#34495e\">%s</text>\n",
                    margin - 15, y + 4, process);
            yIndex++;
        }

        // Grade e Eixo X - Escala de tempo
        int numIntervals = 20;
        double timeInterval = Math.max(1, totalTime / numIntervals);
        
        for (int i = 0; i <= totalTime; i += timeInterval) {
            int x = margin + (int)((i / totalTime) * chartWidth);
            
            // Linha de grade vertical
            writer.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#bdc3c7\" stroke-width=\"1\" stroke-dasharray=\"2,2\"/>\n",
                    x, margin, x, margin + uniqueProcesses.size() * rowHeight);
            
            // Marcação de tempo
            writer.printf("<text x=\"%d\" y=\"%d\" text-anchor=\"middle\" font-size=\"10\" fill=\"#7f8c8d\">%d</text>\n",
                    x, margin - 20, i);
            
            // Linha de marcação no eixo
            writer.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#34495e\" stroke-width=\"1\"/>\n",
                    x, margin - 5, x, margin);
        }

        // Linha do eixo X principal
        writer.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#34495e\" stroke-width=\"2\"/>\n",
                margin, margin, margin + chartWidth, margin);

        // Linha do eixo Y
        writer.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#34495e\" stroke-width=\"2\"/>\n",
                margin, margin, margin, margin + uniqueProcesses.size() * rowHeight);

        // Barras do Gantt
        for (GanttEvent event : events) {
            String processId = event.getProcessId();
            double startTime = event.getStartTime();
            double endTime = event.getEndTime();
            String state = event.getState();

            int xStart = margin + (int)((startTime / totalTime) * chartWidth);
            int xEnd = margin + (int)((endTime / totalTime) * chartWidth);
            int barWidth = Math.max(3, xEnd - xStart);
            
            int y = processYPositions.get(processId) - barHeight / 2;
            String color = getProcessColor(processId, state);

            // Barra do processo
            writer.printf("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" stroke=\"%s\" stroke-width=\"1\" rx=\"4\"/>\n",
                    xStart, y, barWidth, barHeight, color, darkenColor(color));

            // Label do tempo (se houver espaço suficiente)
            if (barWidth > 50) {
                double duration = endTime - startTime;
                String durationText = String.format("%.1f", duration);
                writer.printf("<text x=\"%d\" y=\"%d\" text-anchor=\"middle\" font-size=\"9\" fill=\"white\" font-weight=\"bold\">%s</text>\n",
                        xStart + barWidth / 2, y + barHeight / 2 + 3, durationText);
            }

            // Tooltip simulada (mostra informações ao passar o mouse em visualizadores SVG)
            writer.printf("<title>Processo: %s | Início: %.1f | Fim: %.1f | Duração: %.1f | Estado: %s</title>\n",
                    processId, startTime, endTime, endTime - startTime, state);
        }

        // Legenda
        int legendY = margin + uniqueProcesses.size() * rowHeight + 50;
        addLegend(writer, legendY);

        // Informações do gráfico
        writer.printf("<text x=\"%d\" y=\"%d\" text-anchor=\"middle\" font-size=\"12\" fill=\"#7f8c8d\">Tempo total: %d | Processos: %d | Eventos: %d</text>\n",
                width / 2, height - 30, (int)totalTime, uniqueProcesses.size(), events.size());

        writer.println("</svg>");
    }

    private void addLegend(PrintWriter writer, int legendY) {
        String[][] legendItems = {
            {"Executando", "#4ECDC4"},
            {"Pronto", "#3498db"},
            {"Esperando", "#FFA500"},
            {"Bloqueado", "#e74c3c"},
            {"Terminado", "#2ecc71"}
        };

        writer.printf("<text x=\"%d\" y=\"%d\" font-size=\"14\" font-weight=\"bold\" fill=\"#2c3e50\">Legenda:</text>\n",
                margin, legendY - 10);

        for (int i = 0; i < legendItems.length; i++) {
            int x = margin + i * 150;
            writer.printf("<rect x=\"%d\" y=\"%d\" width=\"18\" height=\"18\" fill=\"%s\" stroke=\"%s\" stroke-width=\"1\" rx=\"3\"/>\n",
                    x, legendY, legendItems[i][1], darkenColor(legendItems[i][1]));
            writer.printf("<text x=\"%d\" y=\"%d\" font-size=\"12\" fill=\"#2c3e50\">%s</text>\n",
                    x + 25, legendY + 13, legendItems[i][0]);
        }
    }

    private String getProcessColor(String processId, String state) {
        // Cores baseadas no estado
        switch (state.toLowerCase()) {
            case "ready":
                return "#3498db";
            case "waiting":
                return "#FFA500";
            case "blocked":
                return "#e74c3c";
            case "terminated":
                return "#2ecc71";
            case "running":
            default:
                // Cor única por processo para estado running
                if (!processColors.containsKey(processId)) {
                    int colorIndex = processColors.size() % colorPalette.length;
                    processColors.put(processId, colorPalette[colorIndex]);
                }
                return processColors.get(processId);
        }
    }

    private String darkenColor(String color) {
        try {
            // Remove o # do início
            String hex = color.startsWith("#") ? color.substring(1) : color;
            
            // Converte para RGB
            int r, g, b;
            if (hex.length() == 3) {
                // Formato #RGB
                r = Integer.parseInt(hex.substring(0, 1) + hex.substring(0, 1), 16);
                g = Integer.parseInt(hex.substring(1, 2) + hex.substring(1, 2), 16);
                b = Integer.parseInt(hex.substring(2, 3) + hex.substring(2, 3), 16);
            } else if (hex.length() == 6) {
                // Formato #RRGGBB
                r = Integer.parseInt(hex.substring(0, 2), 16);
                g = Integer.parseInt(hex.substring(2, 4), 16);
                b = Integer.parseInt(hex.substring(4, 6), 16);
            } else {
                // Formato inválido, retorna cinza
                return "#666666";
            }
            
            // Escurece a cor (80% do valor original)
            r = (int) (r * 0.8);
            g = (int) (g * 0.8);
            b = (int) (b * 0.8);
            
            // Garante que está no range 0-255
            r = Math.max(0, Math.min(255, r));
            g = Math.max(0, Math.min(255, g));
            b = Math.max(0, Math.min(255, b));
            
            // Converte de volta para hex
            return String.format("#%02x%02x%02x", r, g, b);
            
        } catch (NumberFormatException e) {
            // Em caso de erro, retorna uma cor padrão escura
            return "#333333";
        }
    }

    /**
     * Limpa todos os eventos registrados
     */
    public void clearEvents() {
        events.clear();
        processColors.clear();
        currentProcessId = null;
        currentStartTime = 0;
    }

    /**
     * Retorna a lista de eventos registrados
     */
    public List<GanttEvent> getEvents() {
        return new ArrayList<>(events);
    }

    /**
     * Classe interna para representar eventos de processo no Gantt
     * (Renomeada para evitar conflito com model.Process)
     */
    public static class GanttEvent {
        private String processId;
        private double startTime;
        private double endTime;
        private String state;

        public GanttEvent(String processId, double startTime, double endTime) {
            this(processId, startTime, endTime, "running");
        }

        public GanttEvent(String processId, double startTime, double endTime, String state) {
            this.processId = processId;
            this.startTime = startTime;
            this.endTime = endTime;
            this.state = state;
        }

        // Getters
        public String getProcessId() { return processId; }
        public double getStartTime() { return startTime; }
        public double getEndTime() { return endTime; }
        public String getState() { return state; }

        @Override
        public String toString() {
            return String.format("GanttEvent[%s: %.1f-%.1f (%s)]", 
                processId, startTime, endTime, state);
        }
    }

    /**
     * Método de exemplo/teste
     */
    public static void main(String[] args) {
        GanttChart gantt = new GanttChart();
        
        // Dados de exemplo
        gantt.addCustomEvent("P1", 0, 5, "running");
        gantt.addCustomEvent("P2", 5, 8, "running");
        gantt.addCustomEvent("P1", 8, 12, "running");
        gantt.addCustomEvent("P3", 12, 15, "running");
        gantt.addCustomEvent("P2", 15, 18, "running");
        gantt.addCustomEvent("P4", 2, 4, "waiting");
        gantt.addCustomEvent("P3", 6, 7, "blocked");
        
        gantt.generateGanttChart("gantt_chart_example.svg");
        
        System.out.println("Eventos registrados:");
        for (GanttEvent event : gantt.getEvents()) {
            System.out.println(" - " + event);
        }
    }
}