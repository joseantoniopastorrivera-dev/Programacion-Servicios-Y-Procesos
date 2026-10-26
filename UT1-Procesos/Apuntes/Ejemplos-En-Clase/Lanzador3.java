package ut1;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class Lanzador3 {
    public static void main(String[] args) {
        try {
            // Creación del processBuilder
            //String app[] = {"ping", "-c", "10", "google.com"};
        	String app[] = {"ping", "-c", "3", "google.com"};

            ProcessBuilder pb = new ProcessBuilder(app);

            Process p = pb.start();
            

            Boolean isProcessDead = p.waitFor(3, TimeUnit.SECONDS);

            //System.out.println(isProcessDead);
                             
            if (!isProcessDead) {
                System.out.println("Destruyendo la aplicación");
                p.destroy(); 					// Sugerencia: Puedes usar destroyForcibly y 
                //p.destroyForcibly();            // comprobar el resultado.
            }


            while (p.isAlive()) {
                System.out.println("El proceso sigue vivo. Espero un milisegundo.");
                p.waitFor(1, TimeUnit.MILLISECONDS);
            }
            

            System.out.println("El proceso ha finalizado con la salida: "+p.exitValue());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        


    }
}
