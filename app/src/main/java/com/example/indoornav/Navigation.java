package com.example.indoornav;

import android.content.Intent;
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

public class Navigation extends AppCompatActivity {

    private ArrayList<Pair<String, String> > getshortestpath(String src, String des, String map, Graph graph){

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
        for (Pair i : result){
            System.out.println(i.first + " -> " + i.second);
        }

        return result;
    }

    protected void completerouting(String src, String des, String map){
        String EdgesFileContent = getFileContents(map + Helper.Edges);
        String NodesFileContent = getFileContents(map + Helper.Nodes);
        Graph graph = Graph.getInstance(map, NodesFileContent, EdgesFileContent);
        ArrayList<Pair<String, String> > shortestpath = getshortestpath(src, des, map, graph);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Intent intent = getIntent();
        String source = intent.getStringExtra(Helper.SourceNodeName);
        String dest   = intent.getStringExtra(Helper.DestinationNodeName);
        String map    = intent.getStringExtra(Helper.mapname);
        completerouting(source, dest, map);
        //Routing is completed
    }
}
