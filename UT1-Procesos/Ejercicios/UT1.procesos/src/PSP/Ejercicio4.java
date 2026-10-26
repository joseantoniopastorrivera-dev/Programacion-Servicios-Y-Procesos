package PSP;

import java.io.IOException;

public class Ejercicio4 {
    public static void main(String[] args) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("mkdir", "pruebas");
        Process p = pb.start();

        int codigo = p.waitFor();
        System.out.println("Código de salida: " + codigo);

        if (codigo == 0) {
            System.out.println("Carpeta creada");
        } else {
            System.out.println("Error al crear la carpeta");
        }
    }
}
