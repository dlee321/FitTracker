package com.smartfitness.daniellee.fittracker;


import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {


    private ArrayList<String> soundList;
    private ArrayList<String> absolutePathList;
    MediaPlayer mMediaPlayer;

    ProgressDialog mProgress;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgress = new ProgressDialog(getActivity());
        mProgress.setMessage("Loading settings...");
        mProgress.show();

        // load preferences
        addPreferencesFromResource(R.xml.preference_screen);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);


        ActionBar bar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowTitleEnabled(true);
        bar.setTitle("Settings");

        soundList = new ArrayList<>();
        absolutePathList = new ArrayList<>();

        new GetAlarmSoundsAsyncTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
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
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
        }
    }

    public void findSoundFiles(File dir) {
        String msqPattern = ".mp3";// Can include more strings for more extensions and check it.

        File[] listFile = dir.listFiles();

        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {

                if (listFile[i].isDirectory()) {
                    findSoundFiles(listFile[i]);
                } else {
                    if (listFile[i].getName().endsWith(msqPattern)) {
                        soundList.add(listFile[i].getName());
                        absolutePathList.add(listFile[i].getAbsolutePath());
                    }
                }
            }
        }
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Log.d("TAG", "onSharedPreferenceChanged key: " + key);
        switch (key) {
            case "pref_stepsGoal":
                String stepsGoal = sharedPreferences.getString(key, "");
                FitTracker.mSettings.edit().putString(key, stepsGoal).apply();
                break;
            case "pref_vibrateAlarm":
                boolean vibrate = sharedPreferences.getBoolean(key, false);
                FitTracker.mSettings.edit().putBoolean(key, vibrate).apply();
                break;
            case "pref_alarmSound":
                String entryValue = sharedPreferences.getString(key, "android.resource://com.smartfitness.daniellee.fittracker/raw/" + R.raw.alarm);
                Log.d("SettingsFragment", "Value: " + entryValue);
                FitTracker.mSettings.edit().putString(key, entryValue).apply();
                /*if (mMediaPlayer == null) {
                    mMediaPlayer = new MediaPlayer();
                }
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                }
                try {
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                    mMediaPlayer.setDataSource(getActivity(), Uri.parse(entryValue));
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                break;
        }
    }

    public class GetAlarmSoundsAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            findSoundFiles(Environment.getExternalStorageDirectory());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            final ListPreference listPreference = (ListPreference) findPreference("pref_alarmSound");
            soundList.add(0, "Default");
            absolutePathList.add(0, "android.resource://com.smartfitness.daniellee.fittracker/raw/" + R.raw.alarm);
            listPreference.setEntries(soundList.toArray(new String[0]));
            listPreference.setEntryValues(absolutePathList.toArray(new String[0]));
            if (listPreference.getValue() == null) {
                listPreference.setValueIndex(0);
            } else {
                listPreference.setValue(listPreference.getValue());
            }

            mProgress.dismiss();
        }
    }
}
