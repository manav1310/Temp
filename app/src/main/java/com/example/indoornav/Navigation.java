package com.example.indoornav;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Pair;
import android.widget.TextView;

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

public class Navigation extends AppCompatActivity {

    private Graph graph;
    private Integer height;
    private SensorManager sensorManager;
    private Sensor sensorGravity;
    private String source;
    private String destination;
    private float[] gravity = new float[3];
    private int stepCount;
    private boolean toggle;
    private double prevY;
    private double threshold;
    private boolean ignore;
    private int countdown;
    private int distance;
    private boolean stoploop = false;
    private ArrayList<Edge> shortestPath;
    private int currentIndex;
    private float distanceToNextNode;
    TextView steps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Intent intent = getIntent();
        source = intent.getStringExtra(Helper.SourceNodeName);
        destination = intent.getStringExtra(Helper.DestinationNodeName);
        height = parseInt(Objects.requireNonNull(intent.getStringExtra(Helper.Height)));
        steps = findViewById(R.id.steps);
        completeRouting();
    }

    @Override
    protected void onStop(){
        stopService(new Intent(this, RSSICalculator.class));
        //unregisterReceiver(DistanceBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        stopService(new Intent(this, RSSICalculator.class));
        unregisterReceiver(DistanceBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause(){
//        stopService(new Intent(this, RSSICalculator.class));
//        unregisterReceiver(DistanceBroadcastReceiver);
        finish();
        super.onPause();
    }

    protected void completeRouting(){
        graph = Graph.getInstance();
        shortestPath = getShortestPath();
        currentIndex = 0;
//        System.out.println("shortest path size : "+shortestPath.size());
        startService(new Intent(this, DistanceCalc.class));
        registerReceiver(DistanceBroadcastReceiver, new IntentFilter("DistanceCalc"));
        startRouting(0);
    }

//    @Override
//    protected void onResume(){
//        finish();
//        super.onResume();
//    }

    private ArrayList<Edge> getShortestPath(){

        Map<String, Boolean> visited = new HashMap<>();
        Map<String, Float> minimumDistance   = new HashMap<>();
        Map<String, Edge> parent   = new HashMap<>();
        ArrayList<Edge> result = new ArrayList<>();

        for (Node node : graph.getNodes()){
            visited.put(node.getNodeName(), false);
            minimumDistance.put(node.getNodeName(),(float) Integer.MAX_VALUE);
            parent.put(node.getNodeName(), null);
        }

        minimumDistance.put(source, (float) 0);

        Map<String,ArrayList<Edge> > adjacencyList = graph.getAdjacencyList();

        Queue<String> queue = new LinkedList<>();
        queue.add(source);

        while(!visited.get(destination)){
            if(visited.get(queue.peek())){
                queue.remove();
                continue;
            }
            visited.put(queue.peek(), true);
            for(Edge edge : adjacencyList.get(queue.peek())){
                if(!visited.get(edge.getEn())){
                    queue.add(edge.getEn());
                }
                float distance = minimumDistance.get(edge.getSt()) + edge.getDistance();
                if(distance < minimumDistance.get(edge.getEn())) {
                    minimumDistance.put(edge.getEn(), distance);
                    parent.put(edge.getEn(), edge);
                }
            }
            queue.remove();
        }
        Edge curr = parent.get(destination);
        while(curr!=null){
            Edge prev = parent.get(curr.getSt());
            result.add(curr);
            curr = prev;
        }
        Collections.reverse(result);
        return result;
    }

//    private ArrayList<Edge> getShortestPath(){
//
//        Map<String, Boolean> visited = new HashMap<>();
//        Map<String, Float> minimumDistance   = new HashMap<>();
//        Map<String, Edge> parent   = new HashMap<>();
//        ArrayList<Edge> result = new ArrayList<>();
//
//        for (Node node : graph.getNodes()){
//            visited.put(node.getNodeName(), false);
//            minimumDistance.put(node.getNodeName(),(float) Integer.MAX_VALUE);
//            parent.put(node.getNodeName(), null);
//        }
//
//        visited.put(source, true);
//        minimumDistance.put(source, (float) 0);
//        parent.put(source, null);
//
//        Map<String,ArrayList<Edge> > adjacencyList = graph.getAdjacencyList();
//
//        Queue<String> queue = new LinkedList<>();
//        queue.add(source);
//        while(!destination.equals(queue.peek())){
//
//            visited.put(queue.peek(), true);
//
//            for(Edge edge : adjacencyList.get(queue.peek())){
//
//                if(!visited.get(edge.getEn())){
//                    queue.add(edge.getEn());
//                }
//
//                float distance = minimumDistance.get(edge.getSt()) + edge.getDistance();
//
//                if(distance < minimumDistance.get(edge.getEn())) {
//                    minimumDistance.put(edge.getEn(), distance);
//                    parent.put(edge.getEn(), edge);
//                }
//            }
//            queue.remove();
//        }
//
//        Edge curr = parent.get(destination);
//        while(curr!=null){
//            Edge prev = parent.get(curr);
//            result.add(curr);
//            curr = prev;
//        }
//
//        Collections.reverse(result);
//        return result;
//    }

    private BroadcastReceiver DistanceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //System.out.println(distanceToNextNode);
            distanceToNextNode-=height*0.45;
            if(distanceToNextNode<=0){              //change it to 10 percent threshold
                startRouting(currentIndex++);
            }
        }
    };

    protected void startRouting(int currentIndex) {

        if(currentIndex==shortestPath.size()){
            //TODO reached
            System.out.println("Routing Complete");
//            String val = "Routing completed";
            steps.setText("Routing completed");
//            stopService(new Intent(this, RSSICalculator.class));
//            unregisterReceiver(DistanceBroadcastReceiver);
        }else {
            distanceToNextNode = 500;
//            distanceToNextNode = shortestPath.get(currentIndex).getDistance();
//            System.out.println(distanceToNextNode);
        }
//        while(!stoploop){
//            System.out.println("Running");
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
}