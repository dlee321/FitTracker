package com.smartfitness.daniellee.fittracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by danie_000 on 11/20/2015.
 */
public class StepsHistoryAdapter extends RecyclerView.Adapter<StepsHistoryAdapter.ViewHolder> {

    Integer[] mDataSet;
    Calendar mCalendar;
    SimpleDateFormat mFormat;
    long timeMillis;
    int length;

    static Context context;

    public static int textColor = 0;
    static int goal;

    public StepsHistoryAdapter(Integer[] dataSet, Context c) {
        mDataSet = dataSet;
        mCalendar = Calendar.getInstance();
        timeMillis = mCalendar.getTimeInMillis();
        mFormat = new SimpleDateFormat("MMMM dd");

        context = c;
        length = mDataSet.length;

        goal = Integer.parseInt(FitTracker.mSettings.getString(Constants.PREF_STEPS_GOAL, "10000"));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mStepsTextView;
        public TextView mDateTextView;
        public TextView mGoalTextView;
        public ImageView mImageView;
        public HorizontalProgress mHorizontalProgress;
        public CardView mCardView;

        public ViewHolder(View v) {
            super(v);
            mStepsTextView = (TextView) v.findViewById(R.id.listStepCount);
            mDateTextView = (TextView) v.findViewById(R.id.steps_history_date);

            mGoalTextView = (TextView) v.findViewById(R.id.listStepGoal);
            mGoalTextView.setText(context.getString(R.string.steps_list_item_goal, goal));

            mImageView = (ImageView) v.findViewById(R.id.steps_icon);
            mHorizontalProgress = (HorizontalProgress) v.findViewById(R.id.steps_horizontal_progress);
            mCardView = (CardView) v.findViewById(R.id.card_list_item);

            if (textColor == Color.WHITE || textColor == 0) {
                textColor = mStepsTextView.getCurrentTextColor();
            }
        }
    }

    @Override
    public StepsHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.steps_list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Integer data = mDataSet[length-position-1];
        if (data != null) {
            holder.mStepsTextView.setText(String.valueOf(data));
            holder.mHorizontalProgress.setPartDone((float) data / goal);
            holder.mHorizontalProgress.invalidate();

            mCalendar.setTimeInMillis(timeMillis);
            mCalendar.add(Calendar.DATE, 0 - (position + 1));
            String date = mFormat.format(mCalendar.getTime());
            holder.mDateTextView.setText(date);

            if (data >= goal) {
                holder.mStepsTextView.setTextColor(Color.WHITE);
                holder.mDateTextView.setTextColor(Color.WHITE);
                holder.mGoalTextView.setTextColor(Color.WHITE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.mCardView.setCardBackgroundColor(context.getResources().getColor(R.color.colorPrimary, context.getTheme()));
                    holder.mImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.check_circle, context.getTheme()));
                } else {
                    holder.mCardView.setCardBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                    holder.mImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.check_circle));
                }
            } else {
                holder.mStepsTextView.setTextColor(textColor);
                holder.mDateTextView.setTextColor(textColor);
                holder.mGoalTextView.setTextColor(textColor);

                holder.mCardView.setCardBackgroundColor(Color.WHITE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    holder.mImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.steps, context.getTheme()));
                } else {
                    holder.mImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.steps));
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return length;
    }
}
