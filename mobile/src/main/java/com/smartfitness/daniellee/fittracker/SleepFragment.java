package com.smartfitness.daniellee.fittracker;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SleepFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SleepFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SleepFragment extends android.support.v4.app.Fragment {

    protected static final String ALARM_TIME_TAG = "alarmtime";

    private OnFragmentInteractionListener mListener;

    TextView sleepTextView;
    TextView alarmTextView;

    RelativeLayout layout;

    //private Intent mServiceIntent;

    public static SleepFragment newInstance() {
        SleepFragment fragment = new SleepFragment();
        return fragment;
    }

    public SleepFragment() {
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
        View v = inflater.inflate(R.layout.fragment_sleep, container, false);
        layout = (RelativeLayout)v.findViewById(R.id.relativeLayout);

        alarmTextView = (TextView)v.findViewById(R.id.alarmTextView);

        if (MainActivity.mSettings == null) {
            MainActivity.mSettings = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        }
        String alarm = null;
        if ((alarm = (MainActivity.mSettings.getString(ALARM_TIME_TAG, ""))).equals("")) {
            MainActivity.mSettings.edit().putString(ALARM_TIME_TAG, getActivity().getString(R.string.default_alarm_time)).apply();
            alarmTextView.setText(R.string.default_alarm_time);
        } else {
            alarmTextView.setText(alarm);
        }

        alarmTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
            }
        });

        sleepTextView = (TextView)v.findViewById(R.id.sleepTextView);
        sleepTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                int daysCalibrated = MainActivity.mSettings.getInt(Keys.DAYS_CALIBRATED, -1);
                Log.d("SleepFragment", daysCalibrated + "");
                if (daysCalibrated == -1 || daysCalibrated >= 7) {
                    intent = new Intent(getActivity(), CalibrateActivity.class);
                } else {
                    intent = new Intent(getActivity(), SleepActivity.class);
                }
                intent.putExtra(ALARM_TIME_TAG, alarmTextView.getText());
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });

        return v;
    }

    /*public Intent getIntent() {
        return mServiceIntent;
    }*/

    private void showTimePicker() {
        DialogFragment fragment = new TimePickerFragment();
        fragment.show(getFragmentManager(), "timePicker");
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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

}
