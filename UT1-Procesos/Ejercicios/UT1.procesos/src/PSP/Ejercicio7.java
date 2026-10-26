package PSP;

import java.io.IOException;

public class Ejercicio7 {
    public static void main(String[] args) throws IOException, InterruptedException {
        String[] segundos = {"3", "5", "1"};

        // Versión A: secuencial
        long inicio = System.currentTimeMillis();
        for (int i = 0; i < segundos.length; i++) {
            Process p = new ProcessBuilder("sleep", segundos[i]).start();
            p.waitFor();
        }
        long fin = System.currentTimeMillis();
        System.out.println("Versión A (secuencial): " + (fin - inicio) / 1000.0 + " s");

        // Versión B: paralela
        inicio = System.currentTimeMillis();
        Process[] procesos = new Process[segundos.length];
        for (int i = 0; i < segundos.length; i++) {
            procesos[i] = new ProcessBuilder("sleep", segundos[i]).start();
        }
        for (int i = 0; i < procesos.length; i++) {
            procesos[i].waitFor();
        }
        fin = System.currentTimeMillis();
        System.out.println("Versión B (paralela): " + (fin - inicio) / 1000.0 + " s");
    }
}
