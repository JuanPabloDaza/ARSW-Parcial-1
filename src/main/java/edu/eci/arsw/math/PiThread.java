package edu.eci.arsw.math;

public class PiThread extends Thread{
    int start;
    int count;
    byte[] digits;

    public PiThread(int start, int count){
        this.start=start;
        this.count=count;
    }

    public void run(){
        digits = PiDigits.getDigits(start, count);
    }

    public byte[] getDigits(){
        return digits;
    }

}
