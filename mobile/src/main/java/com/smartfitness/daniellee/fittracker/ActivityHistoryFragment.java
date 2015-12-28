package com.smartfitness.daniellee.fittracker;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 */
public class ActivityHistoryFragment extends Fragment {

    private static final String TAG = ActivityHistoryFragment.class.getSimpleName();


    ArrayList<Run> runs;

    ActivityHistoryAdapter adapter;

    private static ProgressDialog mProgress;

    CardView cardView;

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
        registerForContextMenu(listView);
        ParseUser user = ParseUser.getCurrentUser();
        runs = (ArrayList<Run>) user.get(Constants.RUNS_KEY);
        cardView = (CardView) view.findViewById(R.id.noActivitiesCardView);

        Run[] runData = new Run[runs.size()];
        ParseQuery<Run> query = new ParseQuery<>(Run.class);
        int iii = 0;
        try {
            for (Run run : runs) {
                runData[iii] = query.get(run.getObjectId());
                iii++;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (runs.size() == 0) {
            cardView.setVisibility(CardView.VISIBLE);
        } else {
            adapter = new ActivityHistoryAdapter(getActivity(), R.layout.activity_history_list_item, runData);
            listView.setAdapter(adapter);
        }


        Toolbar toolbar = (Toolbar) view.findViewById(R.id.activity_history_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.activityList) {
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.menu_activity_item, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_edit:
                return true;
            case R.id.action_delete:
                Log.d(TAG, info.id + " " + info.position);
                mProgress = new ProgressDialog(getActivity());
                mProgress.setMessage("Deleting activity...");
                mProgress.show();
                deleteItem(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteItem(int position) {
        ParseQuery<Run> query = ParseQuery.getQuery(Run.class);
        try {
            Run data = query.get(runs.get(position).getObjectId());
            runs.remove(position);
            if (runs.size() > 0) {
                adapter.addAll(runs);
                adapter.notifyDataSetChanged();
            } else {
                cardView.setVisibility(CardView.VISIBLE);
            }
            // update runs list in parse
            ParseUser user = ParseUser.getCurrentUser();
            user.removeAll(Constants.RUNS_KEY, Arrays.asList(data));
            data.deleteInBackground();
            mProgress.dismiss();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
