package com.example.indoornav;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MagnetometerReader extends AppCompatActivity implements SensorEventListener {

    private SensorManager SensorManage;
    private int degree;
    private ImageView compassimage;
    private float DegreeStart = 0f;
    TextView DegreeTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnetometer_reader);
        compassimage = findViewById(R.id.CompassImage);
        DegreeTV = findViewById(R.id.DegreeTextView);
        SensorManage = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SensorManage.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SensorManage.registerListener(this, SensorManage.getDefaultSensor(3),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {
        degree = Math.round(event.values[0]);
        DegreeTV.setText("Magnetometer Reading: " + degree + " degrees N");
        RotateAnimation ra = new RotateAnimation(
                DegreeStart,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setFillAfter(true);
        ra.setDuration(210);
        compassimage.startAnimation(ra);
        DegreeStart = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSendButtonClick(View view){
        Intent intent = new Intent();
        intent.putExtra(Helper.Magnetometer, degree);
        setResult(RESULT_OK, intent);
        finish();
    }
}
