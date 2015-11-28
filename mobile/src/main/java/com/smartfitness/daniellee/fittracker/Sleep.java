package com.smartfitness.daniellee.fittracker;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by danie_000 on 5/22/2015.
 */
@ParseClassName("Sleep")
public class Sleep extends ParseObject{

    public Sleep () {

    }

    public void setStart(String start) {
        this.put(Constants.START, start);
    }

    public String getStart() {
        return this.getString(Constants.START);
    }

    public void setEnd(String end) {
        this.put(Constants.END, end);
    }

    public String getEnd() {
        return this.getString(Constants.END);
    }

    public void setValues(JSONArray values) {
        this.put(Constants.VALUES, values);
    }

    public JSONArray getValues() {
        return this.getJSONArray(Constants.VALUES);
    }

    public void setDuration(int duration) {
        this.put(Constants.DURATION, duration);
    }

    public int getDuration() {
        return this.getInt(Constants.DURATION);
    }

    public void setDeepSleepDuration(int duration) {
        this.put(Constants.DEEP_SLEEP_DURATION, duration);
    }

    public int getDeepSleepDuration() {
        return this.getInt(Constants.DEEP_SLEEP_DURATION);
    }
}
