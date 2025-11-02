package view;

import controller.SimController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;

public class UIResult extends JFrame {
    private SimController controller;
    
    private JTextArea resultTextArea;
    private JButton showGanttButton;
    private JButton exportButton;
    private JButton newSimulationButton;
    
    public UIResult(SimController controller) {
        this.controller = controller;
        initializeUI();
        displayResults();
    }
    
    private void initializeUI() {
        setTitle("Resultados da Simulação");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);
        
        // Layout principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Área de resultados
        resultTextArea = new JTextArea(20, 50);
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Resumo da Simulação"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Painel de botões
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        showGanttButton = new JButton("Mostrar Gantt Chart");
        showGanttButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showGanttChart();
            }
        });
        
        exportButton = new JButton("Exportar Relatório");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportReport();
            }
        });
        
        newSimulationButton = new JButton("Nova Simulação");
        newSimulationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newSimulation();
            }
        });
        
        JButton closeButton = new JButton("Fechar");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        panel.add(showGanttButton);
        panel.add(exportButton);
        panel.add(newSimulationButton);
        panel.add(closeButton);
        
        return panel;
    }
    
    private void displayResults() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== RESULTADOS DA SIMULAÇÃO ===\n\n");
        sb.append("Tempo Final: ").append(controller.getCurrentTime()).append("\n\n");
        
        sb.append("=== ESTATÍSTICAS DOS PROCESSOS ===\n");
        
        // Estatísticas básicas dos processos
        int totalProcesses = 0;
        int completedProcesses = 0;
        int totalExecutionTime = 0;
        int totalWaitTime = 0;
        
        // Em uma implementação real, você obteria essas informações do controller
        // Por enquanto, vamos usar dados de exemplo
        for (int i = 1; i <= 3; i++) {
            int execTime = i * 2;
            int waitTime = i;
            boolean completed = i < 3;
            
            sb.append(String.format("Processo P%d: Execução=%d, Espera=%d, Concluído=%s\n",
                    i, execTime, waitTime, completed ? "Sim" : "Não"));
            
            totalProcesses++;
            if (completed) completedProcesses++;
            totalExecutionTime += execTime;
            totalWaitTime += waitTime;
        }
        
        sb.append("\n=== ESTATÍSTICAS GERAIS ===\n");
        sb.append(String.format("Processos Totais: %d\n", totalProcesses));
        sb.append(String.format("Processos Concluídos: %d\n", completedProcesses));
        sb.append(String.format("Taxa de Conclusão: %.1f%%\n", (completedProcesses * 100.0 / totalProcesses)));
        sb.append(String.format("Tempo Total de Execução: %d\n", totalExecutionTime));
        sb.append(String.format("Tempo Total de Espera: %d\n", totalWaitTime));
        
        if (controller.getCurrentTime() > 0) {
            sb.append(String.format("Throughput: %.2f processos/unidade tempo\n", 
                    totalProcesses / (double) controller.getCurrentTime()));
        } else {
            sb.append("Throughput: N/A\n");
        }
        
        resultTextArea.setText(sb.toString());
    }
    
    private void showGanttChart() {
        try {
            // Abre o arquivo SVG do Gantt Chart no visualizador padrão
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                File file = new File("simulation_gantt.svg");
                if (file.exists()) {
                    desktop.open(file);
                    JOptionPane.showMessageDialog(this, 
                        "Gantt Chart aberto no visualizador padrão!");
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Arquivo do Gantt Chart não encontrado: simulation_gantt.svg\n" +
                        "O gráfico será gerado quando a simulação terminar.");
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Não foi possível abrir o visualizador de arquivos.\n" +
                    "O Gantt Chart foi salvo como: simulation_gantt.svg");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao abrir Gantt Chart: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void exportReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exportar Relatório");
        fileChooser.setSelectedFile(new File("relatorio_simulacao.txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                PrintWriter writer = new PrintWriter(file);
                writer.write(resultTextArea.getText());
                writer.close();
                
                JOptionPane.showMessageDialog(this, 
                    "Relatório exportado com sucesso para: " + file.getAbsolutePath());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao exportar relatório: " + ex.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void newSimulation() {
        // Volta para o configurador
        UIConfigurator configurator = new UIConfigurator();
        configurator.setVisible(true);
        this.dispose();
    }
}