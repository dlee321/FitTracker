package com.smartfitness.daniellee.fittracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class RunActivity extends ActionBarActivity implements LocationListener {

    protected static final String RUN_ARRAY_KEY = "runs";
    protected static final String CALORIES_KEY = "caloriesperrun";

    public static final String IDENTIFIER = RunActivity.class.getCanonicalName();

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

    ParseUser user;

    ArrayList<double[]> coordinateList;
    long coordinateNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        coordinateList = new ArrayList<double[]>();

        user = ParseUser.getCurrentUser();

        mSettings = getSharedPreferences(MainActivity.PREFS_NAME, 0);

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

                // add time of run
                coordinateList.add(new double[] {new Date().getTime(), 0});

                user.add(RUN_ARRAY_KEY, coordinateList.toArray());
                user.add(CALORIES_KEY, mCalories);

                AlertDialog dialog = new AlertDialog.Builder(RunActivity.this)
                        .setTitle("Description")
                        .setMessage("Enter Description")
                        .setView(input)
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String description = input.getText().toString();

                                new InputSessionTask().execute(description);
                                Intent intent = new Intent(RunActivity.this, MainActivity.class);
                                startActivity(intent);
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

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                String minutes = "" + timeMinutes;
                String seconds = "" + timeSeconds;
                if (timeSeconds < 10) {
                    seconds = "0" + timeSeconds;
                }
                mDurationTextView.setText(minutes + ":" + seconds);
                double dMinutes = (timeSeconds + timeMinutes * 60)/60.0;
                averagePace = dMinutes/mTotalDistance;
                Log.d(TAG, "Pace:" + averagePace);
                averageSpeedKM = mTotalDistanceKM/(dMinutes * 60);
                double paceSeconds = averagePace - Math.floor(averagePace);
                int nSeconds = (int) (paceSeconds * 60);
                minutes = "" + (int)averagePace;
                seconds = "" + nSeconds;
                if (nSeconds < 10) {
                    seconds = "0" + seconds;
                }
                mPaceTextView.setText(minutes + ":" + seconds);
                int weight = (int) (0.453592 * mSettings.getInt(SignUpActivity.WEIGHT_TAG, 150));
                int timeHours = (timeMinutes + timeSeconds/60)/60;
                int age = mSettings.getInt(SignUpActivity.AGE_TAG, 20);
                if (calculateWorkoutType().equals(FitnessActivities.RUNNING) || calculateWorkoutType().equals(FitnessActivities.RUNNING_JOGGING)) {
                    double TF = 0.84;
                    double vO2max = 15.03 * ((208 - 0.7*age)/70);
                    // TODO: calculate actual CFF
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
                } else {
                    mCalories = (0.0215 * Math.pow(averageSpeedKM, 3) - 0.1765 * averageSpeedKM * averageSpeedKM +
                            0.8710 * averageSpeedKM + 1.4577) * weight * timeHours;
                }
                long calories = Math.round(mCalories);
                mCaloriesTextView.setText("" + calories);
            }
        };

        mTotalDistance = 0.0;

        mDistanceTextView = (TextView)findViewById(R.id.distanceTextView);
        mDurationTextView = (TextView)findViewById(R.id.durationTextView);
        mCaloriesTextView = (TextView)findViewById(R.id.caloriesTextView);
        mPaceTextView = (TextView)findViewById(R.id.paceTextView);

        Timer timer = new Timer();
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
                .addApi(Fitness.API)
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
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
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

            if (coordinateNumber % 3 == 0) {
                coordinateList.add(new double[] {latitude, longitude});
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

    public class InputSessionTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... description) {
            String workoutType = calculateWorkoutType();
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            int day = c.get(Calendar.DAY_OF_MONTH);
            int month = c.get(Calendar.MONTH);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            Session session = new Session.Builder()
                    .setName(workoutType + month + "-" + day + ":" + hour + ":" + minute)
                    .setIdentifier(IDENTIFIER)
                    .setDescription(description[0])
                    .setActivity(workoutType)
                    .setStartTime(startTime + (pauseLength/2), TimeUnit.MILLISECONDS)
                    .setEndTime(endTime - (pauseLength/2), TimeUnit.MILLISECONDS)
                    .build();

            SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                    .setSession(session)
                    .build();

            // Then, invoke the Sessions API to insert the session and await the result,
            // which is possible here because of the AsyncTask. Always include a timeout when
            // calling await() to avoid hanging that can occur from the service being shutdown
            // because of low memory or other conditions.
            Log.i(TAG, "Inserting the session in the History API");
            com.google.android.gms.common.api.Status insertStatus =
                    Fitness.SessionsApi.insertSession(mGoogleApiClient, insertRequest)
                            .await(1, TimeUnit.MINUTES);

            // Before querying the session, check to see if the insertion succeeded.
            if (!insertStatus.isSuccess()) {
                Log.i(TAG, "There was a problem inserting the session: " +
                        insertStatus.getStatusMessage());
            }

            // At this point, the session has been inserted and can be read.
            Log.i(TAG, "Session insert was successful!");

            return null;
        }
    }

    private String calculateWorkoutType() {
        String workoutType = "";
        if (averagePace > 20) {
            workoutType = FitnessActivities.WALKING;
        } else if (averagePace <= 20) {
            workoutType = FitnessActivities.WALKING_FITNESS;
            if (averagePace <= 12) {
                workoutType = FitnessActivities.RUNNING_JOGGING;
            }
            if (averagePace <= 10) {
                workoutType = FitnessActivities.RUNNING;
            }
        }
        return workoutType;
    }
}
