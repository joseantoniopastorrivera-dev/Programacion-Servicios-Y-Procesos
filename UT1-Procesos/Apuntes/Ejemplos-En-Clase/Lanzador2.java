package ut1;

import java.io.IOException;

public class Lanzador2 {
    public static void main(String[] args) {
        try {
            // Definimos un vector de Strings con el comando
            // y los argumentos. En este caso, lanzamos el comando 
            // firefox y una URL como argumento.
            //String app[] = {"ping", "-c", "3", "google.com"};
            

        	
            // Creamos el ProcessBuilder
            ProcessBuilder pb = new ProcessBuilder("ping", "-c", "3", "google.com");

            // Crearemos dos objetos de tipo Process para almacenar 
            // los procesos devueltos por cada una de las invocaciones a start.
            Process p1=pb.start();
            Process p2=pb.start();

            // Mostramos el PID de los procesos
            System.out.println("PID del proceso 1: "+p1.pid());
            System.out.println("PID del proceso 2: "+p2.pid());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}