package PSP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * EJEMPLO 4. Conversación entre un padre y un hijo escritos en Java.
 *
 * El padre lanza HijoCuadrado y mantiene una conversación con él:
 * le envía un número, espera su respuesta, le envía otro...
 *
 *      Padre --- "4" --->  HijoCuadrado
 *      Padre <-- "16" ---  HijoCuadrado
 *
 * Diferencia con el Ejemplo 3: allí enviábamos TODO y después leíamos TODO.
 * Aquí alternamos escribir y leer, así que no podemos cerrar el flujo tras
 * cada envío. En su lugar usamos flush(), que obliga a enviar ya lo escrito
 * (si no, se queda en el buffer del padre y el hijo nunca lo recibe).
 *
 * Hay que ejecutarlo desde la raíz del proyecto: java -cp bin PSP.Ejemplo04_PadreHijo
 */
public class Ejemplo04_PadreHijo {

    public static void main(String[] args) throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder("java", "-cp", "bin", "PSP.HijoCuadrado");
        Process hijo = pb.start();

        PrintWriter haciaHijo = new PrintWriter(hijo.getOutputStream());
        BufferedReader desdeHijo = new BufferedReader(new InputStreamReader(hijo.getInputStream()));

        int[] numeros = {4, 7, 12};
        for (int i = 0; i < numeros.length; i++) {
            haciaHijo.println(numeros[i]);
            haciaHijo.flush();                       // enviamos YA el número

            String respuesta = desdeHijo.readLine(); // esperamos la respuesta
            System.out.println("Padre: el cuadrado de " + numeros[i] + " es " + respuesta);
        }

        // Le decimos al hijo que hemos terminado
        haciaHijo.println("fin");
        haciaHijo.close();

        // Leemos lo que quede (su mensaje de despedida)
        String linea;
        while ((linea = desdeHijo.readLine()) != null) {
            System.out.println("Padre recibe: " + linea);
        }

        System.out.println("Código de salida del hijo: " + hijo.waitFor());
    }
}
