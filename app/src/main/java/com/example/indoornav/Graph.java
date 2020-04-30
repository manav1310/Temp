package com.example.indoornav;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Graph {

    private ArrayList<Edge> edges = new ArrayList<>();
    private ArrayList<Node> nodes = new ArrayList<>();
    private Map<String,ArrayList<Edge> > adjacencyList = new HashMap<String, ArrayList<Edge>>();
    private Gson gson = new Gson();
    private String mapName;
    private static Graph graph = null;

    public static Graph getInstance(String mapName, String nodesFileContent,String edgesFileContent) {
        if(graph == null)
            graph = new Graph(mapName, nodesFileContent, edgesFileContent);
        if(!mapName.equalsIgnoreCase(graph.getMapName()))
            graph = new Graph(mapName, nodesFileContent,edgesFileContent);
        return graph;
    }

    public static void destroyGraph(){
        graph = null;
    }

    public static Graph getInstance() {
        assert graph != null;
        return graph;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }
    public void addNodes(Node data) {
        this.nodes.add(data);
        adjacencyList.put(data.getNodeName(),new ArrayList<Edge>());
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }
    public void addEdges(Edge data) {
        this.edges.add(data);
        adjacencyList.get(data.getSt()).add(data);
    }

    public Map<String, ArrayList<Edge>> getAdjacencyList(){
        return adjacencyList;
    }

    private Graph(String mapName, String nodesFileContent, String edgesFileContent){
        this.mapName = mapName;
        if(nodesFileContent.isEmpty()) {
            return;
        }
        JSONArray nodesJsonArray = null;
        try {
            nodesJsonArray = new JSONArray(nodesFileContent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i=0; i < nodesJsonArray.length(); i++) {
            try {
                nodes.add(new Node(new JSONObject((String) nodesJsonArray.get(i))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JSONArray edgesJsonArray = null;
        try {
            edgesJsonArray = new JSONArray(edgesFileContent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i=0; i < edgesJsonArray.length(); i++) {
            try {
                edges.add(new Edge(new JSONObject((String) edgesJsonArray.get(i))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        setAdjacencyList();
    }

    private void setAdjacencyList() {
        for (Node node : nodes) {
            adjacencyList.put(node.getNodeName(), new ArrayList<Edge>());
        }
        for (Edge edge : edges ) {
            adjacencyList.get(edge.getSt()).add(edge);
        }
    }

    public JsonArray nodesToJson(){
        JsonArray nodesJsonArray = new JsonArray();
        for(Node node : nodes) {
            nodesJsonArray.add(gson.toJson(node));
            Log.d("node",gson.toJson(node));
        }
        return nodesJsonArray;
    }

    public JsonArray edgesToJson() {
        JsonArray edgesJsonArray = new JsonArray();
        for(Edge edge : edges) {
            edgesJsonArray.add(gson.toJson(edge));
        }
        return edgesJsonArray;
    }

    private String getMapName() {
        return mapName;
    }
}

