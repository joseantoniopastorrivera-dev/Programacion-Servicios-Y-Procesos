package PSP;

import java.io.IOException;

public class Padre {
    public static void main(String[] args) throws IOException, InterruptedException {
        String[] valores = {"0", "5", "42"};

        for (int i = 0; i < valores.length; i++) {
            String[] comando = {"java", "-cp", "bin", "PSP.Hijo", valores[i]};
            Process p = new ProcessBuilder(comando).start();

            int codigo = p.waitFor();
            System.out.println("Hijo lanzado con " + valores[i] + " -> código de salida: " + codigo);
        }
    }
}
