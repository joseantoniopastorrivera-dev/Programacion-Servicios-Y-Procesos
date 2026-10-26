package PSP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * EJEMPLO 1. El padre LEE lo que escribe el hijo.
 *
 * Por defecto, la salida estándar del hijo (lo que imprimiría en pantalla)
 * no va a la consola: va a una tubería que el padre puede leer con
 * getInputStream().
 *
 * Ojo con el nombre: es "Input" porque, desde el punto de vista del padre,
 * es un flujo de ENTRADA (el padre recibe datos). Pero lo que contiene es
 * la SALIDA del hijo.
 *
 *      hijo (System.out) ----tubería----> padre (getInputStream)
 */
public class Ejemplo01_LeerSalida {

    public static void main(String[] args) throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder("ping", "-c", "3", "google.com");
        Process p = pb.start();

        // 1) Obtenemos el flujo con la salida del hijo
        InputStream is = p.getInputStream();

        // 2) Lo envolvemos para poder leer línea a línea
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        // 3) Leemos hasta que el hijo cierre su salida (readLine devuelve null)
        String linea;
        int numLinea = 1;
        while ((linea = br.readLine()) != null) {
            System.out.println("[HIJO línea " + numLinea + "] " + linea);
            numLinea++;
        }
        br.close();

        // 4) Esperamos a que termine y mostramos su código de salida
        int codigo = p.waitFor();
        System.out.println("\nEl hijo ha terminado con código " + codigo);
    }
}
