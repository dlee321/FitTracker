package com.smartfitness.daniellee.fittracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;


public class SleepActivity extends ActionBarActivity {

    TextView timeTextView;
    Button doneSleepingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);

        View v = findViewById(android.R.id.content);
        v.setBackgroundColor(getResources().getColor(R.color.material_blue_grey_800));

        timeTextView = (TextView) findViewById(R.id.timeTextView);
        timeTextView.setText(new SimpleDateFormat("h:mm").format(new Date()));

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = dm.widthPixels;

        timeTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, width / 3);
        timeTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        BroadcastReceiver br = new BroadcastReceiver() {
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
                stopService(MainActivity.getSleepFragment().getIntent());
            }
        });
    }

}
