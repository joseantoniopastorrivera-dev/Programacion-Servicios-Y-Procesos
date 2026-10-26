package PSP;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Ejercicio9 {
    public static void main(String[] args) throws IOException, InterruptedException {
        Process p = new ProcessBuilder("sleep", "10").start();
        System.out.println("Lanzado sleep 10 con PID " + p.pid());

        boolean terminado = p.waitFor(3, TimeUnit.SECONDS);

        if (!terminado) {
            System.out.println("Han pasado 3 segundos. ¿Vivo?: " + p.isAlive());
            p.destroy();
            p.waitFor();   // esperamos a que termine de verdad
            System.out.println("Después de destroy(). ¿Vivo?: " + p.isAlive());
        }

        System.out.println("Código de salida: " + p.exitValue());
    }
}
