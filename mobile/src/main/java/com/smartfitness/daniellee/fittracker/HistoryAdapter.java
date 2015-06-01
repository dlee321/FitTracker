package com.smartfitness.daniellee.fittracker;

import android.app.Activity;
import android.content.Context;
import android.nfc.Tag;
import android.support.v7.widget.CardView;
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
            Log.d(TAG, "GetView " + position + " " + (convertView == null) + "  " + data);
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(mLayoutResource, null);

                holder = new ViewHolder();
                holder.circleView = (CircleView2) convertView.findViewById(R.id.circleView);
                holder.position = position;
                holder.height = convertView.getHeight();
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            int parentHeight = holder.height;
            int circleHeight = holder.height;
            int circleWidth = holder.height;
            int margin = (parentHeight - circleHeight) / 2;
            Log.d(TAG, parentHeight + " " + circleHeight + " " + circleWidth + "  " + margin);
            Log.d(TAG, "" + holder.circleView.toString());
            holder.circleView.layout(margin, margin, margin + circleWidth, margin + circleHeight);
            holder.circleView.setStepsString(data);
            holder.circleView.invalidate();
            return convertView;
        } catch (NullPointerException e) {
            return null;
        }
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
        int height;
    }
}
