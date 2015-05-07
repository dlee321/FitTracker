package com.smartfitness.daniellee.fittracker;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;


public class MainActivity extends ActionBarActivity implements MainFragment.OnFragmentInteractionListener, StepsFragment.OnFragmentInteractionListener, SleepFragment.OnFragmentInteractionListener, TrackFragment.OnFragmentInteractionListener, AdapterView.OnItemClickListener, HistoryFragment.OnFragmentInteractionListener {

    public static final String[] DRAWER_LIST_ITEMS = new String[] {"Home", "History", "Activities"};

    public static final String PREFS_NAME = "MyPrefsData";

    public static final String TAG = MainActivity.class.getSimpleName();
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    static SharedPreferences mSettings;

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    private DrawerLayout mDrawerLayout;

    // ActionBar toggle for drawerlayout
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;

    FragmentManager mFragmentManager;

    private String currentFragmentName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSettings = getSharedPreferences(PREFS_NAME, 0);

        // if currently sleep tracking, start SleepActivity
        if (isMyServiceRunning(SleepService.class)) {
            Intent intent = new Intent(this, SleepActivity.class);
            startActivity(intent);
        }

        // START MAINFRAGMENT **********************
        MainFragment fragment = MainFragment.newInstance();
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        currentFragmentName = DRAWER_LIST_ITEMS[0];

        // Setup Navigation Drawer
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                int p = mDrawerList.getCheckedItemPosition();
                if (p >= 0 && p < DRAWER_LIST_ITEMS.length) {
                    getSupportActionBar().setTitle(DRAWER_LIST_ITEMS[p]);
                } else if (p < 0) {
                    getSupportActionBar().setTitle(currentFragmentName);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(R.string.drawer_title);
            }
        };

        populateDrawer();


        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        // Set up the action bar.
        //final ActionBar actionBar = getSupportActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.


        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        /*mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });*/

        // For each of the sections in the app, add a tab to the action bar.
        /*for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }*/
    }

    private void populateDrawer() {
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, DRAWER_LIST_ITEMS));
        mDrawerList.setOnItemClickListener(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     *  Build a {@link GoogleApiClient} that will authenticate the user and allow the application
     *  to connect to Fitness APIs. The scopes included should match the scopes your app needs
     *  (see documentation for details). Authentication will occasionally fail intentionally,
     *  and in those cases, there will be a known resolution, which the OnConnectionFailedListener()
     *  can address. Examples of this include the user never having signed in before, or having
     *  multiple accounts on the device and needing to specify which account to use, etc.
     */


    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Fragment fragment = null;
        getSupportActionBar().setTitle(DRAWER_LIST_ITEMS[i]);
        if (i == 0) {
            fragment = MainFragment.newInstance();
        } else if (i == 1) {
            fragment = HistoryFragment.newInstance();
        } else if (i == 2) {
            fragment = ActivityHistoryFragment.newInstance();
        }
        currentFragmentName = DRAWER_LIST_ITEMS[i];
        mFragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        mDrawerList.setItemChecked(i, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
