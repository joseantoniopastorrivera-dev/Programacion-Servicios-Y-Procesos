package PSP;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Ejercicio10 {
    public static void main(String[] args) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", "trap '' TERM; sleep 30");
        Process p = pb.start();
        System.out.println("Proceso rebelde lanzado con PID " + p.pid());

        Thread.sleep(500);   // damos tiempo a que la shell ejecute el trap

        p.destroy();
        boolean terminado = p.waitFor(2, TimeUnit.SECONDS);
        System.out.println("¿Ha terminado con destroy()?: " + terminado);

        if (!terminado) {
            p.destroyForcibly();
            int codigo = p.waitFor();
            System.out.println("Terminado con destroyForcibly(). Código de salida: " + codigo);
        }
    }
}
