package ut1;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Lanzador4 {
    public static void main(String[] args) {
        try {
            // Creación del processBuilder
            String app[] = {"ping", "-c", "20", "google.com"};
            // Otras alternativas para lanzar, por ejemplo un editor de texto
            // Linux: Escritorio KDE-Plasma
            // String app[] = {"kwrite", "ejemplo.txt"};
            // Linux: Escritorio Gnome
            // String app[] = {"gedit", "ejemplo.txt"};
            // Windows
            // String app[] = {"notepad.exe", "ejemplo.txt"};

            ProcessBuilder pb = new ProcessBuilder(app);

            // Definimos e instanciamos el proceso
            Process p = pb.start();

            // Obtenemos la información asociada al proceso
            ProcessHandle.Info info = p.info();

            System.out.println("PID: "+p.pid());
            System.out.println("Comando: "+info.command().orElse("-"));
            System.out.println("Línea de comando: "+info.commandLine().orElse("-"));           
            System.out.println("Hora de inicio: "+info.startInstant().toString());
            System.out.println("Duración en la CPU: "+info.totalCpuDuration().toString());
            System.out.println("Usuario:"+info.user().orElse("-"));


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
