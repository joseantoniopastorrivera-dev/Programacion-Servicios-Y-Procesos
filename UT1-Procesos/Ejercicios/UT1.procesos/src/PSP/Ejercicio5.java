package PSP;

import java.io.IOException;
import java.util.Scanner;

public class Ejercicio5 {
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Introduce un host: ");
        String host = sc.nextLine();

        ProcessBuilder pb = new ProcessBuilder("ping", "-c", "1", host);
        Process p = pb.start();

        int codigo = p.waitFor();
        System.out.println("Código de salida: " + codigo);

        if (codigo == 0) {
            System.out.println(host + " responde");
        } else {
            System.out.println(host + " NO responde");
        }
        sc.close();
    }
}
