package ut1; 

import java.io.IOException;

public class Lanzador {
    public static void main(String[] args) {
        try {
            // Definimos un vector de Strings con el comando
            // y los argumentos. En este caso, lanzamos el comando 
            // firefox y una URL como argumento.
            String app[] = {"ping", "-c", "3", "google.com"};
            //String app[] = {"open", "-a", "Calculator"};
        	
            // Creamos el ProcessBuilder
            ProcessBuilder pb = new ProcessBuilder(app);
            
            //pb.inheritIO();

            // Para lanzar el proceso debemos utilizar el método start.
            pb.start();
            
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}