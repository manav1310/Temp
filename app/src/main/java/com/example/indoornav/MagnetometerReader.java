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
    // record the angle turned of the compass picture
    private float DegreeStart = 0f;

    TextView DegreeTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnetometer_reader);
        compassimage = (ImageView) findViewById(R.id.CompassImage);
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
        SensorManage.registerListener(this, SensorManage.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {
        degree = Math.round(event.values[0]);
        DegreeTV.setText("Magnetometer Reading: " + degree + " degrees N");
        // rotation animation - reverse turn degree degrees
        RotateAnimation ra = new RotateAnimation(
                DegreeStart,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        // set the compass animation after the end of the reservation status
        ra.setFillAfter(true);
        // set how long the animation for the compass image will take place
        ra.setDuration(210);
        // Start animation of compass image
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
