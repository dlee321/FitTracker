package com.smartfitness.daniellee.fittracker;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.ArrayList;

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

    public byte getActivityType() {
        return (Byte) this.get(Keys.ACTIVITY_TYPE);
    }

    public void setActivityType(byte activityType) {
        //this.activityType = activityType;
        this.add(Keys.ACTIVITY_TYPE, activityType);
    }

    public String getDescription() {
        return (String) this.get(Keys.DESCRIPTION);
    }

    public void setDescription(String description) {
        this.add(Keys.DESCRIPTION, description);
    }

    public String getNotes() {
        return (String) this.get(Keys.NOTES);
    }

    public void setNotes(String notes) {
        this.add(Keys.NOTES, notes);
    }

    public double getDistance() {
        return (Double) this.get(Keys.DISTANCE);
    }

    public void setDistance(double distance) {
        this.add(Keys.DISTANCE, distance);
    }

    public ArrayList<double[]> getCoordinates() {
        return (ArrayList<double[]>) this.get(Keys.COORDINATES);
    }

    public void setCoordinates(ArrayList<double[]> coordinates) {
        this.add(Keys.COORDINATES, coordinates);
    }

    public double getCalories() {
        return (Double) this.get(Keys.CALORIES);
    }

    public void setCalories(double calories) {
        this.add(Keys.CALORIES, calories);
    }

    public long getStartTime() {
        return (Long) this.get(Keys.START_TIME);
    }

    public void setStartTime(long startTime) {
        this.add(Keys.START_TIME, startTime);
    }

    public long getEndTime() {
        return (Long) this.get(Keys.END_TIME);
    }

    public void setEndTime(long endTime) {
        this.add(Keys.END_TIME, endTime);
    }



}
