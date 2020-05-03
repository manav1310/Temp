package com.example.indoornav;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Pair;
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
import static java.lang.Integer.parseInt;

public class Navigation extends AppCompatActivity implements SensorEventListener {
    //public Bundle out = new Bundle();
    private String source;
    private String destination;
    private String map;
    private Integer height;
    private SensorManager sensorManager;
    private Sensor sensorGravity;
    private float[] gravity = new float[3];
    private int stepCount;
    private boolean toggle;
    private double prevY;
    private double threshold;
    private boolean ignore;
    private int countdown;
    private int distance;

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
            } catch (IOException ignored) {}
        }
        return stringBuilder.toString();
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

        System.out.println(stepCount);
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
                distance = (int)(stepCount*0.415*height);
                ignore = true;
            }
            prevY = gravity[1];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onStop(){
        super.onStop();
        sensorManager.unregisterListener(this, sensorGravity);
    }

    private ArrayList<Pair<String, String> > getshortestpath(Graph graph){

        Map<String, Boolean> visited = new HashMap<String, Boolean>();
        Map<String, Float> mindist   = new HashMap<String, Float>();
        Map<String, String> parent   = new HashMap<String, String>();
        ArrayList<Pair<String, String> > result = new ArrayList<Pair<String, String>>();

        for (Node node : graph.getNodes()){
            visited.put(node.getNodeName(), false);
            mindist.put(node.getNodeName(), (float) 1000000);
            parent.put(node.getNodeName(), null);
        }

        visited.put(source, true);
        mindist.put(source, (float) 0);
        parent.put(source, source);
        Map<String,ArrayList<Edge> > adjacencyList = graph.getAdjacencyList();

        Queue<String> queue = new LinkedList<String>();
        queue.add(source);
        while(!Objects.equals(queue.peek(), destination)){

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

        String curr = destination;
        while(!curr.equals(source)){
            String prev = parent.get(curr);
            result.add(Pair.create(prev, curr));
            curr = prev;
        }
        Collections.reverse(result);
        return result;
    }

    protected void routebwintermediate(Pair<String, String> p, Edge edge) {

        toggle = true;
        threshold = 0.64;
        stepCount = 0;
        String intersrc = p.first;
        String interdes = p.second;
        float  interdis = edge.getDistance();
        float  interdir = edge.getDirection();
        System.out.println(intersrc);
        System.out.println(interdes);
        System.out.println(interdis);
        System.out.println(interdir);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        assert sensorManager != null;
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void completerouting(){

        String EdgesFileContent = getFileContents(map + Helper.Edges);
        String NodesFileContent = getFileContents(map + Helper.Nodes);
        Graph graph = Graph.getInstance(map, NodesFileContent, EdgesFileContent);

        ArrayList<Pair<String, String> > shortestpath = getshortestpath(graph);
        ArrayList<Edge> edges = graph.getEdges();

        for(Pair<String, String> p : shortestpath){
            Edge edge = new Edge();
            for(Edge i : edges){
                if((i.getSt().equals(p.first)) && (i.getEn().equals(p.second))){
                    edge.copycontents(i);
                    break;
                }
            }
            routebwintermediate(p, edge);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Intent intent = getIntent();
        source = intent.getStringExtra(Helper.SourceNodeName);
        destination   = intent.getStringExtra(Helper.DestinationNodeName);
        map    = intent.getStringExtra(Helper.mapname);
        height = parseInt(Objects.requireNonNull(intent.getStringExtra(Helper.Height)));
        completerouting();
    }
}
