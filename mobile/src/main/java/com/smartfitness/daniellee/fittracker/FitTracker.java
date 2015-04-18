package com.smartfitness.daniellee.fittracker;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by danie_000 on 4/17/2015.
 */
public class FitTracker extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            ParseObject.registerSubclass(Run.class);

            Parse.enableLocalDatastore(this);

            Parse.initialize(this, "lz7HKIzCQV2i7x0YtxbxgYGuXd0P4phmYXDm292d", "pguHKsDRwYhB8iZiD8ntv5XE8VTPmR0t2e4n3U0V");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }
}
