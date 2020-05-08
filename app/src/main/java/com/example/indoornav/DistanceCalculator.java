package com.example.indoornav;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class DistanceCalculator extends Activity implements SensorEventListener{

    private float[] gravity = new float[3];
    private SensorManager sensorManager;
    private Sensor sensorGravity;
    private TextView dist;
    private TextView stepView;
    private TextView thresholdView;
    private SeekBar seek;
    private ToggleButton countToggle;
    private EditText height;
    private int stepCount;
    private boolean toggle;
    private double prevY;
    private double threshold;
    private boolean ignore;
    private int countdown;
    private int ht;
    private int distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_calculator);
        dist = findViewById((R.id.Distance));
        thresholdView = findViewById((R.id.thresholdView));
        stepView = findViewById((R.id.stepView));
        countToggle = findViewById(R.id.countToggle);
        seek = findViewById(R.id.seek);
        height = findViewById(R.id.height);
        seek.setProgress(0);
        seek.incrementProgressBy(1);
        seek.setMax(40);
        implementListeners();
    }

    public void getHeight(View view)
    {
        height = findViewById(R.id.height);
        String content = height.getText().toString();
        ht = Integer.parseInt(content);
        Toast.makeText(getApplicationContext(),"Height is: "+ht+" cm",Toast.LENGTH_SHORT).show();
    }

    protected float[] lowPassFilter( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + 1.0f * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    protected void onStart() {
        super.onStart();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensorGravity,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this, sensorGravity);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float[] smoothed = lowPassFilter(event.values, gravity);
            gravity[0] = smoothed[0];
            gravity[1] = smoothed[1];
            gravity[2] = smoothed[2];


            if(ignore) {
                countdown--;
                ignore = (countdown >= 0);
            }
            else
                countdown = 22;
            if(toggle && (Math.abs(prevY - gravity[1]) > threshold) && !ignore){
                stepCount++;
                distance = (int)(stepCount*0.415*ht);
                stepView.setText("Step Count: " + stepCount);
                dist.setText("Distance: " + distance);
                ignore = true;
            }
            prevY = gravity[1];
        }
    }

    public void implementListeners(){
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                threshold = ((double)seek.getProgress()) * 0.02;
                thresholdView.setText("Threshold: "+ threshold);
            }
        });

        countToggle.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                toggle = !toggle ;
                if(toggle){
                    stepCount = 0;
                    countdown = 5;
                    ignore = true;
                    distance = (int)(stepCount*0.415*ht);
                    stepView.setText("Step Count: " + stepCount);
                    dist.setText("Distance: " + distance);
                }
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onButtonSend1(View view){
        Intent intent = new Intent();
        intent.putExtra(Helper.Dist, distance);
        setResult(RESULT_OK, intent);
        finish();
    }
}