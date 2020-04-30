package com.example.indoornav;

import org.json.JSONException;
import org.json.JSONObject;

public class Edge {
    private String st;
    private String en;
    private float distance;
    private float direction;

    public Edge(JSONObject jsonObject) throws JSONException {
        this.st = jsonObject.getString("st");
        this.en = jsonObject.getString("en");
        this.distance = (float) jsonObject.getDouble("distance");
        this.direction = (float) jsonObject.getDouble("direction");
    }

    public Edge(String st, String en, float distance){
        this.st = st;
        this.en = en;
        this.distance = distance;
        this.direction = 0;
    }

    public String getSt() {
        return st;
    }

    public void setSt(String st) {
        this.st = st;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getDirection() {
        return direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }
}
