package com.smartfitness.daniellee.fittracker;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;

public class SleepHistoryActivity extends AppCompatActivity {

    public static final String TAG = SleepHistoryActivity.class.getSimpleName();

    ProgressDialog mProgress;

    TextView noHistoryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.sleep_history_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Sleep History");

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Loading Sleep History...");
        mProgress.show();

        noHistoryTextView = (TextView) findViewById(R.id.no_sleep_history);

        ParseUser user = ParseUser.getCurrentUser();
        ArrayList<Sleep> sleepData = (ArrayList<Sleep>) user.get(Constants.SLEEPS_KEY);
        Log.d(TAG, "" + sleepData.size());
        if (sleepData.size() != 0) {
            new GetSleepHistoryTask().execute(sleepData);
        } else {
            noHistoryTextView.setVisibility(View.VISIBLE);
        }
    }

    private class GetSleepHistoryTask extends AsyncTask<ArrayList, Void, ArrayList<Sleep>> {

        @Override
        protected ArrayList doInBackground(ArrayList... params) {
            ArrayList<Sleep> sleepData = params[0];
            ArrayList<Sleep> sleeps = new ArrayList<>();

            ParseQuery<Sleep> query = ParseQuery.getQuery(Sleep.class);
            for (Sleep s : sleepData) {
                try {
                    Sleep sleep = query.get(s.getObjectId());
                    sleeps.add(sleep);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            return sleeps;
        }

        @Override
        protected void onPostExecute(ArrayList<Sleep> arrayList) {
            int length = arrayList.size();
            Log.d(TAG, "" + length);
            if (length > 0) {
                int[] duration = new int[length];
                int[] deepSleep = new int[length];
                Date[] dates = new Date[length];
                int iii = 0;
                for (Sleep sleep : arrayList) {
                    duration[iii] = sleep.getDuration();
                    deepSleep[iii] = sleep.getDeepSleepDuration();
                    dates[iii] = sleep.getCreatedAt();
                    iii++;
                }
                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.sleep_history_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(SleepHistoryActivity.this));
                SleepHistoryAdapter adapter = new SleepHistoryAdapter(duration, deepSleep, dates, getApplicationContext());
                recyclerView.setAdapter(adapter);
            } else {
                noHistoryTextView.setVisibility(View.VISIBLE);
            }

            mProgress.dismiss();
        }
    }

}
