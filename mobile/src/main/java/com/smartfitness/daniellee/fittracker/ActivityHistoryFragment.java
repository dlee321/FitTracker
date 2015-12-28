package com.smartfitness.daniellee.fittracker;


import android.app.ProgressDialog;
import android.os.AsyncTask;
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


    ArrayList<Run> mRuns;

    ActivityHistoryAdapter adapter;

    private static ProgressDialog mProgress;

    CardView mCardView;


    ListView mListView;

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

        mProgress = new ProgressDialog(getActivity());
        mProgress.setMessage("Loading Activity History....");
        mProgress.show();

        mListView = (ListView) view.findViewById(R.id.activityList);
        registerForContextMenu(mListView);
        ParseUser user = ParseUser.getCurrentUser();
        mRuns = (ArrayList<Run>) user.get(Constants.RUNS_KEY);
        mCardView = (CardView) view.findViewById(R.id.noActivitiesCardView);

        new ParseRunDataTask().execute();


        /*Run[] runData = new Run[mRuns.size()];
        ParseQuery<Run> query = new ParseQuery<>(Run.class);
        int iii = 0;
        try {
            for (Run run : mRuns) {
                runData[iii] = query.get(run.getObjectId());
                iii++;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (mRuns.size() == 0) {
            mCardView.setVisibility(CardView.VISIBLE);
        } else {
            adapter = new ActivityHistoryAdapter(getActivity(), R.layout.activity_history_list_item, runData);
            listView.setAdapter(adapter);
        }*/


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

    /*@Override
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
            Run data = query.get(mRuns.get(position).getObjectId());
            mRuns.remove(position);
            if (mRuns.size() > 0) {
                adapter.addAll(mRuns);
                adapter.notifyDataSetChanged();
            } else {
                mCardView.setVisibility(CardView.VISIBLE);
            }
            // update mRuns list in parse
            ParseUser user = ParseUser.getCurrentUser();
            user.removeAll(Constants.RUNS_KEY, Arrays.asList(data));
            data.deleteInBackground();
            mProgress.dismiss();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }*/

    private class ParseRunDataTask extends AsyncTask<Void, Void, Run[]> {
        @Override
        protected Run[] doInBackground(Void... params) {
            Run[] runData = new Run[mRuns.size()];
            ParseQuery<Run> query = new ParseQuery<>(Run.class);
            int iii = 0;
            try {
                for (Run run : mRuns) {
                    runData[iii] = query.get(run.getObjectId());
                    iii++;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return runData;
        }

        @Override
        protected void onPostExecute(Run[] runData) {
            if (mRuns.size() == 0) {
                mCardView.setVisibility(CardView.VISIBLE);
            } else {
                adapter = new ActivityHistoryAdapter(getActivity(), R.layout.activity_history_list_item, runData);
                mListView.setAdapter(adapter);
            }
            mProgress.dismiss();
        }
    }
}
