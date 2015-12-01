package com.meeple.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by harry on 9/25/15.
 */


public class Utils {

    public static boolean isValidEmail(String target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void saveToUserDefaults(Context context, String key,
                                          String value) {
        Log.d("Utils", "Saving:" + key + ":" + value);
        SharedPreferences preferences = context.getSharedPreferences(
                Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void saveLatLong(Context context, String key,
                                   double value) {
        Log.d("Utils", "Saving:" + key + ":" + value);
        SharedPreferences preferences = context.getSharedPreferences(
                Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(key, Double.doubleToLongBits(value));
        editor.commit();
    }

    public static Double getLatLong(Context context, String key) {

        SharedPreferences preferences = context.getSharedPreferences(
                Constant.SHARED_PREFS, Context.MODE_PRIVATE);

        return Double.longBitsToDouble(preferences.getLong(key, 0));

    }

    public static void clearUserDefaults(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        preferences.edit().clear().commit();

    }

    public static String getFromUserDefaults(Context context, String key) {

        SharedPreferences preferences = context.getSharedPreferences(
                Constant.SHARED_PREFS, Context.MODE_PRIVATE);
        return preferences.getString(key, "");

    }

    public static String getDateinWords(String startDate) {

        String time = null;
        Date date1 = null;
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        format1.setTimeZone(utcZone);

        try {
            date1 = format1.parse(startDate);
        } catch (android.net.ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        SimpleDateFormat inWordsFormat = new SimpleDateFormat("dd MMM,hh:mm aa");
        try {
            time = inWordsFormat.format(date1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

//    public static String getTimeLocally(String inputTime){
//        String time = null;
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z", Locale.getDefault());
//        Date date = new Date();
//        try {
//            date = sdf.parse(inputTime);
//            SimpleDateFormat timeFormatter = new SimpleDateFormat("dd MMM,hh:mm aa");
//            time = timeFormatter.format(date);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return time;
//    }

    public static String getDeviceTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd MMM,hh:mm aa");
        String formattedDate = df.format(c.getTime());
        Log.e("Device Time",formattedDate);
        return formattedDate;
    }


}
