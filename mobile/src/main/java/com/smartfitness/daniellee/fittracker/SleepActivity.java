package com.smartfitness.daniellee.fittracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;


public class SleepActivity extends ActionBarActivity {

    protected static final String ALARM_TIME_TAG = "alarmtime";

    TextView timeTextView;
    Button doneSleepingButton;

    BroadcastReceiver br;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String alarmTime = "";
        Intent i = getIntent();
        if (i != null) {
            alarmTime = i.getStringExtra(ALARM_TIME_TAG);
        }
        setContentView(R.layout.activity_sleep);
        final Intent serviceIntent = new Intent(this, SleepService.class);
        serviceIntent.putExtra(ALARM_TIME_TAG, alarmTime);
        startService(serviceIntent);

        // make sure CPU doesn't sleep
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        wl.acquire();

        View v = findViewById(android.R.id.content);
        v.setBackgroundColor(getResources().getColor(R.color.material_blue_grey_800));

        timeTextView = (TextView) findViewById(R.id.timeTextView);
        timeTextView.setText(new SimpleDateFormat("h:mm").format(new Date()));

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;

        timeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, width / 3);
        timeTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    timeTextView.setText(new SimpleDateFormat("h:mm").format(new Date()));
                }
            }
        };

        registerReceiver(br, new IntentFilter(Intent.ACTION_TIME_TICK));

        doneSleepingButton = (Button) findViewById(R.id.doneSleepingButton);
        doneSleepingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wl.release();
                stopService(serviceIntent);
            }
        });
    }

    @Override
    protected void onStop() {
        unregisterReceiver(br);
        super.onStop();
    }
}
