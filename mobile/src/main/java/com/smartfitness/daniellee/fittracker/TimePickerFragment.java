package com.smartfitness.daniellee.fittracker;


import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.TimePicker;

import android.text.format.DateFormat;


/**
 * A simple {@link Fragment} subclass.
 */
public class TimePickerFragment extends DialogFragment implements
        TimePickerDialog.OnTimeSetListener {

    TextView mAlarmTextView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mAlarmTextView = (TextView) getActivity().findViewById(R.id.alarmTextView);
        String[] splits = mAlarmTextView.getText().toString().split(":");
        int hour = Integer.parseInt(splits[0]);
        int minute = Integer.parseInt(splits[1]);

        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i2) {
        String hour = "" + i;
        String minute;
        if (i2 < 10) {
            minute = "0" + i2;
        } else {
            minute = "" + i2;
        }
        String time = hour + ":" + minute;
        FitTracker.mSettings.edit().putString(SleepFragment.ALARM_TIME_TAG, time);
        mAlarmTextView.setText(time);
    }
}
