package com.smartfitness.daniellee.fittracker;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.ParseACL;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class RunDataActivity extends ActionBarActivity {

    public static final String IDENTIFIER = RunDataActivity.class.getCanonicalName();

    private static final String TAG = RunDataActivity.class.getSimpleName();

    private static final int REQUEST_OAUTH = 1;

    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    MapFragment fragment;
    GoogleMap map;

    GoogleApiClient mGoogleApiClient;

    PolylineOptions mPoly;

    ParseUser user;

    double averagePace;

    long startTime;
    long endTime;
    long pauseLength;
    double calories;
    double distance;
    ArrayList<double[]> coordinateList;


    TextView notesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_data);

        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        user = ParseUser.getCurrentUser();

        buildGoogleApiClient();

        Intent intent = getIntent();
        if (intent != null) {

            fragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.routeMap));
            map = fragment.getMap();

            mPoly = new PolylineOptions().width(9).color(Color.BLUE).visible(true).zIndex(30);

            final String description = intent.getStringExtra("description");
            setTitle(description);

            coordinateList = (ArrayList<double[]>) intent.getSerializableExtra("coordinates");

            // coordinates for the bounds of the map
            double minX = Integer.MAX_VALUE;
            double minY = Integer.MAX_VALUE;
            double maxX = Integer.MIN_VALUE;
            double maxY = Integer.MIN_VALUE;
            for (double[] coordinate: coordinateList) {
                mPoly.add(new LatLng(coordinate[0], coordinate[1]));
                if (coordinate[0] < minX) {
                    minX = coordinate[0];
                } else if (coordinate[0] > maxX) {
                    maxX = coordinate[0];
                }

                if (coordinate[1] < minY) {
                    minY = coordinate[1];
                } else if (coordinate[1] > maxY) {
                    maxY = coordinate[1];
                }
            }

            map.addPolyline(mPoly);

            if (maxX > -90) {
                LatLngBounds bounds = new LatLngBounds(new LatLng(minX, minY), new LatLng(maxX, maxY));
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, this.getResources().getDisplayMetrics().widthPixels, this.getResources().getDisplayMetrics().heightPixels, 1));
            }

            calories = intent.getDoubleExtra("calories", 0.0);
            averagePace = intent.getDoubleExtra("pace", 0.0);
            distance = intent.getDoubleExtra("distance", 0.0);
            String duration = intent.getStringExtra("duration");

            startTime = intent.getLongExtra("starttime", 0);
            endTime = intent.getLongExtra("endtime", 0);
            pauseLength = intent.getLongExtra("pauselength", 0);

            TextView durationTextView = (TextView) findViewById(R.id.endDurationTextView);
            TextView distanceTextView = (TextView) findViewById(R.id.endDistanceTextView);
            TextView paceTextView = (TextView) findViewById(R.id.endPaceTextView);
            TextView caloriesTextView = (TextView) findViewById(R.id.endCaloriesTextView);

            notesTextView = (TextView) findViewById(R.id.noteTextView);

            durationTextView.setText(duration);

            // round distance to 2 decimal places
            distanceTextView.setText("" + (((double)Math.round(distance*100.0))/100.0) + "\nmi");

            String paceString = "" + (int) averagePace;
            paceString += ":\n" + (int)((averagePace - Math.floor(averagePace)) * 60);

            paceTextView.setText(paceString);

            caloriesTextView.setText("" + Math.round(calories));

            TextView saveTextView = (TextView) findViewById(R.id.saveTextView);
            saveTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //user.remove(getString(R.string.run_key));
                    Toast.makeText(RunDataActivity.this, "Saving activity...", Toast.LENGTH_LONG).show();

                    new InputSessionTask().execute(description);

                    returnToMainActivity();

                }
            });
        }
    }

    private void reduceArrayList(ArrayList<double[]> coordinateList) {
        for (int iii = 1; iii < coordinateList.size(); iii++) {
            coordinateList.remove(iii);
        }
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!!");
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
                                            RunDataActivity.this, 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.i(TAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(RunDataActivity.this,
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
                .addApi(Fitness.SESSIONS_API)
                .addApi(Fitness.RECORDING_API)
                .build();
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
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public class InputSessionTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... description) {
            // PARSE
            if (user == null) {
                user = ParseUser.getCurrentUser();
            }
            Run run = new Run();
            boolean outOfMemory = run.setCoordinates(coordinateList);
            while (outOfMemory) {
                run = new Run();
                reduceArrayList(coordinateList);
                outOfMemory = run.setCoordinates(coordinateList);
            }
            Log.d(TAG, "Coordinates set");
            run.setCalories(calories);
            Log.d(TAG, "Calories set");
            run.setStartTime(startTime + (pauseLength / 2));
            Log.d(TAG, "Start time set");
            run.setEndTime(endTime - (pauseLength / 2));
            Log.d(TAG, "End time set");
            run.setDistance(distance);
            Log.d(TAG, "Distance set");
            run.setACL(new ParseACL(user));
            Log.d(TAG, "ACL set");
            String workoutType = calculateWorkoutType();
            if (workoutType.equals(FitnessActivities.WALKING) || workoutType.equals(FitnessActivities.WALKING_FITNESS)) {
                run.setActivityType(Run.WALKING);
            } else if (workoutType.equals(FitnessActivities.RUNNING) || workoutType.equals(FitnessActivities.RUNNING_JOGGING)) {
                run.setActivityType(Run.RUNNING);
            }
            Log.d(TAG, "Activity type set");
            run.setDescription(description[0]);
            run.setNotes(notesTextView.getText().toString());
            user.add(Keys.RUNS_KEY, run);
            Log.d(TAG, "Run added");
            user.saveInBackground();
            Log.d(TAG, "Saving user");

            // GOOGLE FIT
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_run_data, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_discard) {
            returnToMainActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void returnToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Do nothing
    }
}
