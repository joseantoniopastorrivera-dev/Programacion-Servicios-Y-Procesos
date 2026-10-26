package PSP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// getInputStream(): el padre LEE lo que el hijo escribe en su salida
public class EjemploInputStream {
    public static void main(String[] args) throws IOException, InterruptedException {

        Process hijo = new ProcessBuilder("ls", "-1").start();

        BufferedReader br = new BufferedReader(new InputStreamReader(hijo.getInputStream()));
        String linea;
        while ((linea = br.readLine()) != null) {
            System.out.println("El hijo me ha enviado: " + linea);
        }
        br.close();

        hijo.waitFor();
    }
}
