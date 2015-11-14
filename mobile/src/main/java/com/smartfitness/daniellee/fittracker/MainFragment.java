package com.smartfitness.daniellee.fittracker;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.florent37.materialviewpager.header.HeaderDesign;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements StepsFragment.OnFragmentInteractionListener, SleepFragment.OnFragmentInteractionListener, TrackFragment.OnFragmentInteractionListener {

    SectionsPagerAdapter mSectionsPagerAdapter;
    public static SleepFragment mSleepFragment;
    private static StepsFragment sf = null;

    private MaterialViewPager mViewPager;

    private OnFragmentInteractionListener mListener;

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main, container, false);

        mSectionsPagerAdapter = new SectionsPagerAdapter(this.getChildFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (MaterialViewPager) v.findViewById(R.id.materialViewPager);
        ViewPager viewPager = mViewPager.getViewPager();
        viewPager.setAdapter(mSectionsPagerAdapter);
        viewPager.setCurrentItem(1);
        mViewPager.getPagerTitleStrip().setViewPager(mViewPager.getViewPager());

        Log.d("ASDF", "asdfasdfasdfasdfasdfa");

        // get pagertabstrip and set indicator color
        /*PagerTabStrip pts = (PagerTabStrip) v.findViewById(R.id.pagerTabStrip);
        pts.setTabIndicatorColorResource(R.color.accentColor);*/


        /*mViewPager.setMaterialViewPagerListener(new MaterialViewPager.Listener() {
            @Override
            public HeaderDesign getHeaderDesign(int page) {
                switch (page) {
                    case 0:
                        return HeaderDesign.fromColorResAndDrawable(
                                R.color.accentColor, getResources().getDrawable(R.drawable.header_image_pink));
                    case 1:
                        return HeaderDesign.fromColorResAndDrawable(
                                R.color.colorPrimary, getResources().getDrawable(R.drawable.header_image_blue));
                    case 2:
                        return HeaderDesign.fromColorResAndDrawable(
                                R.color.colorPrimaryDark, getResources().getDrawable(R.drawable.header_image_night));
                }

                //execute others actions if needed (ex : modify your header logo)

                return null;
            }
        });*/
        return v;
    }

    private void setupToolbar() {
        if (mViewPager != null) {
            Toolbar toolbar = mViewPager.getToolbar();
            Log.d("MainFragment", "setupToolbar");

            if (toolbar != null) {
                ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
                Log.d("MainFragment", "setupToolbarInner");

                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setHomeButtonEnabled(true);
                actionBar.setTitle("");

            }
            getActivity().invalidateOptionsMenu();
            ((MainActivity)getActivity()).mDrawerToggle.syncState();
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MainFragment", "onResume");
        setupToolbar();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d("MainFragment", "onAttachContext");
        Activity activity = null;

        if (context instanceof Activity){
            activity = (Activity) context;
        }
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("MainFragment", "onAttachActivity");
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return TrackFragment.newInstance();
            } else if (position == 1) {
                return sf = StepsFragment.newInstance();
            } else if (position == 2) {
                return mSleepFragment = SleepFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
