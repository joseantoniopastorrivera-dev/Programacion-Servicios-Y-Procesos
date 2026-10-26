package PSP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * EJEMPLO 2. El padre lee los ERRORES del hijo.
 *
 * Un proceso tiene dos salidas distintas:
 *   - salida estándar (System.out)  -> el padre la lee con getInputStream()
 *   - salida de error (System.err)  -> el padre la lee con getErrorStream()
 *
 * Si solo leemos getInputStream(), los mensajes de error se pierden.
 *
 * Parte A: leemos cada salida por separado.
 * Parte B: con redirectErrorStream(true) juntamos los errores con la salida
 *          normal y lo leemos todo con getInputStream().
 *
 * Aviso: en la parte A leemos primero toda la salida y después los errores.
 * Funciona porque "ls" escribe pocos errores. Si el hijo escribiera muchos
 * errores, podría llenar esa tubería y bloquearse (ver Ejemplo 8). Por eso
 * la opción de la parte B es la más segura cuando no necesitamos separarlos.
 */
public class Ejemplo02_LeerErrores {

    public static void main(String[] args) throws IOException, InterruptedException {

        // "ls" con una carpeta que existe (.) y otra que no
        String[] comando = {"ls", ".", "carpetaQueNoExiste"};

        // ---------------- PARTE A: salidas por separado ----------------
        System.out.println("=== PARTE A: getInputStream() y getErrorStream() por separado ===");
        Process p = new ProcessBuilder(comando).start();

        BufferedReader salida = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader errores = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        String linea;
        System.out.println("--- Salida normal ---");
        while ((linea = salida.readLine()) != null) {
            System.out.println("[OUT] " + linea);
        }
        System.out.println("--- Errores ---");
        while ((linea = errores.readLine()) != null) {
            System.out.println("[ERR] " + linea);
        }
        System.out.println("Código de salida: " + p.waitFor());

        // ---------------- PARTE B: todo junto ----------------
        System.out.println("\n=== PARTE B: redirectErrorStream(true) ===");
        ProcessBuilder pb = new ProcessBuilder(comando);
        pb.redirectErrorStream(true);   // los errores van al mismo sitio que la salida
        Process p2 = pb.start();

        BufferedReader todo = new BufferedReader(new InputStreamReader(p2.getInputStream()));
        while ((linea = todo.readLine()) != null) {
            System.out.println("[OUT+ERR] " + linea);
        }
        System.out.println("Código de salida: " + p2.waitFor());
    }
}
