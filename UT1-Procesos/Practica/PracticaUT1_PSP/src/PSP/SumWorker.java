package PSP;

import java.util.Scanner;

public class SumWorker {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // Sacamos el PID de este proceso worker
        long miPid = ProcessHandle.current().pid();

        // Leemos línea por línea mientras el gestor nos siga mandando cosas
        while (sc.hasNextLine()) {
            String linea = sc.nextLine().trim();

            // Si nos mandan una línea vacía, damos error
            if (linea.isEmpty()) {
                mandarError(miPid, linea);
                continue;
            }

            // Separamos la línea por espacios
            String[] partes = linea.split("\\s+");
            String orden = partes[0].toUpperCase();

            switch (orden) {
                case "SUM":
                    if (partes.length != 3) {
                        mandarError(miPid, linea);
                    } else {
                        try {
                            long num1 = Long.parseLong(partes[1]);
                            long num2 = Long.parseLong(partes[2]);
                            System.out.println(num1 + num2);
                        } catch (NumberFormatException e) {
                            mandarError(miPid, linea);
                        }
                    }
                    break;

                case "MUL":
                    if (partes.length != 3) {
                        mandarError(miPid, linea);
                    } else {
                        try {
                            long num1 = Long.parseLong(partes[1]);
                            long num2 = Long.parseLong(partes[2]);
                            System.out.println(num1 * num2);
                        } catch (NumberFormatException e) {
                            mandarError(miPid, linea);
                        }
                    }
                    break;

                case "FACT":
                    if (partes.length != 2) {
                        mandarError(miPid, linea);
                    } else {
                        try {
                            int n = Integer.parseInt(partes[1]);
                            // El enunciado pide entre 0 y 20
                            if (n < 0 || n > 20) {
                                mandarError(miPid, linea);
                            } else {
                                long fact = 1;
                                for (int i = 1; i <= n; i++) {
                                    fact = fact * i;
                                }
                                System.out.println(fact);
                            }
                        } catch (NumberFormatException e) {
                            mandarError(miPid, linea);
                        }
                    }
                    break;

                case "PRIMO":
                    if (partes.length != 2) {
                        mandarError(miPid, linea);
                    } else {
                        try {
                            long n = Long.parseLong(partes[1]);
                            if (comprobarPrimo(n)) {
                                System.out.println("SI");
                            } else {
                                System.out.println("NO");
                            }
                        } catch (NumberFormatException e) {
                            mandarError(miPid, linea);
                        }
                    }
                    break;

                case "PID":
                    if (partes.length != 1) {
                        mandarError(miPid, linea);
                    } else {
                        System.out.println(miPid);
                    }
                    break;

                case "CRASH":
                    if (partes.length != 1) {
                        mandarError(miPid, linea);
                    } else {
                        // No responde nada y se cierra con código 3
                        System.exit(3);
                    }
                    break;

                case "EXIT":
                    if (partes.length != 1) {
                        mandarError(miPid, linea);
                    } else {
                        System.out.println("BYE");
                        sc.close();
                        System.exit(0);
                    }
                    break;

                default:
                    // Si la orden no existe
                    mandarError(miPid, linea);
                    break;
            }
        }
        sc.close();
    }

    // Método para no repetir las dos líneas de error en cada case
    private static void mandarError(long pid, String ordenMala) {
        // 1. Respondemos ERROR por la salida normal para que el gestor no se quede
        // pillado
        System.out.println("ERROR");
        // 2. Escribimos el motivo en la salida de error con el formato exacto del PDF
        System.err.println("[PID " + pid + "] Orden no valida: " + ordenMala);
    }

    // Método sencillo para saber si un número es primo
    private static boolean comprobarPrimo(long n) {
        if (n <= 1) {
            return false;
        }
        // Probamos divisores desde 2 hasta la raíz cuadrada de n
        for (long i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
}