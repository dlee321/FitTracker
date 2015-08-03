package com.smartfitness.daniellee.fittracker;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class NavigationAdapter extends ArrayAdapter {

    static final int LIST_SIZE = 3;

    private static final String TAG = HistoryAdapter.class.getSimpleName();

    Context mContext;
    int mLayoutResource;
    String[] labels;

    public NavigationAdapter(Context context, int resource, String[] objects) {
        super(context, resource);
        mContext = context;
        mLayoutResource = resource;
        labels = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mLayoutResource, null);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.navigationImageView);
        TextView textView = (TextView) convertView.findViewById(R.id.navigationTextView);
        int imageId;
        String text;
        switch (position) {
            case 0: imageId = R.drawable.home;
                text = labels[0];
                break;
            case 1: imageId = R.drawable.history;
                text = labels[1];
                break;
            case 2: imageId = R.drawable.activities;
                text = labels[2];
                break;
            default: imageId = 0;
                text = "";
        }
        imageView.setImageResource(imageId);
        textView.setText(text);
        return convertView;
    }

    @Override
    public int getCount() {
        //Log.d("HistoryAdapter", "getCount");
        return LIST_SIZE;
    }

    @Override
    public String getItem(int position) {
        return labels[position];
    }
}
