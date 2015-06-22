package com.smartfitness.daniellee.fittracker;

import com.parse.ParseClassName;
import com.parse.ParseObject;

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

    public void setValues(ArrayList<Boolean> values) {
        this.put(Constants.VALUES, values);
    }

    public ArrayList<Boolean> getValues() {
        return (ArrayList<Boolean>) this.get(Constants.VALUES);
    }
}
