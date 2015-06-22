package com.smartfitness.daniellee.fittracker;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class CalibrationService extends Service implements SensorEventListener {

    SensorManager sensorManager;

    Sensor mSensor;

    double maxX = 0;
    double maxY = 0;
    double maxZ = 0;

    double maxX2 = 0;
    double maxY2 = 0;
    double maxZ2 = 0;

    public CalibrationService() {
    }

    @Override
    public void onCreate() {
        sensorManager =(SensorManager)getSystemService(SENSOR_SERVICE);

        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this,
                mSensor,
                SensorManager.SENSOR_DELAY_FASTEST);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                maxX2 = maxX;
                maxY2 = maxY;
                maxZ2 = maxZ;

                maxX = 0;
                maxY = 0;
                maxZ = 0;
            }
        }, 15000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            double x = Math.abs(sensorEvent.values[0]);
            double y = Math.abs(sensorEvent.values[1]);
            double z = Math.abs(sensorEvent.values[2]);

            if (x >  maxX) {
                maxX = x;
            }
            if (y > maxY) {
                maxY = y;
            }
            if (z > maxZ) {
                maxZ = z;
            }
        }
    }

    @Override
    public void onDestroy() {
        if (maxX > maxX2) {
            maxX = maxX2;
        }
        if (maxY > maxY2) {
            maxY = maxY2;
        }
        if (maxZ > maxZ2) {
            maxZ = maxZ2;
        }
        Log.d("Calibration", maxX + " " + maxY + " " + maxZ);

        SharedPreferences.Editor editor = FitTracker.mSettings.edit();
        editor.putLong(Constants.SENSORX, Double.doubleToRawLongBits(maxX * 2));
        editor.putLong(Constants.SENSORY, Double.doubleToRawLongBits(maxY * 2));
        editor.putLong(Constants.SENSORZ, Double.doubleToRawLongBits(maxZ * 4));
        editor.putInt(Constants.DAYS_CALIBRATED, 0);
        editor.apply();

        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
