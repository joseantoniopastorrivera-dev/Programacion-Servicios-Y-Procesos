package PSP;

import java.util.Scanner;

/**
 * Proceso HIJO para el Ejemplo 4.
 *
 * Lee números de su entrada estándar (System.in) y, por cada uno, escribe
 * su cuadrado en su salida estándar (System.out). Termina al recibir "fin"
 * o cuando se cierra su entrada.
 *
 * Fíjate en que este programa no sabe nada de procesos: lee del "teclado"
 * y escribe en la "pantalla". Es el padre quien conecta esos flujos.
 * Puedes probarlo solo en la terminal: java -cp bin PSP.HijoCuadrado
 */
public class HijoCuadrado {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (sc.hasNextLine()) {
            String linea = sc.nextLine().trim();
            if (linea.equals("fin")) {
                break;
            }
            int n = Integer.parseInt(linea);
            System.out.println(n * n);
        }

        System.out.println("Hijo: termino");
        sc.close();
    }
}
