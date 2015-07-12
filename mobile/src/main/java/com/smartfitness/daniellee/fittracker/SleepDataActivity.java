package com.smartfitness.daniellee.fittracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.parse.ParseACL;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;


public class SleepDataActivity extends ActionBarActivity {

    public static final String EXTRA_DOUBLE = "extra_double";
    public static final String EXTRA_INT = "extra_int";
    public static final String EXTRA_ARRAY = "extra_array";
    //public static final String EXTRA_ACCURACY_ARRAY = "extra_array";
    public static final String EXTRA_START_TIME = "extra_start_time";

    private double averageMovement = 0.0;
    private int sleepTimeMinutes = 0;
    private double[] movementArray = null;

    //private int[] accuracyArray = null;

    private long startTime;

    private TextView totalSleepTextView;
    private TextView deepSleepTextView;


    // start and end times
    String startText;
    String endText;


    JSONArray values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep_data);

        Intent intent = this.getIntent();
        if (intent != null) {
            averageMovement = intent.getDoubleExtra(EXTRA_DOUBLE, 0.0);
            sleepTimeMinutes = intent.getIntExtra(EXTRA_INT, 0);
            movementArray = intent.getDoubleArrayExtra(EXTRA_ARRAY);
            startTime = intent.getLongExtra(EXTRA_START_TIME, 0);

            //accuracyArray = intent.getIntArrayExtra(EXTRA_ACCURACY_ARRAY);
        }

        totalSleepTextView = (TextView) findViewById(R.id.sleepTimeTextView);
        deepSleepTextView = (TextView) findViewById(R.id.deepSleepTextView);

        String sleepText = calculateTimeString(sleepTimeMinutes);
        totalSleepTextView.setText(sleepText);


        // create calendar to get hour and minute for start time
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startTime);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        startText = hour + ":";
        if (minute >= 10) {
            startText += minute;
        } else {
            startText += "0" + minute;
        }

        c.setTimeInMillis(startTime + sleepTimeMinutes * 60000);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        endText = hour + ":";
        if (minute >= 10) {
            endText += minute;
        } else {
            endText += "0" + minute;
        }

        int minutes = 0;
        boolean  lightSleep = false;
        GraphView.GraphViewData[] data;
        data = new GraphView.GraphViewData[movementArray.length];
        for (int iii = 0; iii < data.length; iii++) {
            /*if (movementArray[iii] < averageMovement * .4) {
                data[iii] = new GraphView.GraphViewData(iii + 1, 1);
            } else {
                data[iii] = new GraphView.GraphViewData(iii + 1, 2);
            }*/

            //data[iii] = new GraphView.GraphViewData(iii + 1, movementArray[iii]);

            if (movementArray[iii] > 0) {
                data[iii] = new GraphView.GraphViewData(iii + 1, 2);
                if (minutes > 0) {
                    for (int jjj = 1; jjj <= minutes; jjj++) {
                        data[iii-jjj] = new GraphView.GraphViewData(iii - jjj + 1, 2);
                    }
                }
                lightSleep = true;
                minutes = 0;

            } else {
                if (lightSleep) {
                    minutes++;
                }
                if (minutes > 6) {
                    minutes = 0;
                    lightSleep = false;
                }
                data[iii] = new GraphView.GraphViewData(iii + 1, 1);
            }
        }

        boolean deepSleep = false;
        minutes = 0;
        for (int iii = 0; iii < data.length; iii++) {
            if (data[iii].getY() == 2) {
                if (deepSleep) {
                    minutes++;
                    if (minutes > 5) {
                        minutes = 0;
                        deepSleep = false;
                    }
                }
            } else {
                if (minutes > 0) {
                    for (int jjj = 1; jjj <= minutes; jjj++) {
                        data[iii-jjj] = new GraphView.GraphViewData(iii - jjj + 1, 1);
                    }
                }
                deepSleep = true;
                minutes = 0;
            }
        }

        GraphViewSeries series = new GraphViewSeries(data);

        // calculate deep sleep time hours:mins
        int deepSleepMins = 0;
        // get values to be passed to Sleep ParseObject
        values = new JSONArray();
        boolean everyOther = false;
        for (GraphView.GraphViewData d: data) {
            if (d.getY() == 1) {
                deepSleepMins++;
                if (everyOther) {
                    values.put(false);
                }
            } else {
                if (everyOther) {
                    values.put(true);
                }
            }
            everyOther = !everyOther;
        }

        // double because each data point is 2 minutes
        //deepSleepMins *= 2;
        //Log.d("SleepDataActivity", deepSleepMins + "");
        String deepSleepString = calculateTimeString(deepSleepMins);
        deepSleepTextView.setText(deepSleepString);


        //GraphView graphView = new LineGraphView(this, "");
        GraphView graphView = new BarGraphView(this, "");
        graphView.addSeries(series);

        graphView.setHorizontalLabels(new String[]{startText,"","","","","","","","","", endText});
        graphView.setManualMinY(true);
        graphView.setManualYMinBound(0);

        graphView.setVerticalLabels(new String[]{""});

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        int dpHeight = displayMetrics.heightPixels;

        LinearLayout graphLayout = (LinearLayout)findViewById(R.id.graphLinearLayout2);
        ViewGroup.LayoutParams params = graphLayout.getLayoutParams();

        params.height = (dpHeight / 6);


        graphLayout.addView(graphView);
    }

    private String calculateTimeString(int minutes) {
        int hours = minutes / 60;
        int addedMinutes = minutes % 60;
        String sleepText = hours + ":";
        if (addedMinutes >= 10) {
            sleepText += addedMinutes;
        } else {
            sleepText += "0" + addedMinutes;
        }
        return sleepText;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_run_data, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // save sleep on parse
            ParseUser user = ParseUser.getCurrentUser();
            Sleep sleep = new Sleep();
            sleep.setStart(startText);
            sleep.setEnd(endText);
            sleep.setValues(values);
            sleep.setACL(new ParseACL(user));
            user.add(Constants.SLEEPS_KEY, sleep);
            user.saveInBackground();
            returnToMainActivity();
        } else if (item.getItemId() == R.id.action_discard) {
            returnToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void returnToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
