package PSP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * EJEMPLO 8. El problema del BLOQUEO: hay que leer ANTES de esperar.
 *
 * La tubería entre el hijo y el padre tiene un tamaño limitado (unos 64 KB).
 * Si el hijo escribe más de eso y el padre no lo lee, la tubería se llena
 * y el hijo se queda BLOQUEADO esperando a que haya sitio.
 *
 * Si el padre, en vez de leer, hace waitFor(), los dos se quedan esperando
 * al otro para siempre:
 *      - el padre espera a que el hijo termine;
 *      - el hijo espera a que el padre lea.
 * Esta situación se llama INTERBLOQUEO (deadlock).
 *
 * Usamos "seq 1 200000", que escribe los números del 1 al 200.000 (más de
 * 1 MB). Para no colgar el programa de verdad, usamos waitFor con tiempo.
 */
public class Ejemplo08_Bloqueo {

    public static void main(String[] args) throws IOException, InterruptedException {

        // ------------- MAL: esperar sin leer -------------
        System.out.println("=== MAL: waitFor() antes de leer ===");
        Process p1 = new ProcessBuilder("seq", "1", "200000").start();

        boolean terminado = p1.waitFor(3, TimeUnit.SECONDS);
        System.out.println("¿Ha terminado el hijo en 3 s?: " + terminado);
        if (!terminado) {
            System.out.println("El hijo está BLOQUEADO: la tubería está llena y nadie la lee.");
            System.out.println("Con waitFor() sin tiempo, el programa se quedaría colgado aquí.");
            p1.destroy();
        }

        // ------------- BIEN: leer primero, esperar después -------------
        System.out.println("\n=== BIEN: leer la salida y después waitFor() ===");
        long inicio = System.currentTimeMillis();
        Process p2 = new ProcessBuilder("seq", "1", "200000").start();

        BufferedReader br = new BufferedReader(new InputStreamReader(p2.getInputStream()));
        int lineas = 0;
        String ultima = null;
        String linea;
        while ((linea = br.readLine()) != null) {
            lineas++;
            ultima = linea;
        }

        int codigo = p2.waitFor();
        System.out.println("Líneas leídas: " + lineas + " (la última es " + ultima + ")");
        System.out.println("Código de salida: " + codigo + ", en "
                + (System.currentTimeMillis() - inicio) + " ms");
    }
}
