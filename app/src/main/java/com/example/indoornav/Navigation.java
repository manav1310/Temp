package com.example.indoornav;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private String source;
    private String destination;
    private ImageView compassimage;
    TextView DegreeTV;
    private float DegreeStart = 0f;
    private ArrayList<Edge> shortestPath;
    private int currentIndex;
    private float distanceToNextNode;
    private float totaldistance;
    private Map<String, Integer> RSSIDataMap = null;
    private Map<String, String> RSSISSIDs = null;
    private Boolean isRSSIDataSet = false;

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
        FloatingActionButton fab = findViewById(R.id.sync);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRouting(currentIndex++);
            }
        });
        completeRouting();
    }

    @Override
    protected void onStop(){
        stopService(new Intent(this, DistanceCalc.class));
        unregisterReceiver(DistanceBroadcastReceiver);
        stopService(new Intent(this, MagRead.class));
        unregisterReceiver(MagnetometerReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        stopService(new Intent(this, DistanceCalc.class));
        unregisterReceiver(DistanceBroadcastReceiver);
        stopService(new Intent(this, MagRead.class));
        unregisterReceiver(MagnetometerReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause(){
        finish();
        super.onPause();
    }

    protected void completeRouting(){
        graph = Graph.getInstance();
        shortestPath = getShortestPath();
        currentIndex = 0;
        startService(new Intent(this, DistanceCalc.class));
        registerReceiver(DistanceBroadcastReceiver, new IntentFilter("DistanceCalc"));
        startService(new Intent(this, MagRead.class));
        registerReceiver(MagnetometerReceiver, new IntentFilter("MagRead"));
        startService(new Intent(this, RSSICalculator.class));
        registerReceiver(RSSIBroadcastReceiver, new IntentFilter(Helper.RSSICalculator_Broadcast));
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

    private BroadcastReceiver RSSIBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getRSSIData(intent);
            checkCondition();
        }
    };

    private void getRSSIData(Intent intent){
        Bundle extras = intent.getExtras();
        assert extras != null;
        if((System.currentTimeMillis() - extras.getLong(Helper.TimeStamp))/1000 < 10) {
            RSSISSIDs = new HashMap<>();
            RSSIDataMap = new HashMap<>();
            int[] RSSI = extras.getIntArray(Helper.RSSI);
            String[] BSSID = extras.getStringArray(Helper.BSSID);
            String[] SSID = extras.getStringArray(Helper.SSID);
            assert RSSI != null;
            for (int i = 0; i < RSSI.length; i++) {
                assert BSSID != null;
                assert SSID != null;
                RSSISSIDs.put(BSSID[i], SSID[i]);
                RSSIDataMap.put(BSSID[i], RSSI[i]);
            }
            isRSSIDataSet = true;
        }
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

    private BroadcastReceiver MagnetometerReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            int degree = intent.getIntExtra("Degree", 0);
            DegreeTV.setText("Heading towards " + degree + " degrees N");
            RotateAnimation ra = new RotateAnimation( DegreeStart, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            ra.setFillAfter(true);
            ra.setDuration(210);
            compassimage.startAnimation(ra);
            DegreeStart = -degree;
        }
    };

    private boolean compareWifi(Map<String, Integer> wifi){
        return true;
    }

    private void checkCondition(){
        if(distanceToNextNode <= 0.05*totaldistance){
            if(isRSSIDataSet){
                //Compare RSSI values and start routing for next pair of nodes
                Map<String, Integer> wifi = null;
                String nextnode = shortestPath.get(currentIndex).getEn();
                ArrayList<Node> nodes = graph.getNodes();
                for(Node i : nodes){
                    if(i.getNodeName().equals(nextnode)){
                        wifi = i.getWifi();
                    }
                }
                if(compareWifi(wifi)){
                    startRouting(currentIndex++);
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    protected void startRouting(int currentIndex) {
        if(currentIndex==shortestPath.size()){
            stopService(new Intent(this, MagRead.class));
            unregisterReceiver(MagnetometerReceiver);
            stopService(new Intent(this, DistanceCalc.class));
            unregisterReceiver(DistanceBroadcastReceiver);
            compassimage.setVisibility(View.INVISIBLE);
            DegreeTV.setText("Routing completed");
        }else {
            distanceToNextNode = shortestPath.get(currentIndex).getDistance();
            totaldistance = distanceToNextNode;
        }
    }
}