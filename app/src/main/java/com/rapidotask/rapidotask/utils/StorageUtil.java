package com.rapidotask.rapidotask.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rapidotask.rapidotask.models.Events;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by haseeb on 29/9/17.
 */

public class StorageUtil {

    public static String SETTINGS_NAME = "TRIPVALUES";
    public static String SOURCE_LATLONG = "SOURCE_LATLON";
    public static String DESTINATION_LATLONG = "DESTINATION_LATLON";
    public static String DESTINATION_NAME = "DESTINATION_NAME";
    public static String CURRENT_LATLONG = "CURRENT_LATLON";
    public static String TRIP_STATUS = "TRIP_STATUS";
    public static String SESSION_ID = "SESSION";
    public static String TRIP_ID = "TRIPID";
    public static String EVENTS = "EVENTS";
    public static String FIREBASEID = "FIREBASEID";


    public static StorageUtil storageUtil;
    public SharedPreferences preferences;
    public SharedPreferences.Editor editor;
    public Gson gson;

    public StorageUtil(Context context) {
        preferences = context.getSharedPreferences(SETTINGS_NAME, MODE_PRIVATE);
        editor = preferences.edit();
        gson = new Gson();
    }

    public static synchronized StorageUtil getInstance(Context context) {
        if (storageUtil == null) {
            storageUtil = new StorageUtil(context.getApplicationContext());
        }
        return storageUtil;
    }

    public void putLocation(LatLng latLng, String key) {
        String data = gson.toJson(latLng);
        editor.putString(key, data);
        editor.commit();
    }

    public void putEvents(List<Events> dataArray) {
        Log.d(TAG, "putEvents: " + dataArray.size());
        String data = gson.toJson(dataArray);
        editor.putString(EVENTS, data);
        editor.commit();
    }

    public void putDestination(String name, String key) {
        editor.putString(key, name);
        editor.commit();
    }

    public void putTripStatus(Boolean param) {
        editor.putBoolean(TRIP_STATUS, param);
        editor.commit();
    }

    public Boolean getTripStatus() {
        return preferences.getBoolean(TRIP_STATUS, false);
    }


    public LatLng getLocation(String key) {
        String data = preferences.getString(key, "");
        LatLng latLng = gson.fromJson(data, LatLng.class);

        return latLng;
    }

    public List<Events> getEvents() {
        Type type = new TypeToken<List<Events>>() {
        }.getType();
        List<Events> out = new ArrayList<>();
        try {
            String data = preferences.getString(EVENTS, "");
            out = gson.fromJson(data, type);
        } catch (Exception e) {

        }

        return out;
    }

    public String getDestination_name() {
        return preferences.getString(DESTINATION_NAME, "");
    }


    public void putSession(String token) {
        editor.putString(SESSION_ID, token);
        editor.commit();
    }

    public String getSession() {
        if (preferences.getString(SESSION_ID, "").length() > 0){
            return preferences.getString(SESSION_ID, "");
        }
        else {
            return null;
        }
    }

    public void putTripid(String token) {
        editor.putString(TRIP_ID, token);
        editor.commit();
    }

    public String getTripId() {
        return preferences.getString(TRIP_ID, "");
    }

    public void putFirebase(String token) {
        editor.putString(FIREBASEID, token);
        editor.commit();
    }

    public String getFirebase() {
        return preferences.getString(FIREBASEID, "");
    }


    public void clearEvents() {
        editor.putString(EVENTS, "");
        editor.commit();
    }
}
