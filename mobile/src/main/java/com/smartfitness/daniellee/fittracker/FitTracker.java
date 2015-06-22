package com.smartfitness.daniellee.fittracker;

import android.app.Application;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentPagerAdapter;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by danie_000 on 4/17/2015.
 */
public class FitTracker extends Application{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    static SharedPreferences mSettings;

    public static final String PREFS_NAME = "MyPrefsData";

    @Override
    public void onCreate() {
        super.onCreate();

        mSettings = getSharedPreferences(PREFS_NAME, 0);

        try {
            ParseObject.registerSubclass(Run.class);
            ParseObject.registerSubclass(Sleep.class);

            Parse.enableLocalDatastore(this);

            Parse.initialize(this, "lz7HKIzCQV2i7x0YtxbxgYGuXd0P4phmYXDm292d", "pguHKsDRwYhB8iZiD8ntv5XE8VTPmR0t2e4n3U0V");
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }
}
