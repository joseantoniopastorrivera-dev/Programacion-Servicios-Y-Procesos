package PSP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Proceso gestor (SumManager) que administra un pool de procesos SumWorker,
 * reparte carga en Round-Robin, recupera caídas en caliente y soporta ejecución
 * tanto interactiva como por lotes (batch).
 */
public class SumManager {

    private static final int DEFAULT_WORKERS = 2;
    private static final String LOG_FILE_NAME = "workers.log";
    private static final String BATCH_RESULTS_FILE = "resultados.txt";
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 2;

    /**
     * Clase interna que encapsula cada proceso trabajador y sus canales de
     * comunicación.
     * Evita tener múltiples arrays desincronizados (Process[], PrintWriter[],
     * BufferedReader[]).
     */
    private static class WorkerNode {
        final Process process;
        final long pid;
        final PrintWriter toWorker;
        final BufferedReader fromWorker;

        WorkerNode(Process process) {
            this.process = process;
            this.pid = process.pid();
            this.toWorker = new PrintWriter(
                    new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8), true);
            this.fromWorker = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        }

        void closeStreams() {
            try {
                toWorker.close();
                fromWorker.close();
            } catch (IOException ignored) {
            }
        }
    }

    // Estructura para almacenar los workers activos en cada ranura (slot)
    private final WorkerNode[] workers;

    // Historial de órdenes atendidas por cada PID (incluye los que hayan caído a
    // mitad de ejecución)
    private final Map<Long, Integer> ordersByPid = new LinkedHashMap<>();

    // Métricas globales para el resumen final
    private int errorResponsesCount = 0;
    private int crashesCount = 0;
    private int relaunchedWorkersCount = 0;
    private int currentTurnIndex = 0;

    public SumManager(int numWorkers) {
        this.workers = new WorkerNode[numWorkers];
    }

    public static void main(String[] args) {
        long startTimeMs = System.currentTimeMillis();

        // 1. Parseo del número de workers (por defecto 2 si no se indica)
        int numWorkers = DEFAULT_WORKERS;
        if (args.length >= 1) {
            try {
                numWorkers = Integer.parseInt(args[0]);
                if (numWorkers <= 0) {
                    System.err.println("El número de workers debe ser mayor que 0. Usando valor por defecto (2).");
                    numWorkers = DEFAULT_WORKERS;
                }
            } catch (NumberFormatException e) {
                System.err.println("Argumento de número de workers inválido. Usando valor por defecto (2).");
            }
        }

        // Determinar si estamos en Modo Lote (Parte 3) o Modo Interactivo (Parte 2)
        String batchFilePath = (args.length >= 2) ? args[1] : null;

        SumManager manager = new SumManager(numWorkers);

        try {
            // 2.1 Lanzar los workers iniciales
            manager.startAllWorkers();

            // 2.3 / Parte 3: Procesar órdenes según el modo
            if (batchFilePath != null) {
                manager.runBatchMode(batchFilePath);
            } else {
                manager.runInteractiveMode();
            }

        } catch (IOException e) {
            System.err.println("Error crítico en el gestor: " + e.getMessage());
        } finally {
            // 2.5 Cierre ordenado garantizado
            manager.shutdownAllWorkers();

            // 2.6 Resumen final de estadísticas
            long elapsedTimeMs = System.currentTimeMillis() - startTimeMs;
            manager.printFinalSummary(elapsedTimeMs);
        }
    }

    /**
     * Lanza todos los workers iniciales al arrancar el programa.
     */
    private void startAllWorkers() throws IOException {
        System.out.println("Iniciando pool de " + workers.length + " workers...");
        for (int i = 0; i < workers.length; i++) {
            workers[i] = spawnWorker();
            System.out.printf(" -> Worker [%d] lanzado con PID: %d%n", (i + 1), workers[i].pid);
        }
    }

    /**
     * Instancia un nuevo subproceso SumWorker configurando su ProcessBuilder y la
     * redirección a workers.log.
     */
    private WorkerNode spawnWorker() throws IOException {
        // Obtenemos la ruta del ejecutable java actual y el classpath en uso para que
        // sea 100% portable
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = SumWorker.class.getName(); // Devuelve "PSP.SumWorker"

        ProcessBuilder pb = new ProcessBuilder(javaBin, "-cp", classpath, className);

        // 2.2 Redirección de la salida de error en modo APPEND a workers.log
        File logFile = new File(LOG_FILE_NAME);
        pb.redirectError(ProcessBuilder.Redirect.appendTo(logFile));

        Process process = pb.start();
        WorkerNode node = new WorkerNode(process);

        // Registramos el nuevo PID en el mapa de métricas con 0 órdenes atendidas
        // inicialmente
        ordersByPid.putIfAbsent(node.pid, 0);

        return node;
    }

    /**
     * Modo Interactivo (Parte 2): Lee órdenes del usuario por consola hasta
     * escribir SALIR.
     */
    private void runInteractiveMode() throws IOException {
        System.out.println("\nModo interactivo activo. Escribe órdenes (o 'SALIR' para terminar):");
        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break; // EOF en teclado (Ctrl+D / Ctrl+Z)
                }
                String command = scanner.nextLine().trim();
                if (command.isEmpty()) {
                    continue;
                }
                if (command.equalsIgnoreCase("SALIR")) {
                    break;
                }

                // Reparto por turnos (Round-Robin)
                dispatchCommand(command, null);
            }
        }
    }

    /**
     * Modo Lote / Ampliación (Parte 3): Lee órdenes de un fichero y vuelca
     * resultados en resultados.txt.
     */
    private void runBatchMode(String inputFilePath) throws IOException {
        File inputFile = new File(inputFilePath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            throw new IOException("El fichero de órdenes no existe: " + inputFilePath);
        }

        System.out.println("\nEjecutando en modo lote leyendo de '" + inputFilePath + "'...");

        try (BufferedReader fileReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8));
                PrintWriter resultsWriter = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(BATCH_RESULTS_FILE), StandardCharsets.UTF_8)),
                        true)) {

            String line;
            while ((line = fileReader.readLine()) != null) {
                String command = line.trim();
                if (command.isEmpty()) {
                    continue;
                }
                if (command.equalsIgnoreCase("SALIR")) {
                    break;
                }

                dispatchCommand(command, resultsWriter);
            }
        }
        System.out.println("Procesamiento por lotes finalizado. Resultados guardados en '" + BATCH_RESULTS_FILE + "'.");
    }

    /**
     * Envía una orden al worker al que le corresponde el turno actual, gestiona
     * posibles caídas
     * y reenvía la orden al worker de reemplazo si no era un CRASH explícito.
     */
    private void dispatchCommand(String command, PrintWriter batchWriter) throws IOException {
        int slot = currentTurnIndex;
        // Avanzamos el turno en Round-Robin para la siguiente orden
        currentTurnIndex = (currentTurnIndex + 1) % workers.length;

        WorkerNode currentWorker = workers[slot];

        // 1. Enviamos la orden al worker
        currentWorker.toWorker.println(command);

        // 2. Intentamos leer su respuesta
        String response = currentWorker.fromWorker.readLine();

        // 2.4 Detección de caídas y recuperación
        if (response == null) {
            handleWorkerCrash(slot, command, batchWriter);
        } else {
            // El worker ha respondido correctamente
            processValidResponse(currentWorker.pid, command, response, batchWriter);
        }
    }

    /**
     * Gestiona la caída inesperada de un worker cuando readLine() devuelve null.
     */
    private void handleWorkerCrash(int slot, String failedCommand, PrintWriter batchWriter) throws IOException {
        WorkerNode deadWorker = workers[slot];
        crashesCount++;

        // Esperamos brevemente a que el SO actualice el estado de terminación para leer
        // su exitValue()
        int exitCode = -1;
        try {
            deadWorker.process.waitFor(1, TimeUnit.SECONDS);
            exitCode = deadWorker.process.exitValue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IllegalThreadStateException e) {
            deadWorker.process.destroyForcibly();
        } finally {
            deadWorker.closeStreams();
        }

        // Informar del PID caído y su código de salida
        System.out.printf("[ALERTA] El worker con PID %d ha caído inesperadamente. Código de salida: %d%n",
                deadWorker.pid, exitCode);

        // Lanzar un worker nuevo que ocupe su lugar en el mismo slot
        WorkerNode replacementWorker = spawnWorker();
        workers[slot] = replacementWorker;
        relaunchedWorkersCount++;

        System.out.printf("[RECUPERACIÓN] Lanzado nuevo worker de reemplazo en slot [%d] con PID: %d%n",
                (slot + 1), replacementWorker.pid);

        // Si la orden era CRASH (o EXIT directo), NO se reenvía al nuevo worker para
        // evitar un bucle infinito
        String baseOp = failedCommand.split("\\s+")[0].toUpperCase();
        if (baseOp.equals("CRASH")) {
            if (batchWriter != null) {
                batchWriter.printf("Orden: %-15s | [PID %d] -> CAÍDA (Código %d)%n",
                        failedCommand, deadWorker.pid, exitCode);
            }
            return;
        }

        // Cualquier otra orden cuya respuesta se haya perdido por una caída SÍ se
        // reenvía al nuevo worker
        System.out.printf("[REENVÍO] Reenviando orden '%s' al nuevo worker [PID %d]...%n",
                failedCommand, replacementWorker.pid);
        replacementWorker.toWorker.println(failedCommand);
        String retryResponse = replacementWorker.fromWorker.readLine();

        if (retryResponse != null) {
            processValidResponse(replacementWorker.pid, failedCommand, retryResponse, batchWriter);
        } else {
            System.err.printf("[ERROR CRÍTICO] El worker de reemplazo [PID %d] también falló al procesar '%s'.%n",
                    replacementWorker.pid, failedCommand);
        }
    }

    /**
     * Registra las métricas de una respuesta recibida y la muestra por pantalla (y
     * en el fichero lote si procede).
     */
    private void processValidResponse(long workerPid, String command, String response, PrintWriter batchWriter) {
        // Contabilizar orden atendida para este PID específico
        ordersByPid.put(workerPid, ordersByPid.getOrDefault(workerPid, 0) + 1);

        // Contabilizar si la respuesta fue ERROR
        if ("ERROR".equals(response.trim())) {
            errorResponsesCount++;
        }

        // Mostrar por consola indicando el PID
        System.out.printf("[PID %d] -> %s%n", workerPid, response);

        // Si estamos en modo lote (Parte 3), escribimos también en resultados.txt
        if (batchWriter != null) {
            batchWriter.printf("Orden: %-15s | [PID %d] -> %s%n", command, workerPid, response);
        }
    }

    /**
     * 2.5 Realiza el cierre ordenado de todos los workers activos al finalizar el
     * gestor.
     */
    private void shutdownAllWorkers() {
        System.out.println("\nIniciando cierre ordenado de workers...");
        for (int i = 0; i < workers.length; i++) {
            WorkerNode worker = workers[i];
            if (worker == null)
                continue;

            try {
                if (worker.process.isAlive()) {
                    // 1. Envía EXIT a cada worker y lee su respuesta BYE
                    worker.toWorker.println("EXIT");
                    String byeResponse = worker.fromWorker.readLine();

                    // 2. Espera a cada worker como máximo 2 segundos
                    boolean finishedInTime = worker.process.waitFor(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);

                    // 3. Si alguno no ha terminado en ese tiempo, lo termina con destroyForcibly()
                    if (!finishedInTime) {
                        System.out.printf(
                                " -> Worker [PID %d] no terminó en %d s. Forzando cierre (destroyForcibly)...%n",
                                worker.pid, SHUTDOWN_TIMEOUT_SECONDS);
                        worker.process.destroyForcibly().waitFor();
                    }

                    // 4. Muestra el código de salida de cada worker
                    int exitCode = worker.process.exitValue();
                    System.out.printf(" -> Worker [PID %d] cerrado (Respuesta: %s | Código de salida: %d)%n",
                            worker.pid, (byeResponse != null ? byeResponse : "N/A"), exitCode);
                } else {
                    System.out.printf(" -> Worker [PID %d] ya estaba detenido (Código de salida: %d)%n",
                            worker.pid, worker.process.exitValue());
                }
            } catch (IOException | InterruptedException e) {
                System.err.printf(" -> Error cerrando worker [PID %d]: %s%n", worker.pid, e.getMessage());
                worker.process.destroyForcibly();
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            } finally {
                worker.closeStreams();
            }
        }
    }

    /**
     * 2.6 Muestra el resumen final estadístico de toda la sesión.
     */
    private void printFinalSummary(long elapsedTimeMs) {
        System.out.println("\n==========================================");
        System.out.println("              RESUMEN FINAL               ");
        System.out.println("==========================================");
        System.out.println("Órdenes atendidas por cada worker:");
        for (Map.Entry<Long, Integer> entry : ordersByPid.entrySet()) {
            System.out.printf("  - [PID %d]: %d órdenes%n", entry.getKey(), entry.getValue());
        }
        System.out.println("------------------------------------------");
        System.out.printf("Número total de respuestas ERROR : %d%n", errorResponsesCount);
        System.out.printf("Número de caídas detectadas      : %d%n", crashesCount);
        System.out.printf("Número de workers relanzados     : %d%n", relaunchedWorkersCount);
        System.out.printf("Tiempo total de ejecución        : %d ms (%.2f s)%n",
                elapsedTimeMs, elapsedTimeMs / 1000.0);
        System.out.println("==========================================");
    }
}