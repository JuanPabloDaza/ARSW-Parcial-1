package edu.eci.arsw.math;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

///  <summary>
///  An implementation of the Bailey-Borwein-Plouffe formula for calculating hexadecimal
///  digits of pi.
///  https://en.wikipedia.org/wiki/Bailey%E2%80%93Borwein%E2%80%93Plouffe_formula
///  *** Translated from C# code: https://github.com/mmoroney/DigitsOfPi ***
///  </summary>
public class PiDigits {

    private static int DigitsPerSum = 8;
    private static double Epsilon = 1e-17;
    private static Object lock = new Object();

    /**
     * Returns a range of hexadecimal digits of pi.
     * 
     * @param start The starting location of the range.
     * @param count The number of digits to return
     * @return An array containing the hexadecimal digits.
     */
    public static byte[] getDigits(int start, int count) {
        if (start < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        if (count < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        byte[] digits = new byte[count];
        double sum = 0;

        for (int i = 0; i < count; i++) {
            if (i % DigitsPerSum == 0) {
                sum = 4 * sum(1, start)
                        - 2 * sum(4, start)
                        - sum(5, start)
                        - sum(6, start);

                start += DigitsPerSum;
            }

            sum = 16 * (sum - Math.floor(sum));
            digits[i] = (byte) sum;
        }

        return digits;
    }

    /// <summary>
    /// Returns the sum of 16^(n - k)/(8 * k + m) from 0 to k.
    /// </summary>
    /// <param name="m"></param>
    /// <param name="n"></param>
    /// <returns></returns>
    private static double sum(int m, int n) {
        double sum = 0;
        int d = m;
        int power = n;

        while (true) {
            double term;

            if (power > 0) {
                term = (double) hexExponentModulo(power, d) / d;
            } else {
                term = Math.pow(16, power) / d;
                if (term < Epsilon) {
                    break;
                }
            }

            sum += term;
            power--;
            d += 8;
        }

        return sum;
    }

    /// <summary>
    /// Return 16^p mod m.
    /// </summary>
    /// <param name="p"></param>
    /// <param name="m"></param>
    /// <returns></returns>
    private static int hexExponentModulo(int p, int m) {
        int power = 1;
        while (power * 2 <= p) {
            power *= 2;
        }

        int result = 1;

        while (power > 0) {
            if (p >= power) {
                result *= 16;
                result %= m;
                p -= power;
            }

            power /= 2;

            if (power > 0) {
                result *= result;
                result %= m;
            }
        }

        return result;
    }

    public static byte[] getDigits(int start, int count, int N) {
        if (start < 0) {
            throw new RuntimeException("Invalid Interval");
        }
        if (count < 0) {
            throw new RuntimeException("Invalid Interval");
        }
        byte[] digits = new byte[count];
        ArrayList<PiThread> threads = new ArrayList<PiThread>();
        int numbers = count / N; //Cantidad de digitos que cada hilo va a calcular.
        int uneven = count % N; //Si la division da un residuo entonces lo almacenamos.
        int lastone = start; //Guardamos donde empezo el ultimo hilo.
        for (int i = 0; i < N; i++) {
            if (i == N - 1) { //Si es el ultimo hilo, entonces le agregamos el residuo a los numeros que debe calcular.
                if (uneven != 0) {
                    numbers += uneven;
                }
            }
            threads.add(new PiThread(lastone, numbers, lock)); //Inicializamos el hilo y lo agregamos al ArrayList.
            lastone += numbers; //Guardamos la nueva ultima posicion.
        }
        for (PiThread thread : threads) {
            thread.start();
        }
        int digit = 0;

        Timer timer = new Timer(); //Timer para esperar.

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (PiThread thread : threads) {
                    thread.setStop(true); //Avisamos a cada hilo que debe detenerse.
                }
                for (PiThread thread : threads) {
                    System.out.println(
                            "El hilo: " + thread.getName() + " Encontro " + thread.getTotalDigits() + " Digitos.");//Obtenemos el numero de digitos encontrados por cada hilo.
                }
                System.out.println("Presione enter para continuar. ");
                String read;
                Scanner scanner = new Scanner(System.in);
                read = scanner.nextLine();//Esperamos la accion del usuario.
                if (read != null) {
                    scanner.close();
                    System.out.println("Continuando Busqueda...");
                    synchronized (lock) {
                        for (PiThread thread : threads) {
                            thread.setStop(false);//Cambiamos el valor de la variable en todos los hilos
                        }
                        lock.notifyAll();//Reanudamos a todos los hilos para terminar con el ejercicio.
                    }

                }

            }
        }, 5000); //Valor que se quiera esperar (milisegundos).

        try {
            for (PiThread thread : threads) {
                thread.join();
            }
            for (int j = 0; j < N; j++) {
                byte[] threadBytes = threads.get(j).getDigits(); //Solicita los digitos encontrados en bytes
                for (int k = 0; k < threadBytes.length; k++) { //Recorremos los digitos retornados por el hilo
                    digits[digit] = threadBytes[k]; //Asignamos el valor del byte a la lista del orquestador.
                    digit++;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return digits;
    }

}
