package com.smartfitness.daniellee.fittracker;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.ParseUser;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ActivityHistoryFragment extends Fragment {

    private static final String TAG = ActivityHistoryFragment.class.getSimpleName();


    public ActivityHistoryFragment() {
        // Required empty public constructor
    }

    public static ActivityHistoryFragment newInstance() {
        return new ActivityHistoryFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activity_history, container, false);
        ListView listView = (ListView) view.findViewById(R.id.activityList);
        ParseUser user = ParseUser.getCurrentUser();
        ArrayList<Run> runs = (ArrayList<Run>) user.get(getString(R.string.run_key));
        if (runs.size() == 0) {
            CardView cardView = (CardView) view.findViewById(R.id.noActivitiesCardView);
            cardView.setVisibility(CardView.VISIBLE);
        } else {
            ActivityHistoryAdapter adapter = new ActivityHistoryAdapter(getActivity(), R.layout.activity_list_item, runs);
            listView.setAdapter(adapter);
        }
        return view;
    }


}
