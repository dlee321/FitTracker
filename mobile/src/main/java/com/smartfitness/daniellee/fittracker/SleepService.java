package com.smartfitness.daniellee.fittracker;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class SleepService extends Service implements SensorEventListener {

    SensorManager sensorManagerAccelerometer;

    private Stopwatch sw;

    private double totalMovement1 = 0;
    private double totalMovement2 = 0;
    private double totalMovement = 0;
    Queue accelerometerData = new Queue();

    private BroadcastReceiver broadcastReceiver;
    private int sleepingTimeMinutes;
    private double maxMovement = 0;
    private double averageMovement = 0;
    private double minMovement = 0;
    private boolean isFirstData = true;

    Timer timer;

    public SleepService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        sw = new Stopwatch();

        timer = new Timer();

        Notification note = new NotificationCompat.Builder(this)
                .setContentTitle("SleepTracker")
                .setContentText("Sleep tracking started!")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();
        startForeground(2014, note);

        sensorManagerAccelerometer=(SensorManager)getSystemService(SENSOR_SERVICE);

        sensorManagerAccelerometer.registerListener(this,
                sensorManagerAccelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    addTotalsToQueues();
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            totalMovement1 = totalMovement2;
                            totalMovement2 = 0;
                            this.cancel();
                        }
                    };
                    timer.schedule(timerTask, 30000);
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
            /*double x = sensorEvent.values[0];
            if (x < 0) {
                x = -x;
            }
            double y = sensorEvent.values[1];
            if (y < 0) {
                y = -y;
            }
            double z = sensorEvent.values[2];
            if (z < 0) {
                z = -z;
            }
            totalMovement2 += (x + y + z);*/
            totalMovement2++;
        }
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        double [] accelerometerDataArray = new double[accelerometerData.size()];
        for (int iii = 0; iii < accelerometerData.size(); iii++) {
            accelerometerDataArray[iii] = (Double)accelerometerData.dequeue();
        }
        sleepingTimeMinutes = sw.elapsedTime();
        averageMovement = totalMovement/sleepingTimeMinutes;
        Intent intent = new Intent(this, SleepDataActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SleepDataActivity.EXTRA_DOUBLE, averageMovement);
        intent.putExtra(SleepDataActivity.EXTRA_INT, sleepingTimeMinutes);
        intent.putExtra(SleepDataActivity.EXTRA_ARRAY, accelerometerDataArray);
        intent.putExtra(SleepDataActivity.EXTRA_START_TIME, sw.getStart());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        /*if (hasTempSensor) {
            intent.putExtra(SleepDataActivity.EXTRA_QUEUE2, temperatureData);
        }
        if (hasHumiditySensor) {
            intent.putExtra(SleepDataActivity.EXTRA_QUEUE3, humidityData);
        }*/
        startActivity(intent);
        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void addTotalsToQueues() {
        double averageMovement = (totalMovement1 + totalMovement2) / 2;
        totalMovement += averageMovement;
        accelerometerData.enqueue(averageMovement);
        if (averageMovement > maxMovement) {
            maxMovement = averageMovement;
        }
        if (isFirstData) {
            minMovement = averageMovement;
            isFirstData = false;
        }
        else if (averageMovement < minMovement) {
            minMovement = averageMovement;
            sensorManagerAccelerometer.unregisterListener(this);
            sensorManagerAccelerometer.registerListener(this,
                    sensorManagerAccelerometer.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_FASTEST);
        }
        totalMovement1 = 0;
        totalMovement2 = 0;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}