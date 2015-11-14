package com.smartfitness.daniellee.fittracker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SleepFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SleepFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SleepFragment extends Fragment {

    private ObservableScrollView mScrollView;

    protected static final String ALARM_TIME_TAG = "alarmtime";

    private OnFragmentInteractionListener mListener;

    TextView sleepTextView;
    TextView alarmTextView;
    CheckBox disableAlarmCheckBox;
    Spinner smartAlarmTimeSpinner;

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
        mScrollView = (ObservableScrollView) v.findViewById(R.id.scrollView1);

        MaterialViewPagerHelper.registerScrollView(getActivity(), mScrollView, null);
        //layout = (RelativeLayout)v.findViewById(R.id.relativeLayout);

        alarmTextView = (TextView)v.findViewById(R.id.alarmTextView);

        if (FitTracker.mSettings == null) {
            FitTracker.mSettings = getActivity().getSharedPreferences(FitTracker.PREFS_NAME, 0);
        }
        String alarm = FitTracker.mSettings.getString(ALARM_TIME_TAG, "");
        if (alarm.equals("")) {
            FitTracker.mSettings.edit().putString(ALARM_TIME_TAG, getActivity().getString(R.string.default_alarm_time)).apply();
            alarmTextView.setText(R.string.default_alarm_time);
        } else {
            alarmTextView.setText(alarm);
        }

        final LinearLayout smartAlarmLinearLayout = (LinearLayout) v.findViewById(R.id.smartAlarmLinearLayout);

        disableAlarmCheckBox = (CheckBox) v.findViewById(R.id.disableAlarmCheckBox);

        disableAlarmCheckBox.setChecked(FitTracker.mSettings.getBoolean(Constants.DISABLE_ALARM, false));
        if (disableAlarmCheckBox.isChecked()) {
            smartAlarmLinearLayout.setVisibility(View.INVISIBLE);
        }
        disableAlarmCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    smartAlarmLinearLayout.setVisibility(View.INVISIBLE);
                } else {
                    smartAlarmLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        smartAlarmTimeSpinner = (Spinner) v.findViewById(R.id.smartAlarmTimeSpinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, Constants.SMART_ALARM_TIMES);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        smartAlarmTimeSpinner.setAdapter(spinnerArrayAdapter);
        smartAlarmTimeSpinner.setSelection(FitTracker.mSettings.getInt(Constants.SMART_ALARM_TIME_INDEX, 0));

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
                boolean disableAlarm = disableAlarmCheckBox.isChecked();
                int smartAlarmTimeIndex = smartAlarmTimeSpinner.getSelectedItemPosition();
                SharedPreferences.Editor editor = FitTracker.mSettings.edit();
                editor.putBoolean(Constants.DISABLE_ALARM, disableAlarm);
                editor.putInt(Constants.SMART_ALARM_TIME_INDEX, smartAlarmTimeIndex);
                editor.commit();

                Intent intent;
                int daysCalibrated = FitTracker.mSettings.getInt(Constants.DAYS_CALIBRATED, -1);
                Log.d("SleepFragment", "Days calibrated: " + daysCalibrated);
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
