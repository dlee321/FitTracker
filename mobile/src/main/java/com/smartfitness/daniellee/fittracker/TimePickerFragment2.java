package com.smartfitness.daniellee.fittracker;


import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class TimePickerFragment2 extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    EditText mTextView;

    public TimePickerFragment2(EditText e) {
        mTextView = e;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        Calendar time = (Calendar) bundle.getSerializable(Constants.TIME_PICKER_FRAGMENT_TIME);
        int hour = time.get(Calendar.HOUR_OF_DAY);
        int minute = time.get(Calendar.MINUTE);

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
        mTextView.setText(time);
    }
}
