package PSP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * Proceso trabajador (SumWorker) que lee comandos matemáticos y de control
 * desde la entrada estándar (stdin) y emite resultados por la salida estándar
 * (stdout).
 * Los errores detallados se reportan por la salida de error estándar (stderr).
 */
public class SumWorker {

    // Código de salida requerido cuando se simula un fallo abrupto
    private static final int EXIT_CODE_CRASH = 3;
    private static final int EXIT_CODE_OK = 0;

    public static void main(String[] args) {
        // Obtenemos el PID real asignado por el Sistema Operativo a esta instancia de
        // la JVM (Java 9+)
        long myPid = ProcessHandle.current().pid();

        // try-with-resources asegura el cierre limpio de los descriptores al finalizar.
        // PrintWriter con autoFlush = true es crítico para que la salida viaje por el
        // pipe al instante.
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true)) {

            String line;
            // Bucle principal de lectura: lee línea a línea hasta que se cierre el pipe
            // (EOF) o llegue EXIT/CRASH
            while ((line = reader.readLine()) != null) {
                String originalCommand = line.trim();

                // Si la línea está vacía, es una orden no válida
                if (originalCommand.isEmpty()) {
                    reportError(out, myPid, "Línea vacía recibida.");
                    continue;
                }

                // Separamos por uno o más espacios en blanco (expresión regular "\\s+")
                String[] tokens = originalCommand.split("\\s+");
                String operation = tokens[0].toUpperCase();

                switch (operation) {
                    case "SUM":
                        handleSum(tokens, originalCommand, out, myPid);
                        break;

                    case "MUL":
                        handleMul(tokens, originalCommand, out, myPid);
                        break;

                    case "FACT":
                        handleFact(tokens, originalCommand, out, myPid);
                        break;

                    case "PRIMO":
                        handlePrimo(tokens, originalCommand, out, myPid);
                        break;

                    case "PID":
                        if (tokens.length != 1) {
                            reportError(out, myPid, "La orden PID no admite argumentos: " + originalCommand);
                        } else {
                            out.println(myPid);
                        }
                        break;

                    case "CRASH":
                        if (tokens.length != 1) {
                            reportError(out, myPid, "La orden CRASH no admite argumentos: " + originalCommand);
                        } else {
                            // No responde por stdout; aborta el proceso inmediatamente con código 3
                            System.exit(EXIT_CODE_CRASH);
                        }
                        break;

                    case "EXIT":
                        if (tokens.length != 1) {
                            reportError(out, myPid, "La orden EXIT no admite argumentos: " + originalCommand);
                        } else {
                            out.println("BYE");
                            System.exit(EXIT_CODE_OK);
                        }
                        break;

                    default:
                        reportError(out, myPid, "Orden desconocida: " + originalCommand);
                        break;
                }
            }
        } catch (IOException e) {
            System.err.printf("[PID %d] Error crítico de E/S en el worker: %s%n", myPid, e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Procesa la orden SUM a b.
     */
    private static void handleSum(String[] tokens, String rawCmd, PrintWriter out, long pid) {
        if (tokens.length != 3) {
            reportError(out, pid, "Número de argumentos incorrecto para SUM: " + rawCmd);
            return;
        }
        try {
            long a = Long.parseLong(tokens[1]);
            long b = Long.parseLong(tokens[2]);
            out.println(a + b);
        } catch (NumberFormatException e) {
            reportError(out, pid, "Argumentos no numéricos en SUM: " + rawCmd);
        }
    }

    /**
     * Procesa la orden MUL a b.
     */
    private static void handleMul(String[] tokens, String rawCmd, PrintWriter out, long pid) {
        if (tokens.length != 3) {
            reportError(out, pid, "Número de argumentos incorrecto para MUL: " + rawCmd);
            return;
        }
        try {
            long a = Long.parseLong(tokens[1]);
            long b = Long.parseLong(tokens[2]);
            out.println(a * b);
        } catch (NumberFormatException e) {
            reportError(out, pid, "Argumentos no numéricos en MUL: " + rawCmd);
        }
    }

    /**
     * Procesa la orden FACT n, validando que 0 <= n <= 20.
     */
    private static void handleFact(String[] tokens, String rawCmd, PrintWriter out, long pid) {
        if (tokens.length != 2) {
            reportError(out, pid, "Número de argumentos incorrecto para FACT: " + rawCmd);
            return;
        }
        try {
            int n = Integer.parseInt(tokens[1]);
            if (n < 0 || n > 20) {
                reportError(out, pid, "Rango inválido en FACT (debe ser 0 <= n <= 20): " + rawCmd);
                return;
            }
            long result = 1L;
            for (int i = 2; i <= n; i++) {
                result *= i;
            }
            out.println(result);
        } catch (NumberFormatException e) {
            reportError(out, pid, "Argumento no válido en FACT: " + rawCmd);
        }
    }

    /**
     * Procesa la orden PRIMO n con optimización O(sqrt(n)).
     */
    private static void handlePrimo(String[] tokens, String rawCmd, PrintWriter out, long pid) {
        if (tokens.length != 2) {
            reportError(out, pid, "Número de argumentos incorrecto para PRIMO: " + rawCmd);
            return;
        }
        try {
            long n = Long.parseLong(tokens[1]);
            out.println(isPrime(n) ? "SI" : "NO");
        } catch (NumberFormatException e) {
            reportError(out, pid, "Argumento no numérico en PRIMO: " + rawCmd);
        }
    }

    /**
     * Algoritmo eficiente de primalidad en tiempo O(√n) descartando pares y
     * múltiplos de 3.
     */
    private static boolean isPrime(long n) {
        if (n <= 1)
            return false;
        if (n <= 3)
            return true;
        if (n % 2 == 0 || n % 3 == 0)
            return false;
        for (long i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0)
                return false;
        }
        return true;
    }

    /**
     * Cumple con el contrato de errores:
     * 1. Envía "ERROR" por stdout para desbloquear el readLine() del Gestor.
     * 2. Escribe el detalle con su PID en stderr (que irá redirigido a
     * workers.log).
     */
    private static void reportError(PrintWriter out, long pid, String reason) {
        out.println("ERROR");
        System.err.printf("[PID %d] Orden no válida: %s%n", pid, reason);
        System.err.flush();
    }
}