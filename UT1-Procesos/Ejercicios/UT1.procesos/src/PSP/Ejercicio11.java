package PSP;

import java.io.IOException;
import java.util.Iterator;

public class Ejercicio11 {
    public static void main(String[] args) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", "sh -c 'sleep 60 & wait' & sleep 60 & wait");
        Process p = pb.start();
        System.out.println("Proceso padre: PID " + p.pid());

        Thread.sleep(500);

        // 1) Hijos directos frente a todos los descendientes
        System.out.println("children(): " + p.children().count());
        System.out.println("descendants(): " + p.descendants().count());

        // 2) Información de cada descendiente
        System.out.println("\n--- Descendientes ---");
        Iterator<ProcessHandle> it = p.descendants().iterator();
        while (it.hasNext()) {
            ProcessHandle ph = it.next();
            long pidPadre = -1;
            if (ph.parent().isPresent()) {
                pidPadre = ph.parent().get().pid();
            }
            System.out.println("PID " + ph.pid()
                    + " | padre " + pidPadre
                    + " | comando: " + ph.info().commandLine().orElse("?"));
        }

        // 3) Destruimos descendientes y padre
        it = p.descendants().iterator();
        while (it.hasNext()) {
            it.next().destroy();
        }
        p.destroy();
        p.waitFor();
        Thread.sleep(500);

        System.out.println("\n¿Padre vivo?: " + p.isAlive());
        System.out.println("Descendientes vivos: " + p.descendants().count());
    }
}
