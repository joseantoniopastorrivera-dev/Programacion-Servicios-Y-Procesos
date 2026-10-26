package PSP;

import java.io.File;
import java.io.IOException;

// redirectOutput(): la salida del hijo va a un FICHERO, no al padre
// Equivale en la terminal a:  ls -l > listado.txt
public class EjemploRedirectOutput {
    public static void main(String[] args) throws IOException, InterruptedException {

        File fichero = new File("listado.txt");

        ProcessBuilder pb = new ProcessBuilder("ls", "-l");
        pb.redirectOutput(fichero);   // si el fichero existe, se sobrescribe
        // Para añadir al final (>>):  pb.redirectOutput(ProcessBuilder.Redirect.appendTo(fichero));

        Process hijo = pb.start();
        int codigo = hijo.waitFor();   // no hay que leer nada: el hijo escribe directamente en el fichero

        System.out.println("El hijo ha terminado con código " + codigo);
        System.out.println("Su salida está en " + fichero.getAbsolutePath());
        System.out.println("Tamaño: " + fichero.length() + " bytes");
    }
}
