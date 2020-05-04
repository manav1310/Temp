package com.example.indoornav;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.Nullable;

public class MagRead extends IntentService implements SensorEventListener {

    public MagRead() {
        super("");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        assert sensorManager != null;
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(3), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {
        int degree = Math.round(event.values[0]);
        Intent intent = new Intent("MagRead");
        intent.putExtra("Degree", degree);
        sendBroadcast(intent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
