package com.example.indoornav;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class DistanceCalc extends IntentService implements SensorEventListener {

    private double prevY;
    private int distance;
    private SensorManager sensorManager;
    private Sensor sensorGravity;
    private Integer height = 170;
    private int stepCount;
    private boolean toggle = true;
    private double threshold = 0.64;
    private float[] gravity = new float[3];
    private Intent intent1;

    protected float[] lowPassFilter( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + 1.0f * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //System.out.println(stepCount);
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] smoothed = lowPassFilter(event.values, gravity);
            gravity[0] = smoothed[0];
            gravity[1] = smoothed[1];
            gravity[2] = smoothed[2];
            if(toggle && (Math.abs(prevY - gravity[1]) > threshold)){
                stepCount++;
                distance = (int)(stepCount*0.415*height);
            }
            prevY = gravity[1];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startid){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        sensorManager.unregisterListener(this, sensorGravity);
    }

    public DistanceCalc(){
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent){
        while(stepCount < 10){
            System.out.println(stepCount);
        }
        sendBroadcast(intent1);
    }
}
