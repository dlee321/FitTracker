package com.smartfitness.daniellee.fittracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;

import java.util.Calendar;


public class SleepDataActivity extends ActionBarActivity {

    public static final String EXTRA_DOUBLE = "extra_double";
    public static final String EXTRA_INT = "extra_int";
    public static final String EXTRA_ARRAY = "extra_array";
    public static final String EXTRA_START_TIME = "extra_start_time";

    private double averageMovement = 0.0;
    private int sleepTimeMinutes = 0;
    private double[] movementArray = null;
    private long startTime;

    private TextView totalSleepTextView;
    private TextView deepSleepTextView;

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
        }

        totalSleepTextView = (TextView) findViewById(R.id.sleepTimeTextView);
        deepSleepTextView = (TextView) findViewById(R.id.deepSleepTextView);

        int sleepTimeHours = sleepTimeMinutes / 60;
        int addedMinutes = sleepTimeMinutes % 60;
        String sleepText = sleepTimeHours + ":";
        if (addedMinutes >= 10) {
            sleepText += addedMinutes;
        } else {
            sleepText += "0" + addedMinutes;
        }
        totalSleepTextView.setText(sleepText);


        // create calendar to get hour and minute for start time
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startTime);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        String startText = hour + ":";
        if (minute >= 10) {
            sleepText += minute;
        } else {
            sleepText += "0" + minute;
        }

        c.setTimeInMillis(startTime + sleepTimeMinutes * 60000);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        String endText = hour + ":";
        if (minute >= 10) {
            sleepText += minute;
        } else {
            sleepText += "0" + minute;
        }

        GraphView.GraphViewData[] data;
        data = new GraphView.GraphViewData[movementArray.length];
        for (int iii = 0; iii < data.length; iii++) {
            if (movementArray[iii] < averageMovement * .4) {
                data[iii] = new GraphView.GraphViewData(iii + 1, 1);
            } else {
                data[iii] = new GraphView.GraphViewData(iii + 1, 2);
            }
        }
        GraphViewSeries series = new GraphViewSeries(data);

        GraphView graphView = new BarGraphView(this, "");
        graphView.addSeries(series);

        graphView.setHorizontalLabels(new String[]{startText,"","","","","","","","","","","","","", endText});
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
