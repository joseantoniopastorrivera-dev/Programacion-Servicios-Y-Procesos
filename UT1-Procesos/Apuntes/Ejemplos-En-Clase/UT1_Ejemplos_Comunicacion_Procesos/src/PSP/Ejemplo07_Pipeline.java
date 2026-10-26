package PSP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * EJEMPLO 7. Tuberías entre procesos con startPipeline (Java 9 o superior).
 *
 * En la terminal, el símbolo | conecta la salida de un proceso con la
 * entrada del siguiente:
 *
 *      ps -ef | grep java | wc -l
 *
 * ProcessBuilder.startPipeline() hace lo mismo: recibe una lista de
 * ProcessBuilder y conecta cada uno con el siguiente. Devuelve la lista
 * de procesos lanzados. Solo leemos la salida del ÚLTIMO.
 *
 * Los datos pasan de un hijo a otro directamente: no pasan por el padre.
 */
public class Ejemplo07_Pipeline {

    public static void main(String[] args) throws IOException, InterruptedException {

        List<ProcessBuilder> tuberia = new ArrayList<ProcessBuilder>();
        tuberia.add(new ProcessBuilder("ps", "-ef"));
        tuberia.add(new ProcessBuilder("grep", "java"));
        tuberia.add(new ProcessBuilder("wc", "-l"));

        List<Process> procesos = ProcessBuilder.startPipeline(tuberia);

        // Leemos solo la salida del último proceso (wc -l)
        Process ultimo = procesos.get(procesos.size() - 1);
        BufferedReader br = new BufferedReader(new InputStreamReader(ultimo.getInputStream()));
        String resultado = br.readLine().trim();

        System.out.println("Comando equivalente: ps -ef | grep java | wc -l");
        System.out.println("Líneas que contienen 'java': " + resultado);

        // Mostramos los PID de cada proceso de la tubería
        for (int i = 0; i < procesos.size(); i++) {
            Process p = procesos.get(i);
            System.out.println("Proceso " + (i + 1) + ": PID " + p.pid()
                    + " " + tuberia.get(i).command() + " -> código " + p.waitFor());
        }
    }
}
