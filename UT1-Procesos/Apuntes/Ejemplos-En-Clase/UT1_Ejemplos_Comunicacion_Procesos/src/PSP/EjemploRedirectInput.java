package PSP;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

// redirectInput(): el hijo lee su entrada de un FICHERO, no del padre
// Equivale en la terminal a:  sort < nombres.txt
public class EjemploRedirectInput {
    public static void main(String[] args) throws IOException, InterruptedException {

        // Preparamos el fichero de entrada (normalmente ya existiría)
        File fichero = new File("nombres.txt");
        FileWriter fw = new FileWriter(fichero);
        fw.write("Marta\nAna\nZoe\nCarlos\nBea\n");
        fw.close();

        ProcessBuilder pb = new ProcessBuilder("sort");
        pb.redirectInput(fichero);   // sort lee los nombres del fichero
        Process hijo = pb.start();

        // El padre no envía nada: solo lee el resultado
        BufferedReader br = new BufferedReader(new InputStreamReader(hijo.getInputStream()));
        String linea;
        System.out.println("Nombres ordenados por el hijo:");
        while ((linea = br.readLine()) != null) {
            System.out.println("  " + linea);
        }
        br.close();

        hijo.waitFor();
    }
}
