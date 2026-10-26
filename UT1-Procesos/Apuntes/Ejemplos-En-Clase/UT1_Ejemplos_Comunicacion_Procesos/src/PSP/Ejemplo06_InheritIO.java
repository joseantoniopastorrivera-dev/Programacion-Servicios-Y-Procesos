package PSP;

import java.io.IOException;

/**
 * EJEMPLO 6. El hijo HEREDA la entrada y las salidas del padre.
 *
 * Con inheritIO() el hijo usa la misma consola que el padre:
 *   - lo que el hijo escribe aparece directamente en la pantalla del padre;
 *   - lo que se teclea en la consola le llega al hijo.
 *
 * El padre no lee nada: no hace falta getInputStream().
 * Si el padre se ejecuta con su salida redirigida a un fichero
 * (java ... > fichero.txt), la salida del hijo también va a ese fichero.
 *
 * Es lo más cómodo cuando solo queremos VER lo que hace el hijo, pero el
 * padre no puede procesar esa información (no la recibe).
 */
public class Ejemplo06_InheritIO {

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("Padre: lanzo ping y su salida aparecerá aquí directamente\n");

        ProcessBuilder pb = new ProcessBuilder("ping", "-c", "3", "google.com");
        pb.inheritIO();

        int codigo = pb.start().waitFor();

        System.out.println("\nPadre: el hijo ha terminado con código " + codigo);
    }
}
