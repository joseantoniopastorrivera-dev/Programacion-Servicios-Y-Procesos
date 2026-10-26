package PSP;

import java.io.IOException;

public class Ejercicio1 {
    public static void main(String[] args) {
        try {
            ProcessBuilder pb = new ProcessBuilder("open", "-a", "Calculator");
            // Segunda parte: new ProcessBuilder("open", "-a", "TextEdit");
            pb.start();
            System.out.println("Calculadora lanzada");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
