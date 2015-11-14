package com.smartfitness.daniellee.fittracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.app.DialogFragment;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.HistoryApi;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DailyTotalRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StepsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StepsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StepsFragment extends Fragment {

    private ObservableScrollView mScrollView;

    private static final double MINUTES_PER_DAY = 1440;
    public static final int MILL_PER_HOUR = 3600000;

    SharedPreferences sp;


    private static final String STEPS_KEY = "TotalStepsKey";

    private static final String FILE_NAME = "graph_file";
    public static final String DATE_FORMAT = "HH:mm:ss:SSSS";
    public static final String TAG = "StepsFragment";

    public static int totalStepsToday = 0;

    private OnFragmentInteractionListener mListener;

    //SwipeRefreshLayout swipeRefreshLayout;

    LinearLayout graphLayout;

    GraphView graphView;
    GraphViewSeries series;
    GraphView.GraphViewData[] data;

    // calories text
    TextView caloriesTextView;

    // floating action button
    FloatingActionButton fabPlus;

    private boolean connected;

    private int mActionBarSize;


    // necessary swipe width to trigger refresh
    private int mSwipeHeight;


    // current y-axis value of swipe
    double currentY;


    // start value of the circleview text
    int currentSteps;


    private static final int REQUEST_OAUTH = 1;

    /**
     * Track whether an authorization activity is stacking over the current activity, i.e. when
     * a known auth error is being resolved, such as showing the account chooser or presenting a
     * consent dialog. This avoids common duplications as might happen on screen rotations, etc.
     */
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;

    public static GoogleApiClient mClient = null;


    // start y-axis of the swipe event
    double startY = 0;


    // display width and width
    float dpHeight;
    float dpWidth;

    CircleView circleView;

    private static ProgressDialog mProgress;

    // variables used in animateEnlarge()
    int x = 0;
    float totalScale = 1;


    // calendars for alertdialog starttime and endtime
    Calendar calStart;
    Calendar calEnd;
    private boolean isStartTime;


    RadioGroup mRadioGroup;
    EditText mStartTime;
    EditText mEndTIme;
    private EditText mDistanceEditText;
    private FloatingActionButton fabActivities;

    public static StepsFragment newInstance() {
        return new StepsFragment();
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

        if (savedInstanceState != null) {
            totalStepsToday = savedInstanceState.getInt(STEPS_KEY);
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        connected = false;

        // show progress dialog while loading
        mProgress = new ProgressDialog(getActivity());
        mProgress.setMessage("Loading Step Data...");
        mProgress.show();
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


        // build fitness client then subscribe if not already subscribed
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

        if ((sp = getActivity().getSharedPreferences(FitTracker.PREFS_NAME, 0)) != null) {
            if (isTimeToday(sp.getLong(Constants.TIME_PREF, 0))) {
                totalStepsToday = sp.getInt(Constants.STEP_PREF, 0);
            } else {
                sp.edit().putBoolean(Constants.ACTIVITY_YET_TODAY, false).apply();
            }
        }

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();

        dpHeight = displayMetrics.heightPixels;
        dpWidth = displayMetrics.widthPixels;

        // get the actionbar size
        final TypedArray styledAttributes = view.getContext().getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        mActionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        mSwipeHeight = (int) ((dpHeight - 2 * mActionBarSize) * 0.5);

        graphLayout = (LinearLayout) view.findViewById(R.id.graphLinearLayout);
        ViewGroup.LayoutParams params = graphLayout.getLayoutParams();

        params.height = (int) (dpHeight / 7);


        graphLayout.addView(graphView);


        // get circle view
        circleView = (CircleView) view.findViewById(R.id.progress_circle);


        final LayoutInflater inflater1 = inflater;

        // set text in circleView to saved instance
        circleView.setStepsString(totalStepsToday);
        circleView.invalidate();
        /*circleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateEnlarge();

                mProgress.dismiss();
            }
        });*/

        // refresh the data when fragment created
        //refresh();


        // set ontouchevent to detect swipes
        /*view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                int action = MotionEventCompat.getActionMasked(event);

                if (action == MotionEvent.ACTION_DOWN) {
                    //Log.d(TAG, "ACTION_DOWN");
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

                    //Log.d(TAG, "X: " + event.getAxisValue(MotionEvent.AXIS_X) + " Y: " + event.getAxisValue(MotionEvent.AXIS_Y));
                } else if (action == MotionEvent.ACTION_UP) {
                    //Log.d(TAG, "ACTION_UP");
                    if (currentY - startY > mSwipeHeight) {
                        refresh();
                    } else {
                        animateOut();
                    }
                    startY = 0;
                }
                return true;
            }
        });*/

        caloriesTextView = (TextView) view.findViewById(R.id.dailyCaloriesTextView);

        /*Path circle = new Path();

        RectF box = new RectF(0,0, 200, 200);
        float sweep = 360 * 50 * 0.01f;
        circle.addArc(box, 0, sweep);*/

        // setup floating action bar
        fabPlus = (FloatingActionButton) view.findViewById(R.id.floatingActionButton1);
        fabActivities = (FloatingActionButton) view.findViewById(R.id.floatingActionButton2);
        if (sp.getBoolean(Constants.ACTIVITY_YET_TODAY, true)) {
            fabActivities.setVisibility(View.VISIBLE);
            fabActivities.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mProgress.setMessage("Loading activities...");
                    mProgress.show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    View v = inflater1.inflate(R.layout.dialog_daily_activity_detail, null);
                    TextView textView1 = (TextView) v.findViewById(R.id.noActivitiesTextView);
                    ListView listView1 = (ListView) v.findViewById(R.id.dailyActivitiesList);

                    ParseUser user = ParseUser.getCurrentUser();
                    ArrayList<Run> runs = (ArrayList<Run>) user.get(Constants.RUNS_KEY);
                    if (runs.size() > 0) {
                        Run run = runs.get(runs.size() - 1);
                        int secondsInADay = 60 * 60 * 24;
                        long timestamp1 = new Date().getTime();
                        int daysSinceEpoch1 = (int) (timestamp1 / secondsInADay);
                        int ind = runs.size() - 1;
                        try {
                            while (daysSinceEpoch1 == run.getStartTime() / secondsInADay) {
                                ind++;
                                run = runs.get(ind);
                            }
                        } catch (NullPointerException e) {

                        }
                        if (ind > 0) {
                            textView1.setVisibility(View.GONE);
                            listView1.setVisibility(View.VISIBLE);
                            runs.subList(ind + 1, runs.size()).clear();

                            ArrayAdapter adapter = new ActivityHistoryAdapter(getActivity(), R.layout.activity_list_item, runs);
                            listView1.setAdapter(adapter);
                        }
                    }


                    builder.setView(v);

                    builder.create().show();
                }
            });
        }
        fabPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                View v = inflater1.inflate(R.layout.dialog_add_activity, null);
                mRadioGroup = (RadioGroup) v.findViewById(R.id.activityTypeRadioGroup);
                mDistanceEditText = (EditText) v.findViewById(R.id.distanceEditText);
                mStartTime = (EditText) v.findViewById(R.id.startTime);
                mEndTIme = (EditText) v.findViewById(R.id.endTime);
                calEnd = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                String endTimeText = format.format(calEnd.getTime());
                calStart = Calendar.getInstance();
                calStart.add(Calendar.MINUTE, -30);
                String startTimeText = format.format(calStart.getTime());
                mStartTime.setText(startTimeText);
                mEndTIme.setText(endTimeText);

                mStartTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isStartTime = true;
                        showTimePicker(calStart);
                    }
                });

                mEndTIme.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isStartTime = false;
                        showTimePicker(calEnd);
                    }
                });

                builder.setTitle("Add Activity")
                        .setView(v)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    Calendar calStart = Calendar.getInstance();
                                    int year = calStart.get(Calendar.YEAR);
                                    int month = calStart.get(Calendar.MONTH);
                                    int dayOfMonth = calStart.get(Calendar.DAY_OF_MONTH);
                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                    calStart.setTime(sdf.parse(mStartTime.getText().toString()));
                                    calStart.set(Calendar.YEAR, year);
                                    calStart.set(Calendar.MONTH, month);
                                    calStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                    Calendar calEnd = Calendar.getInstance();
                                    year = calEnd.get(Calendar.YEAR);
                                    month = calEnd.get(Calendar.MONTH);
                                    dayOfMonth = calEnd.get(Calendar.DAY_OF_MONTH);
                                    calEnd.setTime(sdf.parse(mEndTIme.getText().toString()));
                                    calEnd.set(Calendar.YEAR, year);
                                    calEnd.set(Calendar.MONTH, month);
                                    calEnd.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                    long startTimeMillis = calStart.getTimeInMillis();
                                    long endTimeMillis = calEnd.getTimeInMillis();

                                    long distanceLong = Double.doubleToRawLongBits(Double.parseDouble(mDistanceEditText.getText().toString()));


                                    int radioButtonID = mRadioGroup.getCheckedRadioButtonId();
                                    View radioButton = mRadioGroup.findViewById(radioButtonID);
                                    long workoutType = mRadioGroup.indexOfChild(radioButton);

                                    Log.d(TAG, "Activity Times: " + startTimeMillis + " " + endTimeMillis);

                                    if (startTimeMillis >= endTimeMillis) {
                                        Toast.makeText(getActivity(), "Please make sure the start time is before the end time", Toast.LENGTH_LONG).show();
                                    } else {
                                        new InputSessionTask().execute(startTimeMillis, endTimeMillis, distanceLong, workoutType);
                                    }
                                } catch (java.text.ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                builder.create().show();
            }
        });


        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mScrollView = (ObservableScrollView) view.findViewById(R.id.scrollView);

        MaterialViewPagerHelper.registerScrollView(getActivity(), mScrollView, null);
    }

    /*private void animateEnlarge() {
        Log.d(TAG, "onClick worked");
        //final ViewGroup.LayoutParams params = circleView.getLayoutParams();
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "" + x);
                float scale = (float) (1 + (-0.2 * x*x + 10)/100.0);
                totalScale *= scale;
                Log.d(TAG, "Scales: " + scale + " " + totalScale);
                circleView.setScaleX(scale);
                circleView.setScaleY(scale);
                if (totalScale > 1) {
                    h.postDelayed(this, 10);
                }
                x++;
            }
        }, 10);
        x = 0;
        totalScale = 1;
    }*/


    private void showTimePicker(Calendar time) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.TIME_PICKER_FRAGMENT_TIME, time);
        DialogFragment fragment;
        if (isStartTime) {
            fragment = new TimePickerFragment2(mStartTime);
        } else {
            fragment = new TimePickerFragment2(mEndTIme);
        }
        fragment.setArguments(bundle);
        fragment.show(getFragmentManager(), "timePicker");
    }

    private static boolean isTimeToday(long time2) {
        Calendar cal1 = Calendar.getInstance();

        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(time2);

        int cal1Day = cal1.get(Calendar.DAY_OF_MONTH);
        int cal2Day = cal2.get(Calendar.DAY_OF_MONTH);
        Log.d(TAG, "Days: " + cal1Day + " " + cal2Day);
        return cal1Day == cal2Day;
    }

    private void refresh() {
        mProgress.setMessage("Loading Step Data...");
        mProgress.show();
        new GetReadResultTask().execute();
        new GetReadResultTask2().execute();
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
        float sweep = 360 * 50 * 0.01f;
        circle.addArc(box, 0, sweep);

        canvas.drawPath(circle, paint);
    }*/

    private void buildFitnessClient() {
        // Create the Google API Client
        mClient = new GoogleApiClient.Builder(getActivity())
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
                                new GetReadResultTask().execute();
                                new GetReadResultTask2().execute();
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
                .addApi(Fitness.SESSIONS_API)
                .addApi(Fitness.RECORDING_API)
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
            for (Field field : dp.getDataType().getFields()) {
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

    /*public void animateIn() {
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
    }*/


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
        //Log.d(TAG, "onStart");
        // Connect to the Fitness API
        if (!mClient.isConnected()) {
            mClient.connect();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (mClient.isConnected()) {
            mClient.disconnect();
        }

        if (FitTracker.mSettings == null) {
            FitTracker.mSettings = getActivity().getSharedPreferences(FitTracker.PREFS_NAME, 0);
        }
        SharedPreferences.Editor editor = FitTracker.mSettings.edit();
        editor.putInt(Constants.STEP_PREF, totalStepsToday);

        editor.putLong(Constants.TIME_PREF, new Date().getTime());

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

        @Override
        protected void onPostExecute(DataReadResult dataReadResult) {
            if (dataReadResult != null && connected) {
                    /*AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                    AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                    stepsTextView.setAnimation(fadeOut);
                    fadeOut.setDuration(1200);
                    fadeOut.setFillAfter(true);*/
                List<Bucket> buckets = dataReadResult.getBuckets();
                for (int iii = 0; iii < buckets.size(); iii++) {
                    dumpDataSet(buckets.get(iii).getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA));
                }/*
                    stepsTextView.setAnimation(fadeIn);
                    fadeIn.setDuration(1200);
                    fadeIn.setFillAfter(true);*/
                series.resetData(data);
            }
        }
    }

    public class GetReadResultTask2 extends AsyncTask<Void, Void, DailyTotalResult> {

        protected DailyTotalResult doInBackground(Void... voids) {
            Calendar cal = Calendar.getInstance();
            Date now = new Date();
            cal.setTime(now);
            long endTime = cal.getTimeInMillis();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            long startTime = cal.getTimeInMillis();

            PendingResult<DailyTotalResult> readRequest = Fitness.HistoryApi.readDailyTotal(mClient, DataType.TYPE_STEP_COUNT_DELTA);
            DailyTotalResult result =
                    readRequest.await(1, TimeUnit.MINUTES);
            return result;
        }

        @Override
        protected void onPostExecute(DailyTotalResult dailyTotalResult) {
            totalStepsToday = 0;
            if (dailyTotalResult != null && connected) {
                    /*AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                    AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                    stepsTextView.setAnimation(fadeOut);
                    fadeOut.setDuration(1200);
                    fadeOut.setFillAfter(true);*/
                DataSet totalSet = dailyTotalResult.getTotal();
                totalStepsToday = totalSet.isEmpty()
                        ? 0
                        : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
            }
            Log.d(TAG, "" + totalStepsToday);

            int calories = calculateCalories(totalStepsToday);

            caloriesTextView.setText("" + calories);

            //stepsTextView.setText("" + totalStepsToday);

            mProgress.dismiss();

            animateOut();
        }
    }

    private int calculateActivityCalories(long time, double distance) {
        double distanceKM = distance * 1.60934;
        double timeHours = ((double) time / MILL_PER_HOUR);
        double averageSpeedKM = distanceKM / timeHours;
        double mph = distance / timeHours;
        int age = FitTracker.mSettings.getInt(Constants.AGE_TAG, 20);
        int weightLB = FitTracker.mSettings.getInt(Constants.WEIGHT_TAG, 150);
        int weightKG = (int) (0.453592 * weightLB);
        double calories = 0;
        int radioButtonID = mRadioGroup.getCheckedRadioButtonId();
        View radioButton = mRadioGroup.findViewById(radioButtonID);
        int workoutType = mRadioGroup.indexOfChild(radioButton);
        if (workoutType == Run.RUNNING) {
            double TF = 0.84;
            double vO2max = 15.03 * ((208 - 0.7 * age) / 70);
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
            calories = (0.95 * weightKG + TF) * distanceKM * 1.60934 * CFF;
        } else if (workoutType == Run.WALKING) {
            calories = (0.0215 * Math.pow(averageSpeedKM, 3) - 0.1765 * averageSpeedKM * averageSpeedKM +
                    0.8710 * averageSpeedKM + 1.4577) * weightKG * timeHours;
        } else if (workoutType == Run.CYCLING) {
            int totalWeight = weightLB + 20;
            calories = ((0.046 * mph * totalWeight) + (0.066 * mph * mph * mph)) * timeHours;
        }
        return (int) Math.round(calories);
    }

    private static int calculateCalories(int totalStepsToday) {
        int cals;
        ParseUser user = ParseUser.getCurrentUser();
        double height = user.getInt(Constants.HEIGHT_FEET_TAG) + user.getInt(Constants.HEIGHT_INCH_TAG) / 12.0;
        double stride = .414 * height;
        double weight = user.getDouble(Constants.WEIGHT_TAG);
        double weightKG = weight * 0.453592;
        double caloriesPerMile = weight * .57;
        double miles = stride * totalStepsToday / 5280;
        cals = (int) (caloriesPerMile * miles);
        if (FitTracker.mSettings.getBoolean(Constants.ACTIVITY_YET_TODAY, true)) {
            Log.d(TAG, "Adding activity calories");
            boolean first = true;
            ArrayList<Run> runs = (ArrayList<Run>) user.get(Constants.RUNS_KEY);
            for (int iii = runs.size() - 1; iii >= 0; iii--) {
                try {
                    String id = runs.get(iii).getObjectId();
                    ParseQuery<Run> query = ParseQuery.getQuery(Run.class);
                    Run run = query.get(id);
                    long time = run.getCreatedAt().getTime();
                    if (!isTimeToday(time)) {
                        break;
                    }
                    Log.d(TAG, "RunEndTime: " + time);
                    cals += run.getCalories();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        // add resting metabolic calories
        int restingCals;
        if (user.getBoolean(Constants.GENDER_TAG) == Constants.MALE) {
            restingCals = (int) Math.round((88.4 + 13.4 * weightKG) + 2.54 * 4.8 * height - 5.68 * calculateAge(user.getString(Constants.DATE_BIRTH_TAG)));
        } else {
            restingCals = (int) Math.round((447.6 + 9.25 * weightKG) + 2.54 * 3.1 * height - 4.33 * calculateAge(user.getString(Constants.DATE_BIRTH_TAG)));
        }
        Log.d(TAG, "RestingCals: " + restingCals);
        double factor = fractionOfDay();
        cals += factor * restingCals;
        return cals;
    }

    private static double fractionOfDay() {
        Calendar c = Calendar.getInstance();
        int minutes = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        Log.d(TAG, "Minute: " + minutes);
        double factor = minutes / MINUTES_PER_DAY;
        Log.d(TAG, "Factor: " + factor);
        return factor;
    }

    private static int calculateAge(String date) {
        String[] splits = date.split("-");
        int month = Integer.parseInt(splits[0]);
        int day = Integer.parseInt(splits[1]);
        int year = Integer.parseInt(splits[2]);
        Calendar a = Calendar.getInstance();
        a.set(Calendar.MONTH, month);
        a.set(Calendar.DAY_OF_MONTH, day);
        a.set(Calendar.YEAR, year);
        Calendar b = Calendar.getInstance();

        int age = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) ||
                (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
            age--;
        }
        Log.d(TAG, "Age: " + age);
        return age;
    }


    public class InputSessionTask extends AsyncTask<Long, Void, Void> {

        protected Void doInBackground(Long... times) {
            double distance = Double.longBitsToDouble(times[2]);
            long time = times[1] - times[0];
            Log.d(TAG, "Time distance: " + time + " " + distance);
            int calories = calculateActivityCalories(time, distance);
            // set that there is an activity today
            FitTracker.mSettings.edit().putBoolean(Constants.ACTIVITY_YET_TODAY, true).apply();
            // PARSE
            ParseUser user = ParseUser.getCurrentUser();
            Run run = new Run();
            //boolean outOfMemory =
            /*while (outOfMemory) {
                run = new Run();
                reduceArrayList(coordinateList);
                outOfMemory = run.setCoordinates(coordinateList);
            }*/
            run.setCalories(calories);
            Log.d(TAG, "Calories set");
            run.setStartTime(times[0]);
            Log.d(TAG, "Start time set");
            run.setEndTime(times[1]);
            Log.d(TAG, "End time set");
            run.setDistance(distance);
            Log.d(TAG, "Distance set");
            run.setACL(new ParseACL(user));
            Log.d(TAG, "ACL set");

            byte workoutType = (byte) (long) times[3];

            run.setActivityType(workoutType);
            Log.d(TAG, "Activity type set");
            user.add(Constants.RUNS_KEY, run);
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

            String sWorkoutType = "";

            switch (workoutType) {
                case 0:
                    sWorkoutType = FitnessActivities.WALKING;
                    break;
                case 1:
                    sWorkoutType = FitnessActivities.RUNNING;
                    break;
                case 2:
                    sWorkoutType = FitnessActivities.BIKING;
            }

            Session session = new Session.Builder()
                    .setName(workoutType + month + "-" + day + ":" + hour + ":" + minute)
                    .setIdentifier(RunDataActivity.IDENTIFIER)
                    .setActivity(sWorkoutType)
                    .setStartTime(times[0], TimeUnit.MILLISECONDS)
                    .setEndTime(times[1], TimeUnit.MILLISECONDS)
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
                    Fitness.SessionsApi.insertSession(mClient, insertRequest)
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
}
