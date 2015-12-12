package com.smartfitness.daniellee.fittracker;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.app.FragmentTransaction;
import android.preference.PreferenceActivity;

/**
 * Created by danie_000 on 8/10/2015.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }
}
