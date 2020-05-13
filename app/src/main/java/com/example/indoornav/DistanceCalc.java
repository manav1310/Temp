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
    private float[] gravity = new float[3];
    private boolean ignore;
    private int countdown;

    protected float[] lowPassFilter( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + 1.0f * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] smoothed = lowPassFilter(event.values, gravity);
            gravity[0] = smoothed[0];
            gravity[1] = smoothed[1];
            gravity[2] = smoothed[2];
            double threshold = 0.64;
            if(ignore) {
                countdown--;
                ignore = (countdown >= 0);
            }
            else
                countdown = 22;
            if((Math.abs(prevY - gravity[1]) > threshold) && !ignore){
                ignore = true;
                Intent intent1 = new Intent(Helper.DistanceCalculator_Broadcast);
                sendBroadcast(intent1);
            }
            prevY = gravity[1];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public DistanceCalc(){
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent){
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        Sensor sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL);
        ignore = true;
        countdown = 5;
    }
}
