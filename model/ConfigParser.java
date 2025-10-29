package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ConfigParser {

    public SimulationConfig parse(String path) throws IOException {
        SimulationConfig config = new SimulationConfig();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine();

            if (line == null || line.trim().isEmpty()) {
                throw new IOException("Configuration file is empty.");
            }

            // --- Parse header ---
            // Example:  "PRIOP;5"
            String[] headerParts = line.split(";");
            if (headerParts.length < 2) {
                throw new IOException("Invalid configuration header format.");
            }

            config.setAlgorithmName(headerParts[0].trim());
            config.setQuantum(Integer.parseInt(headerParts[1].trim()));

            // --- Parse processes ---
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Example: t01;0;5;2;IO:2-1;IO:3-2
                String[] parts = line.split(";");
                if (parts.length < 5) {
                    throw new IOException("Invalid process configuration: " + line);
                }

                Process p = new Process();
                p.setId(parts[0].trim());
                p.setColor(Integer.parseInt(parts[1].trim()));
                p.setStartTime(Integer.parseInt(parts[2].trim()));
                p.setDuration(Integer.parseInt(parts[3].trim()));
                p.setPriority(Integer.parseInt(parts[4].trim()));

                // Events will be parsed later (project B)
                config.getProcessList().add(p);
            }
        }

        return config;
    }
}
