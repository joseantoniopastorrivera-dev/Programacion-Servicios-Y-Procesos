package PSP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// getErrorStream(): el padre LEE los mensajes de error del hijo
public class EjemploErrorStream {
    public static void main(String[] args) throws IOException, InterruptedException {

        Process hijo = new ProcessBuilder("ls", "carpetaQueNoExiste").start();

        BufferedReader br = new BufferedReader(new InputStreamReader(hijo.getErrorStream()));
        String linea;
        while ((linea = br.readLine()) != null) {
            System.out.println("El hijo se ha quejado: " + linea);
        }
        br.close();

        int codigo = hijo.waitFor();
        System.out.println("Código de salida: " + codigo);
    }
}
