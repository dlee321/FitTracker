package com.smartfitness.daniellee.fittracker;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by danie_000 on 11/23/2015.
 */
public class SleepHistoryAdapter extends RecyclerView.Adapter<SleepHistoryAdapter.ViewHolder> {


    int[] mDuration;
    int[] mDeepSleep;
    Date[] mDates;
    int length;
    Context mContext;

    Calendar mCalendar;
    long timeMillis;
    SimpleDateFormat mFormat;

    public SleepHistoryAdapter(int[] duration, int[] deepSleep, Date[] dates, Context context) {
        mDuration = duration;
        mDeepSleep = deepSleep;
        mDates = dates;
        length = mDuration.length;
        mContext = context;

        mCalendar = Calendar.getInstance();
        timeMillis = mCalendar.getTimeInMillis();
        mFormat = new SimpleDateFormat("MMMM dd");
        mFormat.setTimeZone(TimeZone.getDefault());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mSleepTextView;
        public TextView mDateTextView;
        public HorizontalProgress mHorizontalProgress;
        public CardView mCardView;

        public ViewHolder(View v) {
            super(v);
            mSleepTextView = (TextView) v.findViewById(R.id.list_sleep_data);
            mDateTextView = (TextView) v.findViewById(R.id.sleep_history_date);
            mHorizontalProgress = (HorizontalProgress) v.findViewById(R.id.sleep_horizontal_progress);
            mCardView = (CardView) v.findViewById(R.id.card_list_item_sleep);
        }
    }

    @Override
    public SleepHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sleep_list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d("asdf", "asdff");
        int duration = mDuration[length - position - 1];
        int deepSleep = mDeepSleep[length - position - 1];
        Date date = mDates[length - position - 1];
        
        holder.mSleepTextView.setText(mContext.getString(R.string.sleep_list_text,
                calculateTimeString(duration), calculateTimeString(deepSleep)));
        holder.mHorizontalProgress.setPartDone((float) deepSleep / duration);
        holder.mHorizontalProgress.invalidate();

        mCalendar.setTime(date);
        mCalendar.add(Calendar.DATE, -1);
        String dateString = mFormat.format(mCalendar.getTime());
        holder.mDateTextView.setText(dateString);

        if ((position) % 2 == 0) {
            holder.mCardView.setCardBackgroundColor(Color.WHITE);
        } else {
            holder.mCardView.setCardBackgroundColor(Color.parseColor("#efefef"));
        }
    }

    @Override
    public int getItemCount() {
        return length;
    }

    private String calculateTimeString(int minutes) {
        int hours = minutes / 60;
        int addedMinutes = minutes % 60;
        String sleepText = hours + ":";
        if (addedMinutes >= 10) {
            sleepText += addedMinutes;
        } else {
            sleepText += "0" + addedMinutes;
        }
        return sleepText;
    }

}
