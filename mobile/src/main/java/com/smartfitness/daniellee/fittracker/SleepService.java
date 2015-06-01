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

    SensorManager sensorManager;
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

    //private boolean notStartTracking = true;
    //private boolean nextStartTracking = false;
    private boolean secondMinute = true;

    private double minX;
    private double minY;
    private double minZ;

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

        // get calibrated values for minx, miny, and minz
        minX = Double.longBitsToDouble(MainActivity.mSettings.getLong(Keys.SENSORX, 0));
        minY = Double.longBitsToDouble(MainActivity.mSettings.getLong(Keys.SENSORY, 0));
        minZ = Double.longBitsToDouble(MainActivity.mSettings.getLong(Keys.SENSORZ, 0));

        Log.d(TAG, minX + " " + minY + " " + minZ);

        Notification note = new NotificationCompat.Builder(this)
                .setContentTitle("SleepTracker")
                .setContentText("Sleep tracking started!")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();
        startForeground(2014, note);

        sensorManager =(SensorManager)getSystemService(SENSOR_SERVICE);

        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this,
                mSensor,
                SensorManager.SENSOR_DELAY_FASTEST);


        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    // make sleep tracking only start after 1st full minute
                    /*if (notStartTracking) {
                        if (nextStartTracking) {
                            notStartTracking = false;
                            sw = new Stopwatch();
                        } else {
                            nextStartTracking = true;
                        }
                    } else {*/
                        // use secondMinute variable to make intervals 2 minutes
                        //if (secondMinute) {
                            //Log.d(TAG, "" + minuteMovement);
                            addTotalsToQueues();
                        //}
                        //secondMinute = !secondMinute;
                    //}
                }
            }
        };
        timer.scheduleAtFixedRate(new TimerTask() {
            // called every hour to reregister sensor
            @Override
            public void run() {
                sensorManager.unregisterListener(SleepService.this);
                sensorManager.registerListener(SleepService.this,
                        mSensor,
                        SensorManager.SENSOR_DELAY_FASTEST);
            }
        }, 36000000, 3600000);
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //if (!notStartTracking) {
            //if (sensorEvent.sensor.getType() == Sensor.) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
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
                if ((tempTime - mTime) > 1000 && (x > minX || y > minY || z > minZ)) {
                    Log.d(TAG, x + " " + y + " " + z);
                    minuteMovement++;
                    Log.d(TAG, "" + minuteMovement);
                    mTime = tempTime;
                }
            }
        //}
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
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