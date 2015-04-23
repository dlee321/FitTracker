package com.smartfitness.daniellee.fittracker;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


public class ActivityHistoryAdapter extends ArrayAdapter<Run> {

    Context mContext;
    int mLayoutResource;
    ArrayList<Run> mObjects;
    int count;

    public ActivityHistoryAdapter(Context context, int resource, ArrayList<Run> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResource = resource;
        mObjects = objects;
        count = mObjects.size();
        for (int iii = mObjects.size() - 1; objects.get(iii) == null; iii--) {
            count--;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Run data = mObjects.get(count - position - 1);
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(mLayoutResource, parent, false);
        }
        CardView cardView = (CardView) convertView.findViewById(R.id.activityCardView);
        ImageView imageView = (ImageView) cardView.findViewById(R.id.activityImage);
        TextView durationTextView = (TextView) cardView.findViewById(R.id.listTimeTextView);
        TextView distanceTextView = (TextView) cardView.findViewById(R.id.listDistanceTextView);
        TextView paceTextView = (TextView) cardView.findViewById(R.id.listPaceTextView);
        TextView calorieTextView = (TextView) cardView.findViewById(R.id.listCalorieTextView);
        TextView dateTextView = (TextView) cardView.findViewById(R.id.listDateTextView);

        if (data.getActivityType() == Run.WALKING) {
            imageView.setImageResource(R.drawable.walking);
        } else if (data.getActivityType() == Run.RUNNING) {
            imageView.setImageResource(R.drawable.running);
        }

        long duration = data.getEndTime() - data.getStartTime();
        double distance = data.getDistance();

        durationTextView.setText(convertToTimeString(duration));
        distanceTextView.setText((((double)Math.round(distance*100.0))/100.0) + "\nmi");
        paceTextView.setText(convertToTimeString(Math.round(duration/distance)));
        calorieTextView.setText("" + Math.round(data.getCalories()));

        String date = calculateDate(data.getStartTime());
        dateTextView.setText(date);

        return convertView;
    }

    private String calculateDate(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        return format.format(cal.getTime());
    }

    private static String convertToTimeString (long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long secondsRemainder = seconds % 60;
        String timeString = minutes + ":";
        if (minutes >= 60) {
            long hours = minutes / 60;
            long minutesRemainder = minutes % 60;
            timeString = hours + ":";
            if (minutesRemainder < 10) {
                timeString += 0;
            }
            timeString += minutesRemainder + ":";
        }
        if (secondsRemainder < 10) {
            timeString += 0;
        }
        timeString += secondsRemainder;
        return timeString;
    }


    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Run getItem(int position) {
        return mObjects.get(position);
    }
}
