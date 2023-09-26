package edu.eci.arsw.math;

public class PiThread extends Thread{
    private int start;
    private int count;
    private int totalDigits = 0;
    private byte[] digits;
    private String state = "RUN";
    private static int DigitsPerSum = 8;
    private static double Epsilon = 1e-17;

    public PiThread(int start, int count){
        this.start=start;
        this.count=count;
        digits = new byte[count];
    }

    public void run(){
        if (start < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        if (count < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        double sum = 0;

        for (int i = 0; i < count; i++) {
            if(state.equals("RUN")){
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
            }else{
                try {
                    synchronized(state){
                        state.wait();
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
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

    public byte[] getDigits(){
        return digits;
    }

    public int getTotalDigits(){
        return totalDigits;
    }

    public void setState(String state){
        if(state.equals("RUN")){
            this.state = state;
            this.state.notify();
        }else{
            this.state = state;
        }
            
    }

}
