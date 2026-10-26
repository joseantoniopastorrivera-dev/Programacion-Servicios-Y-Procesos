package PSP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// Redirect.PIPE: la salida del hijo va por una TUBERÍA hasta el padre
// Es la opción por defecto: si no se indica nada, ProcessBuilder usa PIPE
public class EjemploPipe {
    public static void main(String[] args) throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder("echo", "Hola desde el hijo");
        System.out.println("Redirección por defecto: " + pb.redirectOutput());

        // ---- Con PIPE: el padre recibe la salida del hijo ----
        pb.redirectOutput(ProcessBuilder.Redirect.PIPE);   // no haría falta: es la opción por defecto
        Process hijo1 = pb.start();
        System.out.println("\nCon PIPE, el padre lee: " + leer(hijo1));
        hijo1.waitFor();

        // ---- Con INHERIT: la salida va a la consola y el padre no recibe nada ----
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        Process hijo2 = pb.start();
        hijo2.waitFor();   // aquí la línea del hijo aparece directamente en pantalla
        System.out.println("Con INHERIT, el padre lee: " + leer(hijo2));
    }

    // Lee la primera línea de la salida del hijo
    private static String leer(Process p) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String linea = br.readLine();
        br.close();
        return linea;
    }
}
