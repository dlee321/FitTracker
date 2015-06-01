package com.smartfitness.daniellee.fittracker;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by danie_000 on 4/18/2015.
 */
@ParseClassName("Run")
public class Run extends ParseObject {

    public static final byte WALKING = 0;
    public static final byte RUNNING = 1;

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
        return this.getInt(Keys.ACTIVITY_TYPE);
    }

    public void setActivityType(byte activityType) {
        //this.activityType = activityType;
        this.put(Keys.ACTIVITY_TYPE, activityType);
    }

    public String getDescription() {
        return this.getString(Keys.DESCRIPTION);
    }

    public void setDescription(String description) {
        this.put(Keys.DESCRIPTION, description);
    }

    public String getNotes() {
        return this.getString(Keys.NOTES);
    }

    public void setNotes(String notes) {
        this.put(Keys.NOTES, notes);
    }

    public double getDistance() {
        return this.getDouble(Keys.DISTANCE);
    }

    public void setDistance(double distance) {
        this.put(Keys.DISTANCE, distance);
    }

    public ArrayList<JSONArray> getCoordinates() {
        return (ArrayList<JSONArray>) this.get(Keys.COORDINATES);
    }

    public boolean setCoordinates(ArrayList<double[]> coordinates) {
        int count = 1;
        Double[] coordinateD;
        try {
            for (double[] coordinate : coordinates) {
                coordinateD = new Double[2];
                coordinateD[0] = coordinate[0];
                coordinateD[1] = coordinate[1];
                Log.d("RUN", "" + count);
                this.add(Keys.COORDINATES, Arrays.asList(coordinateD));
            }
        } catch (OutOfMemoryError e) {
            return true;
        }
        return false;
    }


    public double getCalories() {
        return this.getDouble(Keys.CALORIES);
    }

    public void setCalories(double calories) {
        this.put(Keys.CALORIES, calories);
    }

    public long getStartTime() {
        return this.getLong(Keys.START_TIME);
    }

    public void setStartTime(long startTime) {
        this.put(Keys.START_TIME, startTime);
    }

    public long getEndTime() {
        return this.getLong(Keys.END_TIME);
    }

    public void setEndTime(long endTime) {
        this.put(Keys.END_TIME, endTime);
    }



}
