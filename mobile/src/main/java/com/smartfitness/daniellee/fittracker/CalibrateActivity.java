package com.smartfitness.daniellee.fittracker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;
import java.util.TimerTask;


public class CalibrateActivity extends ActionBarActivity {

    String alarmTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);

        Intent intent = getIntent();
        if (intent != null) {
            alarmTime = intent.getStringExtra(SleepFragment.ALARM_TIME_TAG);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Calibrate Sensors")
                .setMessage("Please place your phone face down on your bed and don't move it. Your phone will vibrate when calibration is finished.")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startCalibration();
                    }
                });

        AlertDialog dialog;
        dialog = builder.create();
        dialog.show();
    }

    private void startCalibration() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Calibrating...");
        progressDialog.show();
        final Intent serviceIntent = new Intent(this, CalibrationService.class);
        startService(serviceIntent);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopService(serviceIntent);
                progressDialog.dismiss();
                final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                long [] pattern = {0,500, 500};
                vibrator.vibrate(pattern, 0);
               final  AlertDialog.Builder builder = new AlertDialog.Builder(CalibrateActivity.this);
                builder.setTitle("Calibration Finished")
                        .setMessage("Would you like to recalibrate or begin sleep tracking?")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                vibrator.cancel();
                                Intent sleepIntent = new Intent(CalibrateActivity.this, SleepActivity.class);
                                sleepIntent.putExtra(SleepFragment.ALARM_TIME_TAG, alarmTime);
                                startActivity(sleepIntent);
                                finish();
                            }
                        })
                        .setNegativeButton("Recalibrate", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                vibrator.cancel();
                                startCalibration();
                            }
                        });
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            }
        }, 30000);
    }
}
