package controller;

import model.*;
import java.util.Locale;

public class SimController {
    private final SystemClock clock;
    private final SimulationConfig config;
    private final Scheduler scheduler;

    private model.Process currentProcess;
    private int quantumCounter = 0;
    // accessed from the clock tick thread and from main — make volatile for visibility
    private volatile boolean finished = false;

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
        
        // Verifica se precisa trocar o processo atual
        if (currentProcess == null ||
            currentProcess.isCompleted() ||
            quantumCounter >= config.getQuantum()) {

            model.Process previous = currentProcess;

            // Solicita novo processo ao escalonador e reseta o contador de quantum
            currentProcess = scheduler.nextProcess(config.getProcessList(), time);
            quantumCounter = 0;

            // Log de troca de contexto
            if (previous != currentProcess) {
                System.out.printf("[t=%02d] Troca → %s%n",
                        time, currentProcess != null ? currentProcess.getId() : "CPU Idle");
            }
        }

        // Percorre todos os processos
        for (model.Process p : config.getProcessList()) {
            // Se o processo não iniciou ou já terminou, ignora
            if (p.getStartTime() > time || p.isCompleted()) continue;
            // Se for o processo atual, executa
            if (p == currentProcess) {
                p.executeTick();
            // Se não espera
            } else {
                p.waitTick();
            }
        }

        // Apenas log
        if (currentProcess != null) {
            quantumCounter++;
            System.out.printf("[t=%02d] Running %-4s (runtime=%d/%d, q=%d/%d)%n",
                time, currentProcess.getId(),
                currentProcess.getRunTime(), currentProcess.getDuration(),
                quantumCounter, config.getQuantum());
        } else {
            System.out.printf("[t=%02d] CPU Idle%n", time);
        }

        // Verificação de término da simulação
    boolean allDone = config.getProcessList().stream().allMatch(model.Process::isCompleted);
        if (allDone) {
            System.out.println("All processes finished at t=" + time);
            finished = true;
            stop();
        }
    }

    public void start() {
        // Inicia a simulação
        System.out.println("Simulação iniciando com o escalonador " + scheduler.getName() + "...");
        clock.start();
    }

    public void stop() {
        // Encerra a simulação
        clock.stop();
        System.out.println("Simulação encerrada em t=" + clock.getCurrentTime());
    }

    public void step() {
        // Executa um tick manualmente
        clock.tick();
    }

    public SystemClock getClock() {
        // Getter do clock
        return clock;
    }

    public int getCurrentTime() {
        // Getter do tempo atual (Diferente do clock)
        return clock.getCurrentTime();
    }

    /**
     * Indicates whether the simulation has finished. Safe to call from other threads.
     */
    public boolean isFinished() {
        return finished;
    }

    public static void main(String[] args) throws Exception {
        // Cria o parser e carrega configuração
        ConfigParser parser = new ConfigParser();
        SimulationConfig config = parser.parse("config/test.txt");

        // Inicia o controlador de simulação a depender do modo de execução
        if (config.getRunMode() == 0) {
            System.out.println("Rodando em modo automático.");
            SimController controller = new SimController(new SystemClock(500), config);
            controller.start();
            Thread.sleep(4000);
            controller.stop();
        } else {
            System.out.println("Rodando em modo passo a passo.");
            SimController controller = new SimController(new SystemClock(0), config);
            while (!controller.isFinished()) {
                System.out.print("Pressione Enter para avançar...");
                new java.util.Scanner(System.in).nextLine();
                controller.step();
                Thread.sleep(50); 
            }
            controller.stop();
        }
    }
}
