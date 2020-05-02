package com.example.indoornav;

public class Distance implements Runnable {

    //@Override
    public void start(){

    }

    @Override
    public void run() {
        final Object lock = new Object();
        int i = 0;
        while(i<100000){
            synchronized (lock) {
                Navigation.stepCount++;
            }
//            System.out.println("hello");
//            System.out.println(Navigation.stepCount);
            i++;
        }
    }
}
