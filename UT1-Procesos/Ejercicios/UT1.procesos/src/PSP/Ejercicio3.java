package PSP;

import java.io.IOException;

public class Ejercicio3 {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java -cp bin PSP.Ejercicio3 <comando> [argumentos...]");
            System.out.println("Ejemplo: java -cp bin PSP.Ejercicio3 open -a Calculator");
            return;
        }

        ProcessBuilder pb = new ProcessBuilder(args);
        System.out.println("Comando a ejecutar: " + pb.command());

        try {
            Process p = pb.start();
            System.out.println("Proceso lanzado con PID " + p.pid());
        } catch (IOException e) {
            System.out.println("No se ha podido lanzar el comando: " + e.getMessage());
        }
    }
}
