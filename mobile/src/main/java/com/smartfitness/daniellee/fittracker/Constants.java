package com.smartfitness.daniellee.fittracker;

/**
 * Created by danie_000 on 5/6/2015.
 */
public class Constants {

    // stored constants
    public static final boolean MALE = true;
    public static final boolean FEMALE = false;

    public static final String RUNS_KEY = "runs";
    public static final String SLEEPS_KEY = "sleep";

    // Run ParseObject keys
    public static final String ACTIVITY_TYPE = "activitytype",
            DESCRIPTION = "description",
            NOTES = "notes",
            DISTANCE = "distance",
            COORDINATES = "coordinates",
            CALORIES = "calories",
            START_TIME = "startttime",
            END_TIME = "endtime";

    // Sleep ParseObject keys
    public static final String START = "start",
            END = "end",
            VALUES = "values";

    // Shared Preferences keys
    public static final String SENSORX = "minX",
            SENSORY = "minY",
            SENSORZ = "minZ",
            DAYS_CALIBRATED = "dayscalibrated",

            DISABLE_ALARM = "disablealarm",
            SMART_ALARM_TIME_INDEX = "smartalarmtime";

    public static final String STEP_PREF = "stepdata";
    public static final String TIME_PREF = "time",
            ACTIVITY_YET_TODAY = "actvitiytoday";

    // ParseUser data keys
    public static final String WEIGHT_TAG = "weight",
            HEIGHT_FEET_TAG = "heightft",
            HEIGHT_INCH_TAG = "heightin",
            DATE_BIRTH_TAG = "dateofbirth",
            AGE_TAG = "age",
            GENDER_TAG="gender";


    public static final String[] SMART_ALARM_TIMES = new String[]{"30", "25", "20", "15", "10", "None"};
}
