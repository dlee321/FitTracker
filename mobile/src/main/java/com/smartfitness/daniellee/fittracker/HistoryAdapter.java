package com.smartfitness.daniellee.fittracker;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;


public class HistoryAdapter extends ArrayAdapter<Integer> {

    Context mContext;
    int mLayoutResource;
    Integer[] mObjects;
    int count;

    public HistoryAdapter(Context context, int resource, Integer[] objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResource = resource;
        mObjects = objects;
        count = mObjects.length;
        for (int iii = mObjects.length - 1; objects[iii] == null; iii--) {
            count--;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            int data = mObjects[count - position - 1];
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(mLayoutResource, parent, false);
            }
            CardView cardView = (CardView) convertView.findViewById(R.id.cardView);
            CircleView2 circleView = (CircleView2) cardView.findViewById(R.id.circleView);
            int parentHeight = cardView.getHeight();
            int circleHeight = cardView.getHeight();
            int circleWidth = cardView.getHeight();
            int margin = (parentHeight - circleHeight) / 2;
            circleView.layout(margin, margin, margin + circleWidth, margin + circleHeight);
            circleView.setStepsString(data);
            circleView.invalidate();
            return convertView;
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Override
    public int getCount() {
        Log.d("HistoryAdapter", "getCount");
        return count;
    }

    @Override
    public Integer getItem(int position) {
        return mObjects[position];
    }
}
