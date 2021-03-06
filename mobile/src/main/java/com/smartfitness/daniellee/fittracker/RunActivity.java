package com.smartfitness.daniellee.fittracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class RunActivity extends AppCompatActivity implements LocationListener {

    public static final String TAG = RunActivity.class.getSimpleName();

    public static final int EARTH_RADIUS = 6371;

    private static final int REQUEST_OAUTH = 1;

    /**
     *  Track whether an authorization activity is stacking over the current activity, i.e. when
     *  a known auth error is being resolved, such as showing the account chooser or presenting a
     *  consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    MapFragment fragment;
    GoogleMap map;

    GoogleApiClient mGoogleApiClient;

    Location mLastLocation;
    Location mCurrentLocation;

    LocationRequest mLocationRequest;

    TextView mDistanceTextView;
    TextView mDurationTextView;
    TextView mCaloriesTextView;

    TextView stopTextView;
    TextView pauseTextView;

    TextView mPaceTextView;
    TextView mCurrentPaceTextView;

    int timeMinutes = 0;
    int timeSeconds = 0;

    double mTotalDistance;
    double mTotalDistanceKM;

    Handler mHandler;

    PolylineOptions mPoly;

    boolean paused = false;

    long startTime;
    long endTime;

    double averagePace;
    double averageSpeedKM;

    static long pauseLength = 0;

    static long time1 = 0;
    static long time2 = 0;

    double mCalories;

    SharedPreferences mSettings;

    ArrayList<Double> coordinateList;
    long coordinateNumber = 0;

    long lastLocationTime = 0;



    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        Toolbar toolbar = (Toolbar) findViewById(R.id.run_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // make sure CPU doesn't sleep
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        wl.acquire();

        coordinateList = new ArrayList<Double>();

        mSettings = getSharedPreferences(FitTracker.PREFS_NAME, 0);

        startTime = new Date().getTime();

        stopTextView = (TextView) findViewById(R.id.stopTextView);
        stopTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paused = true;
                endTime = new Date().getTime();

                final EditText input = new EditText(RunActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);

                AlertDialog dialog = new AlertDialog.Builder(RunActivity.this)
                        .setTitle("Description")
                        .setMessage("Enter Description")
                        .setView(input)
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String description = input.getText().toString();

                                Intent intent = new Intent(RunActivity.this, RunDataActivity.class);
                                intent.putExtra("coordinates", coordinateList);
                                intent.putExtra("calories", mCalories);
                                intent.putExtra("description", description);
                                intent.putExtra("pace", averagePace);
                                intent.putExtra("distance", mTotalDistance);
                                intent.putExtra("duration", mDurationTextView.getText());
                                intent.putExtra("starttime", startTime);
                                intent.putExtra("endtime", endTime);
                                intent.putExtra("pauselength", pauseLength);
                                wl.release();
                                startActivity(intent);
                                timer.cancel();
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                paused = false;
                            }
                        })
                        .create();
                dialog.show();
            }
        });
        pauseTextView = (TextView) findViewById(R.id.pauseTextView);
        pauseTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!paused) {
                    time1 = new Date().getTime();
                    pauseTextView.setText("Resume");
                } else {
                    time2 = new Date().getTime();
                    pauseLength += time2 - time1;
                    pauseTextView.setText("Pause");
                }
                paused = !paused;
            }
        });

        // TODO: Add support for hours
        mHandler = new Handler(new Handler.Callback() {
            public boolean handleMessage(Message msg) {
                String minutes = "" + timeMinutes;
                String seconds = "" + timeSeconds;
                if (timeSeconds < 10) {
                    seconds = "0" + timeSeconds;
                }
                mDurationTextView.setText(minutes + ":" + seconds);
                double dMinutes = (timeSeconds + timeMinutes * 60)/60.0;
                averagePace = dMinutes/mTotalDistance;
                Log.d(TAG, "Pace: " + averagePace);
                averageSpeedKM = mTotalDistanceKM/(dMinutes * 60);
                double paceSeconds = averagePace - Math.floor(averagePace);
                int nSeconds = (int) (paceSeconds * 60);
                minutes = "" + (int)averagePace;
                seconds = "" + nSeconds;
                if (nSeconds < 10) {
                    seconds = "0" + seconds;
                }
                mPaceTextView.setText(minutes + ":" + seconds);
                int weight = (int) (0.453592 * mSettings.getInt(Constants.WEIGHT_TAG, 150));
                int weightLB = (int) (weight * 2.20462);
                int timeHours = (timeMinutes + timeSeconds/60)/60;
                int age = mSettings.getInt(Constants.AGE_TAG, 20);
                String workoutType = calculateWorkoutType();
                mCalories = calculateCaloriesBurned(weight, timeMinutes + timeSeconds/60.0, workoutType, averageSpeedKM);
                if (workoutType.equals(FitnessActivities.RUNNING)) {
                    double TF = 0.84;
                    double vO2max = 15.03 * ((208 - 0.7*age)/70);
                    double CFF;
                    if (vO2max >= 56) {
                        CFF = 1.00;
                    } else if (vO2max >= 54) {
                        CFF = 1.01;
                    } else if (vO2max >= 52) {
                        CFF = 1.02;
                    } else if (vO2max >= 50) {
                        CFF = 1.03;
                    } else if (vO2max >= 48) {
                        CFF = 1.04;
                    } else if (vO2max >= 46) {
                        CFF = 1.05;
                    } else if (vO2max >= 44) {
                        CFF = 1.06;
                    } else {
                        CFF = 1.07;
                    }
                    mCalories = (0.95 * weight + TF) * mTotalDistanceKM * CFF;
                } else if (workoutType.equals(FitnessActivities.WALKING)) {
                    mCalories = (0.0215 * Math.pow(averageSpeedKM, 3) - 0.1765 * averageSpeedKM * averageSpeedKM +
                            0.8710 * averageSpeedKM + 1.4577) * weight * timeHours;
                } else {
                    int totalWeight = weightLB + 20;
                    double mph = (1/averagePace) / 60;
                    mCalories = ((0.046 * mph * totalWeight) + (0.066 * mph * mph * mph)) * timeHours;
                }
                long calories = Math.round(mCalories);
                mCaloriesTextView.setText("" + calories);
                return true;
            }
        });

        mTotalDistance = 0.0;

        mDistanceTextView = (TextView)findViewById(R.id.distanceTextView);
        mDurationTextView = (TextView)findViewById(R.id.durationTextView);
        mCaloriesTextView = (TextView)findViewById(R.id.caloriesTextView);
        mPaceTextView = (TextView)findViewById(R.id.paceTextView);
        mCurrentPaceTextView = (TextView)findViewById(R.id.currentPaceTextView);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!paused) {
                    timeSeconds++;
                    if (timeSeconds >= 60) {
                        timeMinutes++;
                        timeSeconds = 0;
                    }
                    mHandler.obtainMessage(1).sendToTarget();
                }
            }
        }, 1000, 1000);

        fragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
        map = fragment.getMap();

        mPoly = new PolylineOptions().width(9).color(Color.BLUE).visible(true).zIndex(30);
        map.addPolyline(mPoly);

        map.setMyLocationEnabled(true);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(250);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        buildGoogleApiClient();
    }

    public static double calculateCaloriesBurned(int weight, double time, String workoutType, double kmPerSecond) {
        double MET = 7;
        double mph = kmPerSecond / 2236.936;
        // determine MET
        if (workoutType.equals(FitnessActivities.RUNNING)) {
            MET = -0.2 + 1.56*mph;
        } else if (workoutType.equals(FitnessActivities.BIKING)) {
            if (mph <= 10) {
                MET = 4.0;
            } else if (mph <= 11.9) {
                MET = 6.8;
            } else if (mph <= 13.9) {
                MET = 8.0;
            } else if (mph <= 15.9) {
                MET = 10.0;
            } else if (mph < 20) {
                MET = 12.0;
            } else if (mph >= 20) {
                MET = 15.8;
            }
        } else if (workoutType.equals(FitnessActivities.WALKING)) {
            MET = 4.0;
        }
        double caloriesPerMinute = MET * 3.5 * weight / 200;

        return caloriesPerMinute * time;
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!!");
                                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                                double latitude = mLastLocation.getLatitude();
                                double longitude = mLastLocation.getLongitude();
                                map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(17).build()));
                                startLocationUpdates();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                                // If your connection to the sensor gets lost at some point,
                                // you'll be able to determine the reason and react to it here.
                                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                    Log.i(TAG, "Connection lost.  Cause: Network Lost.");
                                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                    Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
                                }
                            }
                        }
                )
                .addOnConnectionFailedListener(
                        new GoogleApiClient.OnConnectionFailedListener() {
                            // Called whenever the API client fails to connect.
                            @Override
                            public void onConnectionFailed(ConnectionResult result) {
                                Log.i(TAG, "Connection failed. Cause: " + result.toString());
                                if (!result.hasResolution()) {
                                    // Show the localized error dialog
                                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                                            RunActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.i(TAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(RunActivity.this,
                                                REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e(TAG,
                                                "Exception while starting resolution activity", e);
                                    }
                                }
                            }
                        }
                )
                .addApi(LocationServices.API)
                .build();
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map == null) {
            map = fragment.getMap();
            map.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        timer.cancel();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            timer.cancel();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!paused) {
            mCurrentLocation = location;
            mPoly.add(new LatLng(location.getLatitude(), location.getLongitude()));
            map.addPolyline(mPoly);
            double distance = calculateDistance();
            mTotalDistanceKM += distance;
            mTotalDistance += distance * 0.621371;
            double temp = Math.round(mTotalDistance * 1000.0);
            temp /= 1000.0;
            //mTotalDistance = mTotalDistance.setScale(2, RoundingMode.HALF_UP);
            mDistanceTextView.setText("" + temp);
            mLastLocation = mCurrentLocation;
            // move camera along
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(17).build()));

            /*============
            Calculate current pace
             */
            if (lastLocationTime != 0) {
                long timeDifferenceMil = new Date().getTime() - lastLocationTime;
                long timeDifferenceSec = timeDifferenceMil/1000;
                int currentPace = (int) (timeDifferenceSec/(distance * 0.621371));
                if (currentPace != 0 && distance != 0) {
                    mCurrentPaceTextView.setText(calculateTimeString(currentPace));
                }
            }
            lastLocationTime = new Date().getTime();

            if (coordinateNumber % 3 == 0) {
                coordinateList.add(latitude);
                coordinateList.add(longitude);
            }
        }
    }

    private double calculateDistance() {
        double changeLatitude = Math.toRadians(Math.abs(mCurrentLocation.getLatitude() - mLastLocation.getLatitude()));
        double changeLongitude = Math.toRadians(Math.abs(mCurrentLocation.getLongitude() - mLastLocation.getLongitude()));
        double latitude1 = Math.toRadians(mLastLocation.getLatitude());
        double latitude2 = Math.toRadians(mCurrentLocation.getLatitude());
        double a = Math.pow(Math.sin(changeLatitude/2), 2) + Math.cos(latitude1) * Math.cos(latitude2)
                * Math.pow(Math.sin(changeLongitude/2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = EARTH_RADIUS * c;
        return d;
    }




    static String calculateWorkoutType() {
        String workoutType = "";
        int workout = FitTracker.mSettings.getInt(Constants.ACTIVITY_TRACKING_TYPE, 0);
        switch (workout) {
            case 0: workoutType = FitnessActivities.WALKING;
                break;
            case 1: workoutType = FitnessActivities.RUNNING;
                break;
            case 2: workoutType = FitnessActivities.BIKING;
                break;
        }
        return workoutType;
    }


    private static String calculateTimeString(long seconds) {
        int minutes = (int) seconds/60;
        int secondsLeft = (int)seconds % 60;
        String timeText = minutes + ":";
        if (secondsLeft >= 10) {
            timeText += secondsLeft;
        } else {
            timeText += "0" + secondsLeft;
        }
        return timeText;
    }

}
