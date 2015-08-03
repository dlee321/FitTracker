package com.smartfitness.daniellee.fittracker;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by danie_000 on 4/18/2015.
 */
@ParseClassName("Run")
public class Run extends ParseObject {

    public static final byte WALKING = 0;
    public static final byte RUNNING = 1;
    public static final byte CYCLING = 2;

    /*private ArrayList<double[]> coordinates;
    private double calories;
    private long startTime;
    private long endTime;
    private double distance;
    private String description;
    private String notes;

    private byte activityType;*/

    public Run() {

    }

    public int getActivityType() {
        return this.getInt(Constants.ACTIVITY_TYPE);
    }

    public void setActivityType(byte activityType) {
        //this.activityType = activityType;
        this.put(Constants.ACTIVITY_TYPE, activityType);
    }

    public String getDescription() {
        return this.getString(Constants.DESCRIPTION);
    }

    public void setDescription(String description) {
        this.put(Constants.DESCRIPTION, description);
    }

    public String getNotes() {
        return this.getString(Constants.NOTES);
    }

    public void setNotes(String notes) {
        this.put(Constants.NOTES, notes);
    }

    public double getDistance() {
        return this.getDouble(Constants.DISTANCE);
    }

    public void setDistance(double distance) {
        this.put(Constants.DISTANCE, distance);
    }

    public JSONArray getCoordinates() {
        return (JSONArray) this.get(Constants.COORDINATES);
    }

    public void setCoordinates(ArrayList<Double> coordinates) {
        int count = 1;
        try {
            JSONArray coordinatesArray = new JSONArray();
            for (double coordinate : coordinates) {
                coordinatesArray.put(coordinate);
            }
            this.put(Constants.COORDINATES, coordinatesArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public double getCalories() {
        return this.getDouble(Constants.CALORIES);
    }

    public void setCalories(double calories) {
        this.put(Constants.CALORIES, calories);
    }

    public long getStartTime() {
        return this.getLong(Constants.START_TIME);
    }

    public void setStartTime(long startTime) {
        this.put(Constants.START_TIME, startTime);
    }

    public long getEndTime() {
        return this.getLong(Constants.END_TIME);
    }

    public void setEndTime(long endTime) {
        this.put(Constants.END_TIME, endTime);
    }


}
