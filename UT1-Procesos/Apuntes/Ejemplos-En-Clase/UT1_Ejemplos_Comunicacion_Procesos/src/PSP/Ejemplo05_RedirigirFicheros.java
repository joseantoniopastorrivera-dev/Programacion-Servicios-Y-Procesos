package PSP;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

/**
 * EJEMPLO 5. Redirigir la entrada y las salidas del hijo a FICHEROS.
 *
 * En lugar de leer o escribir nosotros en las tuberías, le decimos al
 * ProcessBuilder que conecte los flujos del hijo directamente a ficheros.
 * El padre no tiene que leer nada: el sistema operativo lo hace por él.
 *
 *   redirectOutput(File)           -> la salida del hijo va a un fichero (lo sobrescribe)
 *   redirectOutput(Redirect.appendTo(File)) -> igual, pero añade al final
 *   redirectError(File)            -> los errores del hijo van a un fichero
 *   redirectInput(File)            -> el hijo lee su entrada de un fichero
 *
 * Es lo mismo que hacemos en la terminal con >, >>, 2> y <.
 */
public class Ejemplo05_RedirigirFicheros {

    public static void main(String[] args) throws IOException, InterruptedException {

        File salida = new File("salida.txt");
        File errores = new File("errores.txt");

        // --- 1) Salida y errores a ficheros (como "ls . noExiste > salida.txt 2> errores.txt")
        ProcessBuilder pb1 = new ProcessBuilder("ls", ".", "carpetaQueNoExiste");
        pb1.redirectOutput(salida);
        pb1.redirectError(errores);
        int codigo = pb1.start().waitFor();
        System.out.println("1) ls terminado con código " + codigo);
        System.out.println("   Salida guardada en " + salida.getName() + " (" + salida.length() + " bytes)");
        System.out.println("   Errores guardados en " + errores.getName() + " (" + errores.length() + " bytes)");

        // --- 2) Añadir al final de un fichero (como "date >> salida.txt")
        ProcessBuilder pb2 = new ProcessBuilder("date");
        pb2.redirectOutput(Redirect.appendTo(salida));
        pb2.start().waitFor();
        System.out.println("2) Fecha añadida al final de " + salida.getName()
                + " (ahora " + salida.length() + " bytes)");

        // --- 3) Entrada desde un fichero (como "sort < nombres.txt > ordenados.txt")
        File nombres = new File("nombres.txt");
        FileWriter fw = new FileWriter(nombres);
        fw.write("Marta\nAna\nZoe\nCarlos\nBea\n");
        fw.close();

        File ordenados = new File("ordenados.txt");
        ProcessBuilder pb3 = new ProcessBuilder("sort");
        pb3.redirectInput(nombres);
        pb3.redirectOutput(ordenados);
        pb3.start().waitFor();
        System.out.println("3) " + nombres.getName() + " ordenado en " + ordenados.getName());

        System.out.println("\nComprueba los ficheros con: cat salida.txt errores.txt ordenados.txt");
    }
}
