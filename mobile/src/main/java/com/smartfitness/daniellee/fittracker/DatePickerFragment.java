package com.smartfitness.daniellee.fittracker;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    public static final String YEAR_PREF = "year";
    public static final String MONTH_PREF = "month";
    public static final String DAY_PREF = "day";

    EditText mEditText;


    public DatePickerFragment() {
        // Required empty public constructor
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mEditText = (EditText) getActivity().findViewById(R.id.birthEditText);

        DatePickerDialog dialog;

        if (mEditText.getText().toString().equals("")) {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            int year = c.get(Calendar.YEAR) - 30 + 1;
            dialog = new DatePickerDialog(getActivity(), this, year, 0, 1);
        } else {
            Log.d("DatePickerFragment", mEditText.getText().toString());
            String[] splits = mEditText.getText().toString().split("-");
            int month = Integer.parseInt(splits[0]) - 1;
            int day = Integer.parseInt(splits[1]);
            int year = Integer.parseInt(splits[2]);
            dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        }
        return dialog;
    }


    @Override
    public void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
        SharedPreferences preferences = getActivity().getSharedPreferences(FitTracker.PREFS_NAME, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(YEAR_PREF, i);
        editor.putInt(MONTH_PREF, i2 + 1);
        editor.putInt(DAY_PREF, i3);

        editor.apply();

        mEditText.setText((i2 + 1) + "-" + i3 + "-" + i);
    }
}
