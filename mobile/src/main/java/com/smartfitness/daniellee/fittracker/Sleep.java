package com.smartfitness.daniellee.fittracker;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.security.spec.KeySpec;
import java.util.ArrayList;

/**
 * Created by danie_000 on 5/22/2015.
 */
@ParseClassName("Sleep")
public class Sleep extends ParseObject{

    public Sleep () {

    }

    public void setStart(String start) {
        this.put(Keys.START, start);
    }

    public String getStart() {
        return this.getString(Keys.START);
    }

    public void setEnd(String end) {
        this.put(Keys.END, end);
    }

    public String getEnd() {
        return this.getString(Keys.END);
    }

    public void setValues(ArrayList<Boolean> values) {
        this.put(Keys.VALUES, values);
    }

    public ArrayList<Boolean> getValues() {
        return (ArrayList<Boolean>) this.get(Keys.VALUES);
    }
}
