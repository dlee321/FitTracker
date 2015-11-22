package com.smartfitness.daniellee.fittracker;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StepsHistoryActivity extends AppCompatActivity {

    private final static String TAG = StepsHistoryActivity.class.getSimpleName();

    ProgressDialog mProgress;

    GoogleApiClient mClient;


    ArrayList<Integer> mDays;

    int index = 0;

    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.steps_history_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Steps History");

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Loading Steps History...");
        mProgress.show();

        buildFitnessClient();

        Log.i(TAG, "Connecting...");
        mClient.connect();

        mRecyclerView = (RecyclerView) findViewById(R.id.steps_history_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!");
                                // Now you can make calls to the Fitness APIs.
                                // Put application specific code here.
                                new GetReadResultTaskHistory().execute();
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
                                            StepsHistoryActivity.this, 0).show();
                                }
                            }
                        }
                )
                .addApi(Fitness.SESSIONS_API)
                .addApi(Fitness.RECORDING_API)
                .build();
    }

    private void dumpDataSet(DataSet dataSet) {

        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                int bucketValue = dp.getValue(field).asInt();
                if (field.getName().equals("steps")) {
                    Log.d(TAG, "" + index);
                    mDays.add(bucketValue);
                    index++;
                }
            }
        }
    }

    public class GetReadResultTaskHistory extends AsyncTask<Void, Void, DataReadResult> {

        protected DataReadResult doInBackground(Void... voids) {
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            long endTime = cal.getTimeInMillis();
            /*int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int[] date = subtract50Days(year, month, day);
            cal.set(date[0], date[1], date[2]);*/
            cal.add(Calendar.MONTH, -1);
            long startTime = cal.getTimeInMillis();

            DataReadRequest readRequest = new DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                    .bucketByTime(1, TimeUnit.DAYS)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build();
            Log.i(TAG, "Reading step data...");
            DataReadResult result =
                    Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
            return result;
        }

        @Override
        protected void onPostExecute(DataReadResult dataReadResult) {
            List<Bucket> buckets;
            buckets = dataReadResult.getBuckets();
            mDays = new ArrayList<>();
            for (int iii = 0; iii < buckets.size(); iii++) {
                dumpDataSet(buckets.get(iii).getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA));
            }

            StepsHistoryAdapter adapter = new StepsHistoryAdapter(mDays.toArray(new Integer[]{}), getApplicationContext());
            mRecyclerView.setAdapter(adapter);
            mProgress.dismiss();
        }
    }

}
