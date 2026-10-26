package PSP;

import java.io.File;
import java.io.IOException;

public class Ejercicio6 {
    public static void main(String[] args) throws IOException, InterruptedException {
        File carpeta = new File("pruebas");

        ProcessBuilder pb = new ProcessBuilder("touch", "fichero.txt");
        pb.directory(carpeta);   // el proceso se ejecuta dentro de "pruebas"

        Process p = pb.start();
        int codigo = p.waitFor();
        System.out.println("Código de salida de touch: " + codigo);

        File fichero = new File(carpeta, "fichero.txt");
        if (fichero.exists()) {
            System.out.println("Fichero creado en: " + fichero.getAbsolutePath());
        } else {
            System.out.println("El fichero no se ha creado");
        }
    }
}
