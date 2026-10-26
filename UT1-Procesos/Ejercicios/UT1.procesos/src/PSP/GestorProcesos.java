package PSP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class GestorProcesos {

    private static ArrayList<Process> procesos = new ArrayList<Process>();
    private static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws InterruptedException {
        int opcion = 0;
        while (opcion != 4) {
            System.out.println("\n1. Lanzar proceso");
            System.out.println("2. Listar procesos lanzados");
            System.out.println("3. Terminar un proceso por PID");
            System.out.println("4. Salir");
            System.out.println("5. Terminar cualquier proceso del sistema por PID");
            System.out.print("Opción: ");
            opcion = Integer.parseInt(sc.nextLine().trim());

            switch (opcion) {
                case 1: lanzar(); break;
                case 2: listar(); break;
                case 3: terminarLanzado(); break;
                case 4: terminarTodos(); break;
                case 5: terminarCualquiera(); break;
                default: System.out.println("Opción no válida");
            }
        }
        System.out.println("Adiós");
    }

    private static void lanzar() {
        System.out.print("Comando: ");
        String linea = sc.nextLine().trim();
        try {
            Process p = new ProcessBuilder(linea.split(" ")).start();
            procesos.add(p);
            System.out.println("Lanzado con PID " + p.pid());
        } catch (IOException e) {
            System.out.println("No se ha podido lanzar: " + e.getMessage());
        }
    }

    private static void listar() {
        if (procesos.isEmpty()) {
            System.out.println("No hay procesos lanzados");
            return;
        }
        for (Process p : procesos) {
            String linea = "PID " + p.pid()
                    + " | " + p.info().commandLine().orElse("(terminado)")
                    + " | vivo: " + p.isAlive();
            if (!p.isAlive()) {
                linea += " | código de salida: " + p.exitValue();
            }
            System.out.println(linea);
        }
    }

    private static void terminarLanzado() throws InterruptedException {
        System.out.print("PID: ");
        long pid = Long.parseLong(sc.nextLine().trim());

        for (Process p : procesos) {
            if (p.pid() == pid) {
                terminar(p);
                return;
            }
        }
        System.out.println("Ese PID no corresponde a ningún proceso lanzado por el gestor");
    }

    private static void terminar(Process p) throws InterruptedException {
        if (!p.isAlive()) {
            System.out.println("El proceso " + p.pid() + " ya había terminado");
            return;
        }
        p.destroy();
        if (!p.waitFor(2, TimeUnit.SECONDS)) {
            System.out.println("No responde a destroy(), usando destroyForcibly()");
            p.destroyForcibly();
            p.waitFor();
        }
        System.out.println("Proceso " + p.pid() + " terminado con código " + p.exitValue());
    }

    private static void terminarTodos() throws InterruptedException {
        for (Process p : procesos) {
            if (p.isAlive()) {
                terminar(p);
            }
        }
    }

    // Ampliación
    private static void terminarCualquiera() {
        System.out.print("PID: ");
        long pid = Long.parseLong(sc.nextLine().trim());

        Optional<ProcessHandle> ph = ProcessHandle.of(pid);
        if (ph.isPresent()) {
            boolean enviado = ph.get().destroy();
            System.out.println("Petición de terminar enviada: " + enviado);
        } else {
            System.out.println("No existe ningún proceso con PID " + pid);
        }
    }
}
