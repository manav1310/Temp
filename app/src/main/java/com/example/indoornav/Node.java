package com.example.indoornav;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Node {
    private String nodeName;
    private String nodeDescription;
    private Map<String, Integer> wifi = new HashMap<String, Integer>();
    private Boolean isBreadcrumb;


    public Node(JSONObject jsonObject) throws JSONException {
        this.nodeName = jsonObject.getString("nodeName");
        this.nodeDescription = jsonObject.getString("nodeDescription");
        this.isBreadcrumb = jsonObject.getBoolean("isBreadcrumb");
        try {
            JSONObject wifiJSONObject = jsonObject.getJSONObject("wifi");
            Iterator<String> keys = wifiJSONObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                wifi.put(key, wifiJSONObject.getInt(key));
            }
        }catch (JSONException ignored) { ; }
    }

    public Node(String nodeName,String nodeDescription,Map<String, Integer> wifi,Boolean isBreadcrumb){
        this.isBreadcrumb = isBreadcrumb;
        this.nodeName = nodeName;
        this.nodeDescription = nodeDescription;
        this.wifi = wifi;
    }

    public Map getWifi() {
        return wifi;
    }

    public void setWifi(Map<String, Integer> wifi) {
        this.wifi = wifi;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeDescription() {
        return nodeDescription;
    }

    public void setNodeDescription(String nodeDescription) {
        this.nodeDescription = nodeDescription;
    }

    public Boolean getBreadcrumb() {
        return isBreadcrumb;
    }

    public void setBreadcrumb(Boolean breadcrumb) {
        isBreadcrumb = breadcrumb;
    }
}
