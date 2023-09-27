package edu.eci.arsw.math;

public class PiThread extends Thread {
    private int start;
    private int count;
    private int totalDigits = 0;
    private byte[] digits;
    private Object lock;
    private boolean stop = false;
    private static int DigitsPerSum = 8;
    private static double Epsilon = 1e-17;

    public PiThread(int start, int count, Object lock) {
        this.start = start;
        this.count = count;
        this.lock = lock;
        digits = new byte[count];
    }

    public void run() {
        if (start < 0) {
            throw new RuntimeException("Invalid Interval");
        }
        if (count < 0) {
            throw new RuntimeException("Invalid Interval");
        }
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
            totalDigits++;
            if (stop) { //Si se necesita parar se verifica la variable.
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        }
    }

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

    public byte[] getDigits() { //Retorna los digitos que el hilo ha encontrado.
        return digits;
    }

    public int getTotalDigits() { //Retorna el numero de digitos que el hilo ha encontrado
        return totalDigits;
    }

    public void setStop(boolean stop) { //Cambia la variable "stop", con la intencion de detener o reanudar los hilos.
        this.stop = stop;
    }

}
