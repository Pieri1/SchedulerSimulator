package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ConfigParser {

    public SimulationConfig parse(String path) throws IOException {
        // Le o documento de configuração e popula um objeto SimulationConfig.
        SimulationConfig config = new SimulationConfig();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine();

            if (line == null || line.trim().isEmpty()) {
                throw new IOException("Arquivo de configuração vazio.");
            }

            // Exemplo:  "PRIOP;5"
            String[] headerParts = line.split(";");
            if (headerParts.length < 2) {
                throw new IOException("Formato de configuração inválido.");
            }

            // Faz atribuição dos valores do cabeçalho
            config.setAlgorithmName(headerParts[0].trim());
            config.setQuantum(Integer.parseInt(headerParts[1].trim()));

            // Pega os processos (A partir da segunda linha)
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Exemplo: t01;0;0;4;2;
                String[] parts = line.split(";");
                if (parts.length < 5) {
                    throw new IOException("Configuração de processo inválida: " + line);
                }

                // Faz atribuição dos valores de cada processo
                Process p = new Process();
                p.setId(parts[0].trim());
                p.setColor(Integer.parseInt(parts[1].trim()));
                p.setStartTime(Integer.parseInt(parts[2].trim()));
                p.setDuration(Integer.parseInt(parts[3].trim()));
                p.setPriority(Integer.parseInt(parts[4].trim()));

                // Eventos por enquanto não são utilizados.
                config.getProcessList().add(p);
            }
        }

        return config;
    }
}
