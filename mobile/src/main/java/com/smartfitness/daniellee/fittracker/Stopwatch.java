package com.smartfitness.daniellee.fittracker;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by daniellee on 1/2/15.
 */
public class Stopwatch {

    private final long start;

    /**
     * Create a stopwatch object.
     */
    public Stopwatch() {
        start = System.currentTimeMillis();
    }


    /**
     * Return elapsed time (in minutes) since this object was created.
     */
    public int elapsedTime() {
        long now = System.currentTimeMillis();
        BigDecimal bg = new BigDecimal((now - start) / 60000.0);
        bg = bg.setScale(0, RoundingMode.HALF_UP);
        int time = bg.intValue();
        return time;
    }

    public long getStart() {
        return start;
    }

}