package com.rapidotask.rapidotask.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.rapidotask.rapidotask.Manifest;

import static android.content.ContentValues.TAG;

/**
 * Created by haseeb on 28/9/17.
 */

public class TaskUtil {

    public static Boolean permissionCheck(String permission, Context context) {
        int res = context.checkCallingOrSelfPermission(permission);
        Log.d(TAG, "permissionCheck: "+res+"---"+PackageManager.PERMISSION_GRANTED);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    public static String getDirectionsUrl(Context context){
        String str_origin = "origin=" + StorageUtil.getInstance(context).getLocation(StorageUtil.SOURCE_LATLONG).latitude + "," + StorageUtil.getInstance(context).getLocation(StorageUtil.SOURCE_LATLONG).longitude;
        String str_dest = "destination=" + StorageUtil.getInstance(context).getLocation(StorageUtil.DESTINATION_LATLONG).latitude + "," + StorageUtil.getInstance(context).getLocation(StorageUtil.DESTINATION_LATLONG).longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;

    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getDeviceId(Context context){
        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return android_id;
    }
}
