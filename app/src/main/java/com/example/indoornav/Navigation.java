package com.example.indoornav;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import javax.microedition.khronos.egl.EGL;

import static java.lang.Integer.parseInt;

public class Navigation extends AppCompatActivity implements SensorEventListener {
    public Bundle out = new Bundle();
    private int distreading;
    private int flag = 0;
    private String source;
    private String dest;
    private String map;
    private Integer height;
    private SensorManager sensorManager;
    private Sensor sensorGravity;
    private float[] smoothed = new float[3];
    private float[] gravity = new float[3];
    public static volatile int stepCount;
    private boolean toggle;
    private double prevY;
    private double threshold;
    private boolean ignore;
    private int countdown;
    private int ht;
    public static volatile int distance;
    protected Thread dis;

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        // Saves the data  on saved Instance as a key value pair Example As below key value pair
//        outState.putString("sourcenode", sourceNode);
//        outState.putString("destnode", destinationNode);
//        outState.putInt("dist", distreading);
//
//        // super.onSaveInstanceState(savedInstanceState);
//
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedState) {
//        super.onRestoreInstanceState(savedState);
//        sourceNode = savedState.getString("sourcenode");
//        destinationNode = savedState.getString("destnode");
//        distreading = savedState.getInt("dist");
//
//    }

    private ArrayList<Pair<String, String> > getshortestpath(String src, String des, Graph graph){

        Map<String, Boolean> visited = new HashMap<String, Boolean>();
        Map<String, Float> mindist   = new HashMap<String, Float>();
        Map<String, String> parent   = new HashMap<String, String>();
        ArrayList<Pair<String, String> > result = new ArrayList<Pair<String, String>>();

        for (Node node : graph.getNodes()){
            visited.put(node.getNodeName(), false);
            mindist.put(node.getNodeName(), (float) 1000000);
            parent.put(node.getNodeName(), null);
        }

        visited.put(src, true);
        mindist.put(src, (float) 0);
        parent.put(src, src);
        Map<String,ArrayList<Edge> > adjacencyList = graph.getAdjacencyList();

        Queue<String> queue = new LinkedList<String>();
        queue.add(src);
        while(!Objects.equals(queue.peek(), des)){

            visited.put(queue.peek(), true);

            for(Edge i : adjacencyList.get(queue.peek())){

                if(!visited.get(i.getEn())){
                    queue.add(i.getEn());
                }

                float distance = mindist.get(i.getSt()) + i.getDistance();

                if(distance < mindist.get(i.getEn())) {
                    mindist.put(i.getEn(), distance);
                    parent.put(i.getEn(), i.getSt());
                }

            }

            queue.remove();
        }

        String curr = des;
        while(!curr.equals(src)){
            String prev = parent.get(curr);
            result.add(Pair.create(prev, curr));
            curr = prev;
        }

        Collections.reverse(result);
//        for (Pair i : result){
//            System.out.println(i.first + " -> " + i.second);
//        }

        return result;
    }

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
        final Object lock = new Object();
        // get accelerometer data
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // we need to use a low pass filter to make data smoothed
            smoothed = lowPassFilter(event.values, gravity);
            gravity[0] = smoothed[0];
            gravity[1] = smoothed[1];
            gravity[2] = smoothed[2];


            if(ignore) {
                countdown--;
                ignore = (countdown >= 0) && ignore;
            }
            else
                countdown = 22;
            if(toggle && (Math.abs(prevY - gravity[1]) > threshold) && !ignore){
                synchronized (lock){
                    stepCount++;
                }
                distance = (int)(stepCount*0.415*ht);
//                stepView.setText("Step Count: " + stepCount);
//                dist.setText("Distance: " + distance);
                ignore = true;
            }
            prevY = gravity[1];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onStop(){
        super.onStop();
        sensorManager.unregisterListener(this, sensorGravity);
        dis.interrupt();
    }

    protected void routebwintermediate(Pair<String, String> p, Edge edge){

//        String intersrc = p.first;
//        String interdes = p.second;
//        float  interdis = edge.getDistance();
//        float  interdir = edge.getDirection();
//        float distcovered = 0;
        toggle = true;
        threshold = 0.64;
        stepCount = 0;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // listen to these sensors
        sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL);

//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        assert sensorManager != null;
//        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//
//        // listen to these sensors
//        sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL);

//        int i = 0;
//        while(i<100000){
//            TextView steps = findViewById(R.id.steps);
//            TextView dist  = findViewById(R.id.distance);
//            steps.setText(Integer.toString(stepCount));
//            dist.setText(Integer.toString(distance));
//            //System.out.println("Running");
//            System.out.println(stepCount);
//            i++;
//        }


//        TextView src1 = findViewById(R.id.textView3);
//        TextView des1 = findViewById(R.id.textView4);
//        Button btn = findViewById(R.id.button);
//        String s = src1.getText().toString();
//        String d = des1.getText().toString();
//        src1.setText(s + p.first);
//        des1.setText(d + p.second);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                System.out.println("Clicked");
//                return;
//            }
//        });
    }



    protected void completerouting(String src, String des, String map){

        String EdgesFileContent = getFileContents(map + Helper.Edges);
        String NodesFileContent = getFileContents(map + Helper.Nodes);
        Graph graph = Graph.getInstance(map, NodesFileContent, EdgesFileContent);

        ArrayList<Pair<String, String> > shortestpath = getshortestpath(src, des, graph);
        ArrayList<Edge> edges = graph.getEdges();

        for(Pair p : shortestpath){
            Edge edge = new Edge();
            for(Edge i : edges){
                if((i.getSt() == p.first) && (i.getEn() == p.second)){
                    edge.copycontents(i);
                    break;
                }
            }
            routebwintermediate(p, edge);
        }
    }

    private String getFileContents(String fileName){
        FileInputStream fis = null;
        try {
            fis = getApplication().openFileInput(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert fis != null;
        InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStreamReader.close();
            } catch (IOException ignored) {
                ;
            }
        }
        return stringBuilder.toString();
    }
    //for Distance(step count + distance)
//    public void  onButtonClick2(View view)
//    {
//        onSaveInstanceState(out);
//        Intent distReaderIntent = new Intent(this, DistanceCalculator.class);
//        startActivityForResult(distReaderIntent,Helper.GET_DIST_REQUEST_CODE);
//    }
//    protected void onActivityResult(int requestCode, int resultCode, Intent data)
//    {
//
//
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(requestCode == Helper.GET_DIST_REQUEST_CODE) {
//            if(resultCode==RESULT_OK) {
//                distreading = data.getIntExtra(Helper.Dist, -1);
//                onRestoreInstanceState(out);
//               // Toast.makeText(getApplicationContext(), "Source Selected : " + sourceNode+" and Destination Selected : "+destinationNode , Toast.LENGTH_LONG).show();
//                if (distreading != (-1)) {///answer is stored in distreading
//                    flag=1;
//                }
//            }
//        }
//
//
//    }

    @Override
    protected void onStart() {
        super.onStart();
        dis.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Intent intent = getIntent();
        source = intent.getStringExtra(Helper.SourceNodeName);
        dest   = intent.getStringExtra(Helper.DestinationNodeName);
        map    = intent.getStringExtra(Helper.mapname);
        height = parseInt(intent.getStringExtra(Helper.Height));
        dis = new Thread(new Distance());
        completerouting(source, dest, map);
        //Routing is completed
    }
}
