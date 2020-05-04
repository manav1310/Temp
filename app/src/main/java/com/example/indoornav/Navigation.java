package com.example.indoornav;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import static java.lang.Integer.parseInt;

public class Navigation extends AppCompatActivity implements SensorEventListener {

    private Graph graph;
    private Integer height;
    private SensorManager sensorManager;
    private ImageView compassimage;
    private float DegreeStart = 0f;
    TextView DegreeTV;
    private String source;
    private String destination;
//    private double threshold;
//    private int distance;
    private ArrayList<Edge> shortestPath;
    private int currentIndex;
    private float distanceToNextNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Intent intent = getIntent();
        source = intent.getStringExtra(Helper.SourceNodeName);
        destination = intent.getStringExtra(Helper.DestinationNodeName);
        height = parseInt(Objects.requireNonNull(intent.getStringExtra(Helper.Height)));
        compassimage = findViewById(R.id.CompassImage);
        DegreeTV = findViewById(R.id.DegreeTextView);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        assert sensorManager != null;
//        sensorManager.registerListener(this, sensorManager.getDefaultSensor(3), SensorManager.SENSOR_DELAY_GAME);
        completeRouting();
    }

    @Override
    protected void onStop(){
        stopService(new Intent(this, RSSICalculator.class));
        sensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        stopService(new Intent(this, RSSICalculator.class));
        unregisterReceiver(DistanceBroadcastReceiver);
        sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override
    protected void onPause(){
        sensorManager.unregisterListener(this);
        finish();
        super.onPause();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {
        int degree = Math.round(event.values[0]);
        DegreeTV.setText("Heading towards " + degree + " degrees N");
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

    protected void completeRouting(){
        graph = Graph.getInstance();
        shortestPath = getShortestPath();
        currentIndex = 0;
//        System.out.println("shortest path size : "+shortestPath.size());
        startService(new Intent(this, DistanceCalc.class));
        registerReceiver(DistanceBroadcastReceiver, new IntentFilter("DistanceCalc"));
        startRouting(0);
    }

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
            for(Edge edge : Objects.requireNonNull(adjacencyList.get(queue.peek()))){
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

    private BroadcastReceiver DistanceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            distanceToNextNode-=height*0.45;
            if(distanceToNextNode<=0){              //change it to 10 percent threshold
                startRouting(currentIndex++);
            }
        }
    };

    @SuppressLint("SetTextI18n")
    protected void startRouting(int currentIndex) {

        if(currentIndex==shortestPath.size()){
            //TODO reached
            DegreeTV.setText("Routing completed");
//            stopService(new Intent(this, RSSICalculator.class));
//            unregisterReceiver(DistanceBroadcastReceiver);
        }else {
            distanceToNextNode = 500;
//            distanceToNextNode = shortestPath.get(currentIndex).getDistance();
        }
    }
}