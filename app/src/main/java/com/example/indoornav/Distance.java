package com.example.indoornav;

import java.util.concurrent.TimeUnit;

public class Distance implements Runnable {

    //@Override
    public void start(){

    }

    @Override
    public void run() {
        final Object lock = new Object();
        int i = 0;
        while(i<10){
            synchronized (lock) {
                System.out.println(Navigation.stepCount);
            }
            i++;
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
