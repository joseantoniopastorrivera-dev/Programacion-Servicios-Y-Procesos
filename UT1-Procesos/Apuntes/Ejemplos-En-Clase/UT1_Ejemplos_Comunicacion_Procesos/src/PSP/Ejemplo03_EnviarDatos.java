package PSP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * EJEMPLO 3. El padre ENVÍA datos al hijo.
 *
 * La entrada estándar del hijo (lo que leería del teclado) está conectada a
 * una tubería en la que el padre escribe con getOutputStream().
 *
 * De nuevo, el nombre se mira desde el padre: es "Output" porque el padre
 * escribe en él, pero para el hijo es su ENTRADA.
 *
 *      padre (getOutputStream) ----tubería----> hijo (System.in)
 *
 * Usamos el comando "sort", que lee líneas de su entrada, las ordena y las
 * escribe en su salida.
 *
 * MUY IMPORTANTE: hay que CERRAR el flujo de escritura. Así el hijo recibe
 * "fin de entrada" y sabe que ya no le llegarán más datos. Si no se cierra,
 * sort se queda esperando para siempre y el programa no termina.
 */
public class Ejemplo03_EnviarDatos {

    public static void main(String[] args) throws IOException, InterruptedException {

        Process p = new ProcessBuilder("sort").start();

        // 1) Escribimos en la entrada del hijo
        PrintWriter pw = new PrintWriter(p.getOutputStream());
        String[] nombres = {"Marta", "Ana", "Zoe", "Carlos", "Bea"};
        for (int i = 0; i < nombres.length; i++) {
            System.out.println("Padre envía: " + nombres[i]);
            pw.println(nombres[i]);
        }
        pw.close();   // ¡imprescindible! envía el "fin de entrada" al hijo

        // 2) Leemos la respuesta del hijo
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        System.out.println("\nEl hijo (sort) devuelve:");
        String linea;
        while ((linea = br.readLine()) != null) {
            System.out.println("  " + linea);
        }

        System.out.println("Código de salida: " + p.waitFor());
    }
}
