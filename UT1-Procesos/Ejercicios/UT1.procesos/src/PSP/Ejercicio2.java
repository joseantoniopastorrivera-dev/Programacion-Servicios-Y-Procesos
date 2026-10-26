package PSP;

import java.io.IOException;

public class Ejercicio2 {
    public static void main(String[] args) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("open", "-a", "Calculator");
        Process p = pb.start();

        System.out.println("PID del proceso lanzado: " + p.pid());
        System.out.println("PID de este programa Java: " + ProcessHandle.current().pid());

        // Esperamos 30 segundos para poder consultar los PID con ps
        Thread.sleep(30000);
    }
}
