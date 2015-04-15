package com.smartfitness.daniellee.fittracker;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StepsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StepsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StepsFragment extends android.support.v4.app.Fragment {


    public static final String STEP_PREF = "stepdata";
    public static final String TIME_PREF = "time";

    private static final String STEPS_KEY = "TotalStepsKey";

    private static final String FILE_NAME = "graph_file";

    public DataReadResult result = null;

    private OnFragmentInteractionListener mListener;
    public static final String DATE_FORMAT = "HH:mm:ss:SSSS";
    public static final String TAG = "StepsFragment";

    public static int totalStepsToday = 0;

    //SwipeRefreshLayout swipeRefreshLayout;

    LinearLayout graphLayout;

    GraphView graphView;
    GraphViewSeries series;
    GraphView.GraphViewData[] data;

    private boolean connected;

    private int mActionBarSize;


    // necessary swipe height to trigger refresh
    private int mSwipeHeight;


    // current y-axis value of swipe
    double currentY;


    // start value of the circleview text
    int currentSteps;


    private static final int REQUEST_OAUTH = 1;

    /**
     *  Track whether an authorization activity is stacking over the current activity, i.e. when
     *  a known auth error is being resolved, such as showing the account chooser or presenting a
     *  consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    public static GoogleApiClient mClient = null;


    // start y-axis of the swipe event
    double startY = 0;


    // display width and height
    float dpHeight;
    float dpWidth;

    CircleView circleView;

    public static StepsFragment newInstance() {
        StepsFragment fragment = new StepsFragment();
        return fragment;
    }

    public StepsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_steps, container, false);

        connected = false;
        /*try {
            result = new MainActivity.GetReadResultTask().execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result != null) {
            for (DataSet ds : result.getDataSets()) {
                dumpDataSet(ds);
            }
        }*/


        buildFitnessClient();

        Fitness.RecordingApi.subscribe(mClient, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(TAG, "Existing subscription for activity detected.");
                            } else {
                                Log.i(TAG, "Successfully subscribed!");
                            }
                            connected = true;
                        } else {
                            Log.i(TAG, "There was a problem subscribing.");
                        }

                    }
                });
        Log.i(TAG, "Connecting...");
        mClient.connect();

        /*swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setColorSchemeResources(R.color.accentColor, R.color.colorPrimary, R.color.colorPrimaryDark);*/

        data = new GraphView.GraphViewData[24];

        for (int iii = 0; iii < data.length; iii++) {
            data[iii] = new GraphView.GraphViewData(iii + 1, 0);
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(getActivity().openFileInput(FILE_NAME));
            long time = ois.readLong();
            long timeNow = new Date().getTime();
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(time);
            int day = c.get(Calendar.DAY_OF_MONTH);
            c.setTimeInMillis(timeNow);
            int dayNow = c.get(Calendar.DAY_OF_MONTH);
            if (day == dayNow) {
                double[] x = (double[]) ois.readObject();
                double[] y = (double[]) ois.readObject();
                for (int iii = 0; iii < x.length; iii++) {
                    data[iii] = new GraphView.GraphViewData(x[iii], y[iii]);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException" + e.getStackTrace());
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException" + e.getStackTrace());
        }


        series = new GraphViewSeries(data);

        graphView = new BarGraphView(getActivity(), "");
        graphView.addSeries(series);

        graphView.setHorizontalLabels(new String[]{"2", "6", "10", "2", "6", "10"});
        graphView.setManualMinY(true);
        graphView.setManualYMinBound(0);

        graphView.setVerticalLabels(new String[]{""});

        if (savedInstanceState != null) {
            totalStepsToday = savedInstanceState.getInt(STEPS_KEY);
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        SharedPreferences sp;

        if ((sp = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0)) != null) {
            Calendar c1 = Calendar.getInstance();
            c1.setTime(new Date());

            Calendar c2 = Calendar.getInstance();
            c2.setTimeInMillis(sp.getLong(TIME_PREF, 0));
            if (c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)) {
                totalStepsToday = sp.getInt(STEP_PREF, 0);
            }
        }

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();

        dpHeight = displayMetrics.heightPixels;
        dpWidth = displayMetrics.widthPixels;

        // get the actionbar size
        final TypedArray styledAttributes = view.getContext().getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        mActionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        mSwipeHeight = (int)((dpHeight - 2 * mActionBarSize) * 0.5);

        graphLayout = (LinearLayout)view.findViewById(R.id.graphLinearLayout);
        ViewGroup.LayoutParams params = graphLayout.getLayoutParams();

        params.height = (int)(dpHeight / 7);


        graphLayout.addView(graphView);


        // get circle view
        circleView = (CircleView) view.findViewById(R.id.progress_circle);

        // set text in circleView to saved instance
        circleView.setStepsString(totalStepsToday);
        circleView.invalidate();

        // refresh the data when fragment created
        //refresh();

        // set ontouchevent to detect swipes
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                int action = MotionEventCompat.getActionMasked(event);

                if (action == MotionEvent.ACTION_DOWN) {
                    Log.d(TAG, "ACTION_DOWN");
                    currentSteps = Integer.parseInt(circleView.getStepsString());
                } else if (action == MotionEvent.ACTION_MOVE) {
                    currentY = event.getAxisValue(MotionEvent.AXIS_Y);
                    if (startY == 0) {
                        startY = currentY;
                    } else if (currentY < startY) {
                        startY = currentY;
                    } else {
                        double amount = (currentY - startY) / mSwipeHeight;
                        int animateTo = (int) (currentSteps - amount * currentSteps);
                        if (animateTo > 0) {
                            circleView.setStepsString(animateTo);
                        } else {
                            circleView.setStepsString(0);
                        }
                        circleView.invalidate();
                    }

                    Log.d(TAG, "X: " + event.getAxisValue(MotionEvent.AXIS_X) + " Y: " + event.getAxisValue(MotionEvent.AXIS_Y));
                } else if (action == MotionEvent.ACTION_UP) {
                    Log.d(TAG, "ACTION_UP");
                    if (currentY - startY > mSwipeHeight) {
                        refresh();
                    } else {
                        animateOut();
                    }
                    startY = 0;
                }
                return true;
            }
        });

        /*Path circle = new Path();

        RectF box = new RectF(0,0, 200, 200);
        float sweep = 360 * 50 * 0.01f;
        circle.addArc(box, 0, sweep);*/
        return view;
    }

    private void refresh() {
        totalStepsToday = 0;
        try {
            result = new GetReadResultTask().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (result != null && connected) {
                    /*AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                    AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                    stepsTextView.setAnimation(fadeOut);
                    fadeOut.setDuration(1200);
                    fadeOut.setFillAfter(true);*/
            List<Bucket> buckets = result.getBuckets();
            for (int iii = 0; iii < buckets.size(); iii++) {
                dumpDataSet(buckets.get(iii).getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA));
            }/*
                    stepsTextView.setAnimation(fadeIn);
                    fadeIn.setDuration(1200);
                    fadeIn.setFillAfter(true);*/
            series.resetData(data);
        }

        //stepsTextView.setText("" + totalStepsToday);


        animateOut();
    }

    /*private void drawCircle() {
        Bitmap bitmap = Bitmap.createBitmap((int)dpWidth * 3/4, (int)dpWidth * 3/4, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(1);

        Path circle = new Path();

        RectF box = new RectF(0,0,bitmap.getWidth(),bitmap.getHeight());
        // TODO: replace with actual goal
        float sweep = 360 * 50 * 0.01f;
        circle.addArc(box, 0, sweep);

        canvas.drawPath(circle, paint);
    }*/

    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Fitness.API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {

                            @Override
                            public void onConnected(Bundle bundle) {
                                Log.i(TAG, "Connected!!!");
                                // Now you can make calls to the Fitness APIs.
                                // Put application specific code here.
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
                                            getActivity(), 0).show();
                                    return;
                                }
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                if (!authInProgress) {
                                    try {
                                        Log.i(TAG, "Attempting to resolve failed connection");
                                        authInProgress = true;
                                        result.startResolutionForResult(getActivity(),
                                                REQUEST_OAUTH);
                                    } catch (IntentSender.SendIntentException e) {
                                        Log.e(TAG,
                                                "Exception while starting resolution activity", e);
                                    }
                                }
                            }
                        }
                )
                .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == Activity.RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mClient.isConnecting() && !mClient.isConnected()) {
                    mClient.connect();
                }
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            long startTime = dp.getStartTime(TimeUnit.MILLISECONDS);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(startTime);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            Log.i(TAG, "\tStart: " + dateFormat.format(startTime));
            long endTime = dp.getEndTime(TimeUnit.MILLISECONDS);
            Log.i(TAG, "\tEnd: " + dateFormat.format(endTime));
            for(Field field : dp.getDataType().getFields()) {
                int bucketValue = dp.getValue(field).asInt();
                double temp = data[hour].getY();
                data[hour] = new GraphView.GraphViewData(hour, temp + bucketValue);
                totalStepsToday += bucketValue;
                Log.i(TAG, "\tField: " + field.getName() +
                        " Value: " + bucketValue);
            }
        }
    }
    int delay = 5;//milli seconds

    int steps;

    boolean done = false;

    public void animateIn() {
        final Handler h = new Handler();
        delay = 20;

        steps = totalStepsToday;

        h.postDelayed(new Runnable() {

            public void run() {
                if (!done) {
                    //do something
                    steps = (int) (steps - steps * 0.1);
                    circleView.setStepsString(steps);
                    circleView.invalidate();
                    if (steps > 0) {
                        h.postDelayed(this, delay);
                    } else {
                        done = true;
                    }
                }
            }
        }, delay);
    }



    private void animateOut() {
        steps = Integer.parseInt(circleView.getStepsString());
        final Handler h = new Handler();
        delay = 20;

        Log.d("StepsFragmentDebug", "" + totalStepsToday);

        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                steps = (int) (steps + (totalStepsToday - steps) * 0.1);
                circleView.setStepsString(steps);
                circleView.invalidate();
                if (steps < totalStepsToday - 20) {
                    h.postDelayed(this, delay);
                } else {
                    circleView.setStepsString(totalStepsToday);
                    circleView.invalidate();
                    done = false;
                }
            }
        }, delay);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Connect to the Fitness API


    }

    @Override
    public void onStop() {
        super.onStop();
        if (mClient.isConnected()) {
            mClient.disconnect();
        }

        if (MainActivity.mSettings == null) {
            MainActivity.mSettings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        }
        SharedPreferences.Editor editor = MainActivity.mSettings.edit();
        editor.putInt(STEP_PREF, totalStepsToday);

        editor.putLong(TIME_PREF, new Date().getTime());

        editor.apply();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(getActivity().openFileOutput(FILE_NAME, Context.MODE_PRIVATE));
            double[] x = new double[data.length];
            double[] y = new double[data.length];
            for (int iii = 0; iii < data.length; iii++) {
                x[iii] = data[iii].getX();
                y[iii] = data[iii].getY();
            }
            oos.writeLong(new Date().getTime());
            oos.writeObject(x);
            oos.writeObject(y);
            oos.close();
        } catch (IOException e) {
            Log.e(TAG, "IOException");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STEPS_KEY, totalStepsToday);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }



    public class GetReadResultTask extends AsyncTask<Void, Void, DataReadResult> {

        protected DataReadResult doInBackground(Void... voids) {
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            long endTime = cal.getTimeInMillis();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            long startTime = cal.getTimeInMillis();

            DataReadRequest readRequest = new DataReadRequest.Builder()
                    .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                    .bucketByTime(5, TimeUnit.MINUTES)
                    .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                    .build();
            DataReadResult result =
                    Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
            return result;
        }
    }
}
