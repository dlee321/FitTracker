package com.smartfitness.daniellee.fittracker;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.ArrayList;

/**
 * Created by danie_000 on 4/18/2015.
 */
@ParseClassName("Run")
public class Run extends ParseObject {

    private ArrayList<double[]> coordinates;
    private double calories;
    private long startTime;
    private long endTime;
    private double distance;

    public Run() {

    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setCoordinates(ArrayList<double[]> coordinates) {
        this.coordinates = coordinates;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public double getCalories() {
        return calories;
    }

    public ArrayList<double[]> getCoordinates() {
        return coordinates;
    }


    public long getEndTime() {
        return endTime;
    }

    public long getStartTime() {
        return startTime;
    }

}
