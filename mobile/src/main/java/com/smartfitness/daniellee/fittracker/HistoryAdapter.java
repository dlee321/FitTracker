package com.smartfitness.daniellee.fittracker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;


public class HistoryAdapter extends ArrayAdapter<Integer> {

    private static final String TAG = HistoryAdapter.class.getSimpleName();

    Context mContext;
    int mLayoutResource;
    Integer[] mObjects;
    Sleep[] mSleepData;
    int count;
    int circleSize;

    public HistoryAdapter(Context context, int resource, Integer[] objects, Sleep[] sleepData, int width) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResource = resource;
        mObjects = objects;
        mSleepData = sleepData;
        circleSize = width;
        count = mObjects.length;
        for (int iii = mObjects.length - 1; objects[iii] == null; iii--) {
            count--;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int data = mObjects[count - position - 1];
        Log.d(TAG, "GetView " + position + " " + (convertView == null) + "  " + data);
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mLayoutResource, null);

            holder = new ViewHolder();
            holder.circleView = (CircleView2) convertView.findViewById(R.id.circleView);
            holder.circleViewSleep = (CircleView3) convertView.findViewById(R.id.circleViewSleep);
            holder.position = position;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        int circleHeight = circleSize;
        int circleWidth = circleSize;
        //int margin = (parentHeight - circleHeight) / 2;
        Log.d(TAG, circleHeight + " " + circleWidth);
        Log.d(TAG, "" + holder.circleView.toString());
        //holder.circleView.layout(margin, margin, margin + circleWidth, margin + circleHeight);
        holder.circleView.setStepsString(data);
        holder.circleView.getLayoutParams().height = circleHeight;
        holder.circleView.getLayoutParams().width = circleWidth;
        holder.circleView.setLayoutParams(holder.circleView.getLayoutParams());
        holder.circleView.invalidate();


        holder.circleViewSleep.reset();

        holder.circleViewSleep.getLayoutParams().height = circleHeight;
        holder.circleViewSleep.getLayoutParams().width = circleWidth;
        holder.circleViewSleep.setLayoutParams(holder.circleViewSleep.getLayoutParams());

        Sleep sleep = mSleepData[position];
        Log.d(TAG, position + " " + (sleep == null));
        if (sleep != null) {
            int duration = sleep.getDuration();
            String durationString = calculateTimeString(duration);
            holder.circleViewSleep.setSleepTime(duration);
            holder.circleViewSleep.setSleepTimeString(durationString);
            holder.circleViewSleep.invalidate();
        }

        return convertView;
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

    @Override
    public int getCount() {
        //Log.d("HistoryAdapter", "getCount");
        return count;
    }

    @Override
    public Integer getItem(int position) {
        return mObjects[position];
    }

    static class ViewHolder {
        CircleView2 circleView;
        int position;
        public CircleView3 circleViewSleep;
    }
}
