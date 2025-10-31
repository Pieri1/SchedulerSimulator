package controller;

import model.*;
import java.util.Locale;

public class SimController {
    private final SystemClock clock;
    private final SimulationConfig config;
    private final Scheduler scheduler;

    private model.Process currentProcess;

    public SimController(SystemClock clock, SimulationConfig config) {
        this.clock = clock;
        this.config = config;

        // Algoritmo selecionado via config
        String algorithm = config.getAlgorithmName().toUpperCase(Locale.ROOT);
        switch (algorithm) {
            case "FIFO":
                scheduler = new FIFO();
                break;
            case "PRIOP":
                scheduler = new PRIOP();
                break;
            case "SRTF":
                scheduler = new SRTF();
                break;
            default:
                System.out.println("Escalonador Desconhecido: " + algorithm + " (Rodando em FIFO como padrão)");
                scheduler = new FIFO();
                break;
        }

        // Assina os ticks do clock
        clock.addListener(this::onTick);
    }

    private void onTick() {
        // Para cada tick, seleciona o proximo processo e executa um tick nele.
        int time = clock.getCurrentTime();
        currentProcess = scheduler.nextProcess(config.getProcessList(), time);

        if (currentProcess != null) {
            currentProcess.executeTick();
            System.out.printf("[t=%d] Running %s (runtime=%d/%d)%n",
                    time, currentProcess.getId(),
                    currentProcess.getRunTime(), currentProcess.getDuration());
        } else {
            System.out.printf("[t=%d] CPU Idle%n", time);
        }
    }

    public void start() {
        // Inicia a simulação
        System.out.println("Simulação iniciando com o escalonador " + scheduler.getName() + "...");
        clock.start(true);
    }

    public void stop() {
        // Encerra a simulação
        clock.stop();
        System.out.println("Simulação encerrada em t=" + clock.getCurrentTime());
    }

    public SystemClock getClock() {
        // Getter do clock
        return clock;
    }

    public int getCurrentTime() {
        // Getter do tempo atual (Diferente do clock)
        return clock.getCurrentTime();
    }

    public static void main(String[] args) throws Exception {
        // Cria o parser e carrega configuração
        ConfigParser parser = new ConfigParser();
        SimulationConfig config = parser.parse("config/test.txt");

        // Inicia o controlador de simulação
        SimController controller = new SimController(new SystemClock(500), config);
        controller.start();
        Thread.sleep(4000);
        controller.stop();
    }
}
