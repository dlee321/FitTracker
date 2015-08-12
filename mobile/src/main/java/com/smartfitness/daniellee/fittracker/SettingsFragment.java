package com.smartfitness.daniellee.fittracker;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {


    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load preferences
        addPreferencesFromResource(R.xml.preference_screen);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Log.d("TAG", "onSharedPreferenceChanged key: " + key);
        if (key.equals("pref_stepsGoal")) {
            String stepsGoal = sharedPreferences.getString(key, "");
            FitTracker.mSettings.edit().putString("pref_stepsGoal", stepsGoal).apply();
        } else if (key.equals("pref_vibrateAlarm")) {
            boolean vibrate = sharedPreferences.getBoolean(key, false);
            FitTracker.mSettings.edit().putBoolean("pref_vibrateAlarm", vibrate).apply();
        }
    }
}
