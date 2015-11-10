package com.wavlite.WLAndroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;

/**
 * Created by javen on 11/10/15.
 */
public class TrialPeriodTimer  {

    private static TrialPeriodTimer userInstance = null;

//    private TrialPeriodTimer(){}

    public static TrialPeriodTimer getUserInstance() {

        if (userInstance == null) {
            userInstance = new TrialPeriodTimer();
        }

        return userInstance;
    }

    public void setStartDate(Date startDate, Context context) {

        long millis = startDate.getTime();
        saveDataToSharedPrefs("STARTDATE",millis, context);
        setEndDate(startDate, context);
        Log.d("TRIAL", "Start in millis: " + millis);
    }
    private void setEndDate(Date startDate, Context context) {

        long millis = startDate.getTime() + 691200000;
        saveDataToSharedPrefs("ENDDATE", millis,context);
        Log.d("TRIAL", "End date in Millis: " + millis);
    }

    private void saveDataToSharedPrefs(String KEY,Long dateInMillis, Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putLong(KEY, dateInMillis).apply();
    }
}
