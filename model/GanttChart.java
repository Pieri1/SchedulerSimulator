package model;

import java.io.*;
import java.util.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

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
            // Try to merge with previous event for same process/state if contiguous
            GanttEvent last = findLastEvent(processId);
            if (last != null && "running".equals(last.state) && last.endTime == startTime) {
                last.endTime = endTime;
            } else {
                GanttEvent event = new GanttEvent(processId, startTime, endTime, "running");
                events.add(event);
            }
            System.out.println("Gantt Event recorded: " + processId + " " + startTime + "-" + endTime);
        } else {
            // ignore invalid
        }
    }

    /**
     * Registra tempo de espera de um processo (será mostrado em tom mais claro)
     */
    public void recordWait(String processId, int startTime, int endTime) {
        if (endTime > startTime && processId != null && !processId.equals("IDLE")) {
            GanttEvent last = findLastEvent(processId);
            if (last != null && "waiting".equals(last.state) && last.endTime == startTime) {
                last.endTime = endTime;
            } else {
                GanttEvent event = new GanttEvent(processId, startTime, endTime, "waiting");
                events.add(event);
            }
            System.out.println("Gantt Wait recorded: " + processId + " " + startTime + "-" + endTime);
        }
    }

    private GanttEvent findLastEvent(String processId) {
        for (int i = events.size() - 1; i >= 0; i--) {
            GanttEvent e = events.get(i);
            if (e.processId.equals(processId)) return e;
        }
        return null;
    }
    
    /**
     * Gera o gráfico SVG
     */
    public void generateChart(String filename) {
        generateChart(filename, null);
    }

    /**
     * Gera o gráfico SVG incluindo também processos da lista fornecida mesmo sem eventos.
     */
    public void generateChart(String filename, List<model.Process> processList) {
        System.out.println("Generating Gantt Chart with " + events.size() + " events");

        if (events.isEmpty() && (processList == null || processList.isEmpty())) {
            System.out.println("AVISO: Nenhum evento para gerar Gantt Chart");
            // Vamos criar alguns eventos de exemplo para debug
            createDebugEvents();
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            generateSVG(out, processList);
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
    
    private void generateSVG(PrintWriter out, List<model.Process> processList) {
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
        // Inclui processos da lista passada mesmo que não tenham eventos
        if (processList != null) {
            for (model.Process p : processList) {
                if (p != null) processes.add(String.valueOf(p.getId()));
            }
        }
        
        System.out.println("Processos no Gantt: " + processes);
        System.out.println("Tempo máximo: " + maxTime);
        
    // Configurações do gráfico (altura ajusta-se dinamicamente ao número de processos)
    int width = 1000;
    int margin = 80;
    int rowHeight = 30;
    int rowSpacing = 10;
    int contentHeight = processes.size() * (rowHeight + rowSpacing);
    int minHeight = 400; // mínima para boa aparência
    int height = Math.max(minHeight, margin * 2 + contentHeight + 100);
    int chartWidth = width - 2 * margin;
        
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

        // Atribui cores distintas de forma determinística por linha (evita cores repetidas)
        int totalProcesses = Math.max(1, processRows.size());
        for (Map.Entry<String, Integer> e : processRows.entrySet()) {
            int idx = e.getValue();
            float hue = idx / (float) totalProcesses; // espaçamento uniforme
            java.awt.Color c = java.awt.Color.getHSBColor(hue, 0.65f, 0.9f);
            String hex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
            processColors.put(e.getKey(), hex);
        }
        
        // Grade de tempo
        for (int t = 0; t <= maxTime; t++) {
            int x = margin + (t * chartWidth) / Math.max(1, maxTime);
            out.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#ccc\" stroke-width=\"1\"/>\n",
                      x, margin, x, margin + contentHeight);
            out.printf("<text x=\"%d\" y=\"%d\" text-anchor=\"middle\" font-size=\"10\">%d</text>\n",
                      x, margin - 10, t);
        }
        
        // Eixos
    out.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"black\" stroke-width=\"2\"/>\n",
          margin, margin, margin + chartWidth, margin);
    out.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"black\" stroke-width=\"2\"/>\n",
          margin, margin, margin, margin + contentHeight);
        
        // Barras do Gantt
        for (GanttEvent event : events) {
            String color = processColors.getOrDefault(event.processId, "#4ECDC4");
            
            int startX = margin + (int)((event.startTime * chartWidth) / Math.max(1, maxTime));
            int endX = margin + (int)((event.endTime * chartWidth) / Math.max(1, maxTime));
            int barWidth = Math.max(2, endX - startX);
            
            int y = margin + processRows.get(event.processId) * (rowHeight + rowSpacing);
            
            if ("running".equals(event.state)) {
                // Barra cheia para execução
                out.printf("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\" stroke=\"black\" stroke-width=\"1\"/>\n",
                          startX, y, barWidth, rowHeight, color);
            } else if ("waiting".equals(event.state)) {
                // Barra menor/mais clara para espera (centro da linha)
                int waitHeight = Math.max(4, rowHeight / 3);
                int waitY = y + (rowHeight - waitHeight) / 2;
                out.printf("<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"#f0f0f0\" stroke=\"#cccccc\" stroke-width=\"1\"/>\n",
                          startX, waitY, barWidth, waitHeight);
            } else {
                // estado desconhecido: desenha como linha fina
                int thinY = y + rowHeight/2;
                out.printf("<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"%s\" stroke-width=\"2\"/>\n",
                          startX, thinY, endX, thinY, color);
            }
            
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
    
    /**
     * Gera o gráfico em PNG (sem dependências externas) reproduzindo o mesmo layout do SVG
     */
    public void generateChartPNG(String filename) {
        generateChartPNG(filename, null);
    }

    public void generateChartPNG(String filename, List<model.Process> processList) {
        File outFile = new File(filename);
        try {
            generatePNG(outFile, processList);
            System.out.println("Gantt PNG gerado: " + filename);
        } catch (IOException e) {
            System.err.println("Erro ao gerar PNG: " + e.getMessage());
        }
    }

    private void generatePNG(File outFile, List<model.Process> processList) throws IOException {
        // Reuse the layout logic from generateSVG
        int width = 1000;
        int margin = 80;
        int rowHeight = 30;
        int rowSpacing = 10;

        int maxTime = events.stream()
                .mapToInt(e -> (int) e.endTime)
                .max()
                .orElse(10);

        // Coleta processos únicos
        java.util.Set<String> processes = new java.util.TreeSet<>();
        for (GanttEvent event : events) {
            processes.add(event.processId);
        }
        if (processList != null) {
            for (model.Process p : processList) {
                if (p != null) processes.add(String.valueOf(p.getId()));
            }
        }

        int contentHeight = processes.size() * (rowHeight + rowSpacing);
        int minHeight = 400;
        int height = Math.max(minHeight, margin * 2 + contentHeight + 100);
        int chartWidth = width - 2 * margin;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Fundo
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);

            // Título
            g.setColor(Color.BLACK);
            Font titleFont = new Font("SansSerif", Font.BOLD, 18);
            g.setFont(titleFont);
            FontMetrics fm = g.getFontMetrics();
            String title = "Gráfico de Gantt - Escalonamento";
            int titleW = fm.stringWidth(title);
            g.drawString(title, (width - titleW) / 2, 30);

            // Mapeia processos para linhas
            java.util.Map<String, Integer> processRows = new java.util.HashMap<>();
            int row = 0;
            Font labelFont = new Font("SansSerif", Font.PLAIN, 12);
            g.setFont(labelFont);
            fm = g.getFontMetrics();
            for (String process : processes) {
                processRows.put(process, row);
                int y = margin + row * (rowHeight + rowSpacing) + rowHeight / 2 + 4;
                int labelX = margin - 10 - fm.stringWidth(process);
                g.setColor(Color.BLACK);
                g.drawString(process, labelX, y);
                row++;
            }

            // Grade de tempo
            g.setColor(new Color(0xCC,0xCC,0xCC));
            for (int t = 0; t <= maxTime; t++) {
                int x = margin + (int) ((t * chartWidth) / Math.max(1, maxTime));
                g.drawLine(x, margin, x, margin + contentHeight);
                String ts = String.valueOf(t);
                int tx = x - g.getFontMetrics().stringWidth(ts)/2;
                g.drawString(ts, tx, margin - 10);
            }

            // Eixos
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2));
            g.drawLine(margin, margin, margin + chartWidth, margin);
            g.drawLine(margin, margin, margin, margin + contentHeight);

            // Barras do Gantt
            // Barras do Gantt
            for (GanttEvent event : events) {
                String hex = processColors.getOrDefault(event.processId, "#4ECDC4");
                Color procColor = Color.decode(hex);

                int startX = margin + (int) ((event.startTime * chartWidth) / Math.max(1, maxTime));
                int endX = margin + (int) ((event.endTime * chartWidth) / Math.max(1, maxTime));
                int barWidth = Math.max(2, endX - startX);
                int y = margin + processRows.get(event.processId) * (rowHeight + rowSpacing);

                if ("running".equals(event.state)) {
                    g.setColor(procColor);
                    g.fillRect(startX, y, barWidth, rowHeight);
                    g.setColor(Color.BLACK);
                    g.drawRect(startX, y, barWidth, rowHeight);
                } else if ("waiting".equals(event.state)) {
                    Color waitCol = Color.decode("#f0f0f0");
                    int waitHeight = Math.max(4, rowHeight / 3);
                    int waitY = y + (rowHeight - waitHeight) / 2;
                    g.setColor(waitCol);
                    g.fillRect(startX, waitY, barWidth, waitHeight);
                    g.setColor(new Color(0xCC,0xCC,0xCC));
                    g.drawRect(startX, waitY, barWidth, waitHeight);
                } else {
                    int thinY = y + rowHeight/2;
                    g.setColor(procColor);
                    g.setStroke(new BasicStroke(2));
                    g.drawLine(startX, thinY, endX, thinY);
                }

                // Label de duração
                if (barWidth > 25) {
                    String dur = String.format("%.1f", event.endTime - event.startTime);
                    FontMetrics fm2 = g.getFontMetrics();
                    int tw = fm2.stringWidth(dur);
                    g.setColor(Color.WHITE);
                    g.drawString(dur, startX + (barWidth - tw)/2, y + rowHeight/2 + 4);
                }
            }

            // Rodapé
            String footer = String.format("Tempo total: %d | Processos: %d | Eventos: %d", maxTime, processes.size(), events.size());
            Font footerFont = new Font("SansSerif", Font.PLAIN, 12);
            g.setFont(footerFont);
            int fw = g.getFontMetrics().stringWidth(footer);
            g.setColor(new Color(0x66,0x66,0x66));
            g.drawString(footer, (width - fw)/2, height - 20);

        } finally {
            g.dispose();
        }

        // Escreve PNG
        ImageIO.write(img, "png", outFile);
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