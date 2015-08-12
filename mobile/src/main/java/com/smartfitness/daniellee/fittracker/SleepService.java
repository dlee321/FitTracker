package com.smartfitness.daniellee.fittracker;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
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
    private int sleepingTimeMinutes = 0;
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

    private String mAlarmTime;
    Calendar mAlarmCalendar;
    //MediaPlayer mMediaPlayer;

    private long mTime = 0;

    private Runnable alarmRunnable;
    Handler handler;
    AlertDialog dialog;
    PowerManager.WakeLock wl;

    private int currentLightSleepMins;

    public SleepService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*@Override
    public void onCreate() {

    }*/

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
        // increment days calibrated
        if (handler != null && alarmRunnable != null) {
            handler.removeCallbacks(alarmRunnable);
        }
        if (wl.isHeld()) {
            wl.release();
        }
        FitTracker.mSettings.edit().putInt(Constants.DAYS_CALIBRATED, FitTracker.mSettings.getInt(Constants.DAYS_CALIBRATED, 0) + 1).apply();
        if (broadcastReceiver != null) {
            try {
                unregisterReceiver(broadcastReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        double[] gyroDataArray = new double[gyroData.size()];
        for (int iii = 0; iii < gyroDataArray.length; iii++) {
            Double data = (Double) gyroData.dequeue();
            Log.d(TAG, "" + data);
            gyroDataArray[iii] = data;
        }
        if (sleepingTimeMinutes == 0) {
            sleepingTimeMinutes = sw.elapsedTime();
        }
        averageMovement = totalMovement / sleepingTimeMinutes;
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
        Log.d(TAG, "add totals to queue: " + minuteMovement);
        if (minuteMovement == 0) {
            currentLightSleepMins = 0;
        } else {
            currentLightSleepMins++;
        }
        totalMovement += minuteMovement;
        gyroData.enqueue(minuteMovement);
        if (minuteMovement > maxMovement) {
            maxMovement = minuteMovement;
        }
        if (isFirstData) {
            minMovement = minuteMovement;
            isFirstData = false;
        } else if (minuteMovement < minMovement) {
            minMovement = minuteMovement;
        }
        minuteMovement = 0;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "" + (intent == null));
        if (intent != null) {
            mAlarmTime = intent.getStringExtra(SleepActivity.ALARM_TIME_TAG);
        }

        sw = new Stopwatch();

        timer = new Timer();

        // get calibrated values for minx, miny, and minz
        minX = Double.longBitsToDouble(FitTracker.mSettings.getLong(Constants.SENSORX, 0));
        minY = Double.longBitsToDouble(FitTracker.mSettings.getLong(Constants.SENSORY, 0));
        minZ = Double.longBitsToDouble(FitTracker.mSettings.getLong(Constants.SENSORZ, 0));

        Log.d(TAG, minX + " " + minY + " " + minZ);

        Notification note = new NotificationCompat.Builder(this)
                .setContentTitle("SleepTracker")
                .setContentText("Sleep tracking started!")
                .setSmallIcon(R.drawable.ic_launcher)
                .build();
        startForeground(2014, note);

        // schedule alarm
        Date alarmDate = new Date();
        // get hour and minute of alarm time
        String[] splits = mAlarmTime.split(":");
        mAlarmCalendar = Calendar.getInstance();
        mAlarmCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splits[0]));
        mAlarmCalendar.set(Calendar.MINUTE, Integer.parseInt(splits[1]));
        mAlarmCalendar.set(Calendar.SECOND, 0);
        if (alarmDate.getTime() >= mAlarmCalendar.getTimeInMillis()) {
            Log.d(TAG, "Add day");
            mAlarmCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (!FitTracker.mSettings.getBoolean(Constants.DISABLE_ALARM, false)) {
            handler = new Handler();
            alarmRunnable = new Runnable() {
                @Override
                public void run() {
                    // stop recording sleep
                    if (broadcastReceiver != null) {
                        try {
                            unregisterReceiver(broadcastReceiver);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                    // wake-up phone
                    PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
                    final PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
                    wakeLock.acquire();
                    // unlock phone
                    KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
                    final KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
                    keyguardLock.disableKeyguard();
                    // play sounds
                    final MediaPlayer mMediaPlayer = new MediaPlayer();
                    try {
                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                        mMediaPlayer.setDataSource(SleepService.this, Uri.parse("android.resource://com.smartfitness.daniellee.fittracker/raw/" + R.raw.alarm));
                        mMediaPlayer.setLooping(true);
                        mMediaPlayer.prepare();
                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (FitTracker.mSettings.getBoolean("pref_vibrateAlarm", false)) {
                        final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        long[] pattern = {0, 500, 500};
                        vibrator.vibrate(pattern, 0);
                    }
                    // show wake up dialog
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SleepService.this);
                    dialog = dialogBuilder.setTitle("Wake Up!")
                            .setMessage("Good morning. Time to wake up!")
                            .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialog.dismiss();
                                    mMediaPlayer.stop();
                                    wakeLock.release();
                                    keyguardLock.reenableKeyguard();
                                    stopSelf();
                                }
                            })
                            .setPositiveButton("Snooze", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mMediaPlayer.stop();
                                    handler.postDelayed(alarmRunnable, 300000);
                                    dialog.dismiss();
                                    wakeLock.release();
                                    keyguardLock.reenableKeyguard();
                                }
                            })
                            .create();
                    dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    dialog.setCancelable(false);
                    dialog.show();

                    // set sleep duration to this time
                    if (sleepingTimeMinutes == 0) {
                        sleepingTimeMinutes = sw.elapsedTime();
                    }
                }
            };
            long timeDifference = new Date().getTime() - SystemClock.uptimeMillis();
            handler.postAtTime(alarmRunnable, mAlarmCalendar.getTimeInMillis() - timeDifference);
        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

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

                    Calendar c = Calendar.getInstance();
                    if (!FitTracker.mSettings.getBoolean(Constants.DISABLE_ALARM, false)) {
                        int smartAlarmTimeIndex = FitTracker.mSettings.getInt(Constants.SMART_ALARM_TIME_INDEX, 0);
                        if (!(smartAlarmTimeIndex== 5)) {
                            if ((mAlarmCalendar.getTimeInMillis() - c.getTimeInMillis()) < (Integer.parseInt(Constants.SMART_ALARM_TIMES[smartAlarmTimeIndex]) * 60000)) {
                                if (currentLightSleepMins >= 3) {
                                    alarmRunnable.run();
                                }
                            }
                        }
                    }
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

        // wakelock
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        wl.acquire();
        /*alarmDate.setTime(c.getTimeInMillis());
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getApplicationContext());
                dialogBuilder.setTitle("Wake Up!")
                        .setMessage("Good Morning! Time to wake up!")
                        .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .setPositiveButton("Snooze", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                timer.schedule(timerTask, 300000);
                            }
                        });
                mMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.start();
            }
        };
        timer.schedule(timerTask, alarmDate);*/

        return START_STICKY;
    }
}