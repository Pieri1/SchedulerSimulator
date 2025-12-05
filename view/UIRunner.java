package view;

import controller.SimController;
import model.SimulationConfig;
import model.SystemClock;
import javax.swing.*;
import java.awt.*;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UIRunner extends JFrame {
    private SimController controller;
    private SimulationConfig config;
    
    private JLabel timeLabel;
    private JLabel currentProcessLabel;
    private JTextArea logTextArea;
    private JButton stepButton;
    private JButton autoButton;
    private JButton stopButton;
    private JPanel ganttPanel;
    private JLabel ganttImageLabel;
    private Timer autoTimer;
    
    public UIRunner(SimulationConfig config) {
        this.config = config;
        // Usa um intervalo maior para a UI (2 segundos) para ser mais visível
        this.controller = new SimController(new SystemClock(2000), config);
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Execução da Simulação - " + config.getAlgorithmName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // Layout principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Painel de informações
        mainPanel.add(createInfoPanel(), BorderLayout.NORTH);
        
        // Painel do Gantt (simulado)
        ganttPanel = new JPanel();
        ganttPanel.setBackground(Color.WHITE);
        ganttPanel.setBorder(BorderFactory.createTitledBorder("Visualização do Gantt Chart"));
        ganttPanel.setPreferredSize(new Dimension(800, 200));
    ganttPanel.setLayout(new BorderLayout());
    // Label que exibirá a imagem PNG do Gantt
    ganttImageLabel = new JLabel();
    ganttImageLabel.setHorizontalAlignment(JLabel.CENTER);
    ganttPanel.add(ganttImageLabel, BorderLayout.CENTER);
    mainPanel.add(ganttPanel, BorderLayout.CENTER);
        
        // Área de logs
        logTextArea = new JTextArea(10, 60);
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Logs de Execução"));
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
        
        // Painel de controle
        mainPanel.add(createControlPanel(), BorderLayout.EAST);
        
        add(mainPanel);
        updateDisplay();
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        
        panel.add(new JLabel("Algoritmo:"));
        panel.add(new JLabel(config.getAlgorithmName()));
        
        panel.add(new JLabel("Tempo Atual:"));
        timeLabel = new JLabel("0");
        panel.add(timeLabel);
        
        panel.add(new JLabel("Processo em Execução:"));
        currentProcessLabel = new JLabel("Nenhum");
        panel.add(currentProcessLabel);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Controles"));
        
        stepButton = new JButton("Passo a Passo");
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeStep();
            }
        });
        
        autoButton = new JButton("Execução Automática");
        autoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleAutoExecution();
            }
        });
        
        stopButton = new JButton("Parar e Finalizar");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopSimulation();
            }
        });
        
        panel.add(stepButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(autoButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(stopButton);
        
        return panel;
    }
    
    private void executeStep() {
        // Para execução automática se estiver rodando
        if (autoTimer != null && autoTimer.isRunning()) {
            autoTimer.stop();
            autoButton.setText("Execução Automática");
        }
        
        // Executa um tick manualmente
        controller.getClock().tick();
        updateDisplay();
        updateGanttChart();
        
        // Verifica se todos os processos terminaram
        if (allProcessesCompleted()) {
            JOptionPane.showMessageDialog(this, "Todos os processos foram concluídos!");
            stopSimulation();
        }
    }
    
    private void toggleAutoExecution() {
        if (autoTimer == null || !autoTimer.isRunning()) {
            startAutoExecution();
        } else {
            stopAutoExecution();
        }
    }
    
    private void startAutoExecution() {
        autoTimer = new Timer(100, new ActionListener() { // 100ms entre atualizações
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!allProcessesCompleted()) {
                    // Para simulação automática, usamos tick() diretamente
                    // em vez de start() para ter mais controle
                    controller.getClock().tick();
                    updateDisplay();
                    updateGanttChart();
                } else {
                    stopAutoExecution();
                    JOptionPane.showMessageDialog(UIRunner.this, 
                        "Simulação concluída! Todos os processos finalizados.");
                    stopSimulation();
                }
            }
        });
        
        autoTimer.start();
        autoButton.setText("Pausar Execução");
        stepButton.setEnabled(false);
    }
    
    private void stopAutoExecution() {
        if (autoTimer != null) {
            autoTimer.stop();
        }
        autoButton.setText("Execução Automática");
        stepButton.setEnabled(true);
    }
    
    private void stopSimulation() {
        stopAutoExecution();
        controller.stop();
        
        // Mostra resultados
        UIResult results = new UIResult(controller);
        results.setVisible(true);
        
        // Fecha esta janela
        this.dispose();
    }
    
    private void updateDisplay() {
        int currentTime = controller.getCurrentTime();
        timeLabel.setText(String.valueOf(currentTime));
        
        // Atualiza logs - mostra informações básicas
        // Limpa o log e imprime todas as características de cada processo via reflexão
        logTextArea.setText("");
        logTextArea.append("Estado no tempo " + currentTime + ":\n\n");

        for (model.Process p : config.getProcessList()) {
            logTextArea.append("Processo:\n");
            // Usa reflexão para listar getters/isers disponíveis
            java.lang.reflect.Method[] methods = p.getClass().getMethods();
            for (java.lang.reflect.Method m : methods) {
            String name = m.getName();
            // Seleciona métodos que representem propriedades: getXxx() ou isXxx(), exclui getClass()
            if ((name.startsWith("get") && !name.equals("getClass")) || name.startsWith("is")) {
                if (m.getParameterTypes().length != 0) continue; // pula métodos com parâmetros
                String propName;
                if (name.startsWith("get")) {
                propName = name.substring(3);
                } else {
                propName = name.substring(2);
                }
                if (propName.length() == 0) continue;
                propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);

                Object value;
                try {
                value = m.invoke(p);
                } catch (Exception ex) {
                value = "<erro ao obter>";
                }
                logTextArea.append("  " + propName + ": " + String.valueOf(value));
            }
            }
            logTextArea.append("---------------------------\n");
        }
        
        // Mantém apenas os últimos 1000 caracteres no log para não ficar muito pesado
        if (logTextArea.getText().length() > 1000) {
            logTextArea.setText(logTextArea.getText().substring(logTextArea.getText().length() - 1000));
        }
        
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    }
    
    private void updateGanttChart() {
        // Gera PNG do Gantt e atualiza a JLabel com ImageIcon
        try {
            // Gera o PNG (inclui todos os processos configurados)
            controller.getGanttChart().generateChartPNG("simulation_gantt.png", config.getProcessList());

            ImageIcon icon = new ImageIcon("simulation_gantt.png");
            // Escala para caber no painel mantendo proporção simples
            int pw = ganttPanel.getWidth() > 0 ? ganttPanel.getWidth() : 800;
            int ph = ganttPanel.getHeight() > 0 ? ganttPanel.getHeight() : 200;
            Image img = icon.getImage();
            Image scaled = img.getScaledInstance(pw, ph, Image.SCALE_SMOOTH);
            ganttImageLabel.setIcon(new ImageIcon(scaled));
            ganttImageLabel.setText("");
        } catch (Exception ex) {
            ganttImageLabel.setIcon(null);
            ganttImageLabel.setText("Erro ao gerar/mostrar Gantt: " + ex.getMessage());
            ex.printStackTrace();
        }
        ganttPanel.revalidate();
        ganttPanel.repaint();
    }
    
    private boolean allProcessesCompleted() {
        // Verifica se todos os processos foram concluídos
        for (model.Process p : config.getProcessList()) {
            if (!p.isCompleted() && p.getStartTime() <= controller.getCurrentTime()) {
                return false;
            }
        }
        return true;
    }
}