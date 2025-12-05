package view;

import model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UIConfigurator extends JFrame {
    private JComboBox<String> algorithmCombo;
    private JTextField quantumField;
    private JTextField filePathField;
    private JButton loadFileButton;
    private JButton startButton;
    private JTextArea configTextArea;
    private SimulationConfig currentConfig;
    
    public UIConfigurator() {
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Configurador de Simulação - Escalonamento de Processos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        
        // Layout principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Painel de configurações
        JPanel configPanel = createConfigPanel();
        mainPanel.add(configPanel, BorderLayout.NORTH);
        
        // Área de texto para visualização do config
        configTextArea = new JTextArea(15, 50);
        configTextArea.setEditable(false);
        configTextArea.setText("Configuração carregada aparecerá aqui...\n\nClique em 'Carregar' para carregar um arquivo de configuração.");
        JScrollPane scrollPane = new JScrollPane(configTextArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Painel de botões
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        // Algoritmo
        panel.add(new JLabel("Algoritmo de Escalonamento:"));
        algorithmCombo = new JComboBox<>(new String[]{"FIFO", "SRTF", "PRIOP"});
        panel.add(algorithmCombo);
        
        // Quantum
        panel.add(new JLabel("Quantum:"));
        quantumField = new JTextField("5");
        panel.add(quantumField);
        
        // Arquivo de configuração
        panel.add(new JLabel("Arquivo de Configuração:"));
        JPanel filePanel = new JPanel(new BorderLayout());
        filePathField = new JTextField("config/test1.txt");
        JButton browseButton = new JButton("Procurar...");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Selecione o arquivo de configuração");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int ret = chooser.showOpenDialog(UIConfigurator.this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                java.io.File f = chooser.getSelectedFile();
                filePathField.setText(f.getAbsolutePath());
            }
            }
        });
        filePanel.add(browseButton, BorderLayout.WEST);
        loadFileButton = new JButton("Carregar");
        
        loadFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadConfigFile();
            }
        });
        
        filePanel.add(filePathField, BorderLayout.CENTER);
        filePanel.add(loadFileButton, BorderLayout.EAST);
        panel.add(filePanel);
        
        // Espaço vazio para alinhamento
        panel.add(new JLabel());
        panel.add(new JLabel());
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        
        startButton = new JButton("Iniciar Simulação");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSimulation();
            }
        });
        
        JButton exitButton = new JButton("Sair");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        panel.add(startButton);
        panel.add(exitButton);
        
        return panel;
    }
    
    private void loadConfigFile() {
        String filePath = filePathField.getText().trim();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, informe o caminho do arquivo de configuração.");
            return;
        }
        
        try {
            ConfigParser parser = new ConfigParser();
            currentConfig = parser.parse(filePath);
            
            // Atualiza UI com os valores do arquivo
            algorithmCombo.setSelectedItem(currentConfig.getAlgorithmName());
            quantumField.setText(String.valueOf(currentConfig.getQuantum()));
            
            // Mostra informações no text area
            displayConfigInfo();
            
            JOptionPane.showMessageDialog(this, "Configuração carregada com sucesso!");
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Erro ao carregar arquivo: " + ex.getMessage(), 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void displayConfigInfo() {
        if (currentConfig == null) return;
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== CONFIGURAÇÃO DA SIMULAÇÃO ===\n\n");
        sb.append("Algoritmo: ").append(currentConfig.getAlgorithmName()).append("\n");
        sb.append("Quantum: ").append(currentConfig.getQuantum()).append("\n\n");
        sb.append("=== PROCESSOS ===\n");
        
        if (currentConfig.getProcessList().isEmpty()) {
            sb.append("Nenhum processo carregado.\n");
        } else {
            for (model.Process p : currentConfig.getProcessList()) {
                try {
                    // Use %s for all fields to avoid IllegalFormatConversionException when types vary
                    sb.append(String.format("ID: %s | Cor: %s | Ingresso: %s | Duração: %s | Prioridade: %s\n",
                            String.valueOf(p.getId()), String.valueOf(p.getColor()), String.valueOf(p.getStartTime()),
                            String.valueOf(p.getDuration()), String.valueOf(p.getPriority())));
                } catch (Exception ex) {
                    // Fallback: safe concatenation
                    sb.append("ID: " + p.getId() + " | Cor: " + p.getColor() + " | Ingresso: " + p.getStartTime()
                            + " | Duração: " + p.getDuration() + " | Prioridade: " + p.getPriority() + "\n");
                }
            }
        }
        
        configTextArea.setText(sb.toString());
    }
    
    private void startSimulation() {
        if (currentConfig == null) {
            int result = JOptionPane.showConfirmDialog(this, 
                "Nenhum arquivo de configuração foi carregado. Deseja usar configuração padrão?",
                "Configuração Não Carregada",
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                // Cria uma configuração padrão
                currentConfig = createDefaultConfig();
            } else {
                return;
            }
        }
        
        // Atualiza configuração com valores da UI
        currentConfig.setAlgorithmName((String) algorithmCombo.getSelectedItem());
        try {
            currentConfig.setQuantum(Integer.parseInt(quantumField.getText()));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Quantum deve ser um número válido.");
            return;
        }
        
        // Inicia o UIRunner
        UIRunner runner = new UIRunner(currentConfig);
        runner.setVisible(true);
        
        // Opcional: fecha o configurador ou minimiza
        this.setVisible(false);
    }
    
    private SimulationConfig createDefaultConfig() {
        SimulationConfig config = new SimulationConfig();
        config.setAlgorithmName("FIFO");
        config.setQuantum(5);
        
        // Adiciona alguns processos de exemplo
        try {
            ConfigParser parser = new ConfigParser();
            return parser.parse("config/test.txt");
        } catch (Exception e) {
            // Se não conseguir carregar do arquivo, cria processos manualmente
            model.Process p1 = new model.Process();
            p1.setId("P1");
            p1.setColor(1);
            p1.setStartTime(0);
            p1.setDuration(5);
            p1.setPriority(1);
            
            model.Process p2 = new model.Process();
            p2.setId("P2");
            p2.setColor(2);
            p2.setStartTime(2);
            p2.setDuration(3);
            p2.setPriority(2);
            
            config.getProcessList().add(p1);
            config.getProcessList().add(p2);
        }
        
        return config;
    }
    
    public static void main(String[] args) {
        // Ponto de entrada alternativo para a UI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new UIConfigurator().setVisible(true);
            }
        });
    }
}