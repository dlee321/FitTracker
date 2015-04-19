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
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class SleepService extends Service implements SensorEventListener {

    public static final String TAG = SleepService.class.getSimpleName();

    SensorManager sensorManagerGyroscope;
    Sensor mSensor;

    private Stopwatch sw;

    private double minuteMovement = 0;
    private double totalMovement = 0;
    Queue gyroData = new Queue();
    //Queue accelerometerAccuracy = new Queue();

    private BroadcastReceiver broadcastReceiver;
    private int sleepingTimeMinutes;
    private double maxMovement = 0;
    private double averageMovement = 0;
    private double minMovement = 0;
    private boolean isFirstData = true;

    Timer timer;

    private long mTime = 0;

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

        sensorManagerGyroscope =(SensorManager)getSystemService(SENSOR_SERVICE);

        mSensor = sensorManagerGyroscope.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManagerGyroscope.registerListener(this,
                mSensor,
                SensorManager.SENSOR_DELAY_FASTEST);


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    addTotalsToQueues();
                }
            }
        };
        timer.scheduleAtFixedRate(new TimerTask() {
            // called every hour to reregister sensor
            @Override
            public void run() {
                sensorManagerGyroscope.unregisterListener(SleepService.this);
                sensorManagerGyroscope.registerListener(SleepService.this,
                        mSensor,
                        SensorManager.SENSOR_DELAY_FASTEST);
            }
        }, 36000000, 3600000);
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType()==Sensor.TYPE_GYROSCOPE) {
            double x = sensorEvent.values[0];
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
            long tempTime = System.currentTimeMillis();
            if ((x > 0.2 || y > 0.2 || z > 0.2) && (tempTime - mTime) > 1000) {
                Log.d(TAG, "" + minuteMovement);
                minuteMovement++;
                mTime = tempTime;
            }
        }
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        if (sensorManagerGyroscope != null) {
            sensorManagerGyroscope.unregisterListener(this);
        }
        double [] gyroDataArray = new double[gyroData.size()];
        for (int iii = 0; iii < gyroDataArray.length; iii++) {
            Double data = (Double) gyroData.dequeue();
            Log.d(TAG, "" + data);
            gyroDataArray[iii] = data;
        }
        sleepingTimeMinutes = sw.elapsedTime();
        averageMovement = totalMovement/sleepingTimeMinutes;
        Intent intent = new Intent(this, SleepDataActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SleepDataActivity.EXTRA_DOUBLE, averageMovement);
        intent.putExtra(SleepDataActivity.EXTRA_INT, sleepingTimeMinutes);
        intent.putExtra(SleepDataActivity.EXTRA_ARRAY, gyroDataArray);
        //intent.putExtra(SleepDataActivity.EXTRA_ACCURACY_ARRAY, accelerometerAccuracyArray);
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
        /*if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            accelerometerAccuracy.enqueue(i);
        }*/
    }

    public void addTotalsToQueues() {
        Log.d(TAG, "addTotalsToQueues: " + minuteMovement);
        totalMovement += minuteMovement;
        gyroData.enqueue(minuteMovement);
        if (minuteMovement > maxMovement) {
            maxMovement = minuteMovement;
        }
        if (isFirstData) {
            minMovement = minuteMovement;
            isFirstData = false;
        }
        else if (minuteMovement < minMovement) {
            minMovement = minuteMovement;
        }
        minuteMovement = 0;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}