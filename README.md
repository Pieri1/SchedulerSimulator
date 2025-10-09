# 🧩 Simulador de Escalonamento de Processos

Projeto desenvolvido como parte da disciplina de Sistemas Operacionais, com o objetivo de **simular o funcionamento de um sistema operacional multitarefa preemptivo de tempo compartilhado**, incluindo diferentes algoritmos de escalonamento e visualização gráfica da execução das tarefas.

---

## 🎯 Objetivos

- Simular a execução de múltiplas tarefas concorrentes.  
- Implementar e comparar diferentes algoritmos de escalonamento.  
- Permitir execução **passo-a-passo** e **automática**.  
- Gerar visualização no formato de **gráfico de Gantt**.  
- Carregar parâmetros de simulação a partir de um **arquivo de configuração (.txt)**.

---

## ⚙️ Estrutura do Projeto (MVC)

src/
├── model/
│ ├── Process.java
│ ├── Event.java
│ ├── Scheduler.java
│ ├── FIFO.java
│ ├── SRTF.java
│ ├── PRIOP.java
│ ├── SystemClock.java
│ ├── SimulationConfig.java
│ ├── ConfigParser.java
│ ├── GanttChart.java
│ └── GanttGenerator.java
│
├── control/
│ └── SimController.java
│
└── view/
├── UIConfigurator.java
├── UIRunner.java
├── UIResult.java
└── GanttChartView.java

---

## 📄 Formato do Arquivo de Configuração

O simulador lê um arquivo `.txt` no seguinte formato:

algoritmo_escalonamento;quantum
id;cor;ingresso;duracao;prioridade;lista_eventos

### Exemplo:
PRIOP;5
t01;0;5;2;IO:2-1;IO:3-2
t02;0;4;3;IO:3-1
t03;3;5;5;

---

## 🚀 Execução

1. Configure o arquivo de simulação (`config.txt`);
2. Compile o projeto:
   ```bash
   javac src/**/*.java
   ```
3. Execute o simulador:
   ```bash
   javac src.control.SimController
   ```

🧮 Algoritmos Implementados
FIFO – First In, First Out
SRTF – Shortest Remaining Time First
PRIOP – Prioridade Preemptivo

📊 Saída
Ao final da simulação, o programa gera:

Um gráfico de Gantt mostrando a execução das tarefas;

Um relatório com o tempo total, tempo de espera e de execução de cada processo.

👨‍💻 Autores
João Pedro de Pieri Batista da Silva
André Henrique Caseiro Almeida

🏫 Instituição
Universidade Tecnológica Federal do Paraná (UTFPR)
Disciplina: Sistemas Operacionais
Professor: Dr. Marco Aurélio Wehrmeister

---