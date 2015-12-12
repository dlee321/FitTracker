package com.smartfitness.daniellee.fittracker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.ViewHolder> {

    private static final int TYPE_HEADER= 0;
    private static final int TYPE_ITEM = 1;

    String[] mTitles;
    int[] mIcons;
    String mName;
    String mEmail;
    int mProfile;

    MainActivity instance;
    public NavigationAdapter(String[] titles, int[] icons, String name, String email, int profile, MainActivity ma) {
        mTitles = titles;
        mIcons = icons;
        mName = name;
        mEmail = email;
        mProfile = profile;

        instance = ma;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        int holderId;

        LinearLayout mItem;

        TextView textView;
        ImageView icon;

        ImageView profile;
        TextView name;
        TextView email;


        public NavigationViewHolderClicks mListener;

        public ViewHolder(View itemView, int ViewType, NavigationViewHolderClicks listener) {
            super(itemView);

            mListener = listener;

            if (ViewType == TYPE_ITEM) {
                mItem = (LinearLayout) itemView.findViewById(R.id.listItem);
                mItem.setOnClickListener(this);
                textView = (TextView) itemView.findViewById(R.id.navigationTextView);
                icon = (ImageView) itemView.findViewById(R.id.navigationImageView);
                holderId = 1;
            } else {
                name = (TextView) itemView.findViewById(R.id.name);
                email = (TextView) itemView.findViewById(R.id.email);
                profile = (ImageView) itemView.findViewById(R.id.profile_image);
                holderId = 0;
            }
        }


        @Override
        public void onClick(View v) {
            mListener.click(getAdapterPosition());
        }

        public static interface NavigationViewHolderClicks {
            void click(int position);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_list_item, parent, false);
            ViewHolder vhItem = new ViewHolder(v, viewType, new ViewHolder.NavigationViewHolderClicks() {
                @Override
                public void click(int position) {
                    instance.onItemClick(position);
                }
            });
            return vhItem;
        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header, parent, false);
            ViewHolder vhHeader = new ViewHolder(v, viewType, new ViewHolder.NavigationViewHolderClicks() {
                @Override
                public void click(int position) {
                    instance.onItemClick(position);
                }
            });
            return vhHeader;
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (holder.holderId == 1) {
            holder.textView.setText(mTitles[position - 1]);
            holder.icon.setImageResource(mIcons[position - 1]);
        } else {
            holder.profile.setImageResource(mProfile);
            holder.name.setText(mName);
            holder.email.setText(mEmail);
        }
    }

    @Override
    public int getItemCount() {
        return mTitles.length + 1;
    }
}