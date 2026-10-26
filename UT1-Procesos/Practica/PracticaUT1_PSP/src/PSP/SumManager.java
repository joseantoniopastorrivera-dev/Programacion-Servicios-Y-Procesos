package PSP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class SumManager {

    // Arrays para los procesos y sus tuberías de comunicación
    static Process[] workers;
    static PrintWriter[] entradas; // Para enviar órdenes al worker (su stdin)
    static BufferedReader[] salidas; // Para leer respuestas del worker (su stdout)

    // Mapa para guardar cuántas órdenes ha hecho cada PID (así no perdemos los que
    // mueren)
    static Map<Long, Integer> ordenesPorPid = new LinkedHashMap<>();

    // Contadores para el resumen final
    static int totalErrores = 0;
    static int totalCaidas = 0;
    static int totalRelanzados = 0;
    static int turno = 0;

    public static void main(String[] args) {
        long tiempoInicio = System.currentTimeMillis();

        // 2.1 Número de workers por args (si no se indica, 2 por defecto)
        int numWorkers = 2;
        if (args.length >= 1) {
            try {
                numWorkers = Integer.parseInt(args[0]);
                if (numWorkers <= 0) {
                    numWorkers = 2;
                }
            } catch (NumberFormatException e) {
                System.out.println("Número no válido, se usarán 2 workers por defecto.");
            }
        }

        workers = new Process[numWorkers];
        entradas = new PrintWriter[numWorkers];
        salidas = new BufferedReader[numWorkers];

        try {
            // Lanzamos los workers al arrancar y mostramos su PID
            System.out.println("Lanzando " + numWorkers + " workers...");
            for (int i = 0; i < numWorkers; i++) {
                lanzarWorker(i);
                System.out.println("Worker " + (i + 1) + " lanzado con PID: " + workers[i].pid());
            }

            // Miramos si hay segundo argumento para la Parte 3 (modo lote) o Parte 2
            // (teclado)
            if (args.length >= 2) {
                String ficheroOrdenes = args[1];
                ejecutarModoLote(ficheroOrdenes);
            } else {
                ejecutarModoTeclado();
            }

        } catch (IOException e) {
            System.out.println("Error de E/S en el gestor: " + e.getMessage());
        } finally {
            // 2.5 Cierre ordenado al terminar
            cerrarWorkers();

            // 2.6 Mostrar el resumen final
            long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
            mostrarResumen(tiempoTotal);
        }
    }

    // Método para crear un worker en la posición 'pos' del array
    private static void lanzarWorker(int pos) throws IOException {
        String rutaJava = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");

        ProcessBuilder pb = new ProcessBuilder(rutaJava, "-cp", classpath, "PSP.SumWorker");

        // 2.2 Redirigir la salida de error a registro_trabajadores.log añadiendo al
        // final (append)
        File log = new File("registro_trabajadores.log");
        pb.redirectError(ProcessBuilder.Redirect.appendTo(log));

        Process p = pb.start();
        workers[pos] = p;
        // autoFlush en true para que la orden se envíe al momento
        entradas[pos] = new PrintWriter(p.getOutputStream(), true);
        salidas[pos] = new BufferedReader(new InputStreamReader(p.getInputStream()));

        // Lo apuntamos en el mapa con 0 órdenes hechas de momento
        ordenesPorPid.putIfAbsent(p.pid(), 0);
    }

    // Parte 2.3: Pedir órdenes por teclado hasta que escriba SALIR
    private static void ejecutarModoTeclado() throws IOException {
        Scanner teclado = new Scanner(System.in);
        System.out.println("\nEscribe órdenes (escribe SALIR para terminar):");

        while (true) {
            System.out.print("> ");
            if (!teclado.hasNextLine()) {
                break;
            }
            String orden = teclado.nextLine().trim();

            if (orden.isEmpty()) {
                continue;
            }
            if (orden.equalsIgnoreCase("SALIR")) {
                break;
            }

            // Repartimos la orden (pasamos null porque no escribimos en resultados.txt)
            repartirOrden(orden, null);
        }

        teclado.close();
    }

    // Parte 3: Leer órdenes de un fichero y guardar en resultados.txt
    private static void ejecutarModoLote(String nombreFichero) throws IOException {
        System.out.println("\nEjecutando en modo lote con el fichero: " + nombreFichero);

        try (BufferedReader lectorFichero = new BufferedReader(new FileReader(nombreFichero));
                PrintWriter escritorResultados = new PrintWriter(new FileWriter("resultados.txt"), true)) {

            String linea;
            while ((linea = lectorFichero.readLine()) != null) {
                String orden = linea.trim();
                if (orden.isEmpty()) {
                    continue;
                }
                if (orden.equalsIgnoreCase("SALIR")) {
                    break;
                }

                repartirOrden(orden, escritorResultados);
            }
        }
        System.out.println("Modo lote terminado. Revisa el fichero resultados.txt.");
    }

    // Envía la orden al worker que le toca por turno y recoge su respuesta
    private static void repartirOrden(String orden, PrintWriter escritorResultados) throws IOException {
        int actual = turno;
        // Pasamos el turno al siguiente worker (si llega al final, vuelve al 0)
        turno = (turno + 1) % workers.length;

        long pidActual = workers[actual].pid();

        // Enviamos la orden y leemos la respuesta
        entradas[actual].println(orden);
        String respuesta = salidas[actual].readLine();

        // 2.4 Si readLine() devuelve null es que el worker se ha caído
        if (respuesta == null) {
            totalCaidas++;

            // Esperamos un momento a que el proceso termine del todo para sacar su código
            // de salida
            int codigoSalida = -1;
            try {
                workers[actual].waitFor(1, TimeUnit.SECONDS);
                codigoSalida = workers[actual].exitValue();
            } catch (Exception e) {
                // Ignoramos si hubiera algún fallo al leer el estado
            }

            System.out.println(
                    "¡CUIDADO! El worker con PID " + pidActual + " se ha caído. Código de salida: " + codigoSalida);

            // Lanzamos un worker nuevo en esa misma posición
            lanzarWorker(actual);
            totalRelanzados++;
            long nuevoPid = workers[actual].pid();
            System.out.println("Se ha lanzado un nuevo worker con PID " + nuevoPid + " en su lugar.");

            // Si la orden era CRASH, NO se reenvía al nuevo
            String primeraPalabra = orden.split("\\s+")[0].toUpperCase();
            if (!primeraPalabra.equals("CRASH")) {
                System.out.println("Reenviando la orden '" + orden + "' al nuevo worker [PID " + nuevoPid + "]...");
                entradas[actual].println(orden);
                String nuevaRespuesta = salidas[actual].readLine();

                if (nuevaRespuesta != null) {
                    registrarRespuesta(nuevoPid, orden, nuevaRespuesta, escritorResultados);
                }
            } else {
                // Si estábamos en modo lote y hubo CRASH, lo dejamos anotado en resultados.txt
                if (escritorResultados != null) {
                    escritorResultados
                            .println(orden + " | PID " + pidActual + " -> CAÍDO (Código " + codigoSalida + ")");
                }
            }
        } else {
            // Si ha contestado bien a la primera
            registrarRespuesta(pidActual, orden, respuesta, escritorResultados);
        }
    }

    // Suma los contadores y muestra la respuesta por pantalla (y en el fichero si
    // es modo lote)
    private static void registrarRespuesta(long pid, String orden, String respuesta, PrintWriter escritorResultados) {
        // Sumamos 1 a las órdenes atendidas por este PID
        ordenesPorPid.put(pid, ordenesPorPid.get(pid) + 1);

        if (respuesta.equals("ERROR")) {
            totalErrores++;
        }

        // Mostramos la respuesta indicando el PID del worker
        System.out.println("[PID " + pid + "] " + respuesta);

        // Si estamos en modo lote, lo guardamos en resultados.txt
        if (escritorResultados != null) {
            escritorResultados.println(orden + " | PID " + pid + " -> " + respuesta);
        }
    }

    // 2.5 Cierre ordenado de todos los workers
    private static void cerrarWorkers() {
        System.out.println("\nCerrando workers de forma ordenada...");
        for (int i = 0; i < workers.length; i++) {
            Process p = workers[i];
            if (p != null && p.isAlive()) {
                long pid = p.pid();
                try {
                    // 1. Enviamos EXIT y leemos BYE
                    entradas[i].println("EXIT");
                    String despedida = salidas[i].readLine();

                    // 2. Esperamos máximo 2 segundos
                    boolean terminoATiempo = p.waitFor(2, TimeUnit.SECONDS);

                    // 3. Si no ha terminado en 2 segundos, lo matamos con destroyForcibly()
                    if (!terminoATiempo) {
                        System.out.println("El worker [PID " + pid + "] tardó demasiado, forzando cierre...");
                        p.destroyForcibly().waitFor();
                    }

                    // 4. Mostramos su respuesta y código de salida
                    System.out.println("Worker [PID " + pid + "] respondió '" + despedida + "' y terminó con código: "
                            + p.exitValue());

                } catch (Exception e) {
                    p.destroyForcibly();
                }
            }
        }
    }

    // 2.6 Resumen final
    private static void mostrarResumen(long tiempoMs) {
        System.out.println("\n--- RESUMEN FINAL ---");
        System.out.println("Órdenes atendidas por cada worker:");
        for (Map.Entry<Long, Integer> entrada : ordenesPorPid.entrySet()) {
            System.out.println("  PID " + entrada.getKey() + ": " + entrada.getValue() + " órdenes");
        }
        System.out.println("Número de respuestas ERROR: " + totalErrores);
        System.out.println("Número de caídas: " + totalCaidas);
        System.out.println("Número de workers relanzados: " + totalRelanzados);
        System.out.println("Tiempo total de ejecución: " + tiempoMs + " ms (" + (tiempoMs / 1000.0) + " segundos)");
    }
}