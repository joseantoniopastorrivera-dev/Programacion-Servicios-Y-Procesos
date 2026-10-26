package PSP;

import java.io.IOException;

// Redirect.INHERIT: el hijo usa la MISMA consola que el padre
// Lo que escribe el hijo aparece directamente en pantalla, sin que el padre lo lea
public class EjemploInherit {
    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("Padre: lanzo al hijo");

        // ls lista la carpeta actual (salida) y se queja de la que no existe (error)
        ProcessBuilder pb = new ProcessBuilder("ls", ".", "carpetaQueNoExiste");
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);   // salida -> consola del padre
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);    // errores -> consola del padre
        // pb.inheritIO();  hace lo mismo para la entrada, la salida y los errores a la vez

        Process hijo = pb.start();
        int codigo = hijo.waitFor();

        System.out.println("Padre: el hijo ha terminado con código " + codigo);
    }
}
