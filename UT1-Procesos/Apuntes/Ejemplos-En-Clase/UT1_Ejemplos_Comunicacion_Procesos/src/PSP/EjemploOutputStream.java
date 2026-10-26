package PSP;

import java.io.IOException;
import java.io.PrintWriter;

// getOutputStream(): el padre ESCRIBE en la entrada del hijo
public class EjemploOutputStream {
    public static void main(String[] args) throws IOException, InterruptedException {

        // tr pasa a mayúsculas todo lo que recibe por su entrada
        ProcessBuilder pb = new ProcessBuilder("tr", "a-z", "A-Z");
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);   // lo que escriba el hijo sale directamente por pantalla
        Process hijo = pb.start();

        PrintWriter pw = new PrintWriter(hijo.getOutputStream());
        pw.println("hola, soy tu padre");
        pw.println("esto lo escribo yo y lo recibe el hijo");
        pw.close();   // fin de los datos: el hijo puede terminar

        hijo.waitFor();
    }
}
