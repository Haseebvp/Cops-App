package com.rapidotask.rapidotask;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.JsonObject;
import com.rapidotask.rapidotask.network.ApiCommunication;
import com.rapidotask.rapidotask.network.ApiService;
import com.rapidotask.rapidotask.service.MyFirebaseInstanceIDService;
import com.rapidotask.rapidotask.utils.Constants;
import com.rapidotask.rapidotask.utils.StorageUtil;
import com.rapidotask.rapidotask.utils.TaskUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity implements ApiCommunication {

    private static String TAG = "SPLASH";
    private static final int REQUEST_LOCATION_PERMISSION = 100;
    private static final int REQUEST_GPS = 101;

    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progress = (ProgressBar) findViewById(R.id.progress);
        if (TaskUtil.permissionCheck(Manifest.permission.ACCESS_FINE_LOCATION, this) && TaskUtil.permissionCheck(Manifest.permission.ACCESS_COARSE_LOCATION, this)) {
            Proceed();
        } else {
            requestPermission();
        }
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }

    private void Proceed() {
        if (checkGps()) {
            if (TaskUtil.isNetworkAvailable(this)) {
                registerDevice();
            } else {
                Toast.makeText(getApplicationContext(), "Internet is not available", Toast.LENGTH_LONG).show();
            }
        } else {
            ShowDialoge();
        }

    }

    private void GoToMain1page() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(main);
                finish();
            }
        }, 1500);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Proceed();


                } else {
                    requestPermission();
                }
                return;
        }
    }

    public void turnGPSOn() {
        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_GPS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GPS:
                Proceed();
        }
    }

    private Boolean checkGps() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return statusOfGPS;
    }

    private void ShowDialoge() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.gpsmessage);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                // Show location settings when the user acknowledges the alert dialog
                turnGPSOn();
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }


    //    Api call
    private void registerDevice() {
        progress.setVisibility(View.VISIBLE);
        if (StorageUtil.getInstance(this).getSession() == null) {
            JSONObject data = new JSONObject();
            try {
                data.put("device_id", TaskUtil.getDeviceId(this));
                data.put("device_type_id", TaskUtil.getDeviceId(this));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ApiService.getInstance(this).post(this, Constants.BASE_URL + "gettoken/", data, "GETTOKEN");
        } else {
            JSONObject data = new JSONObject();
            try {
                data.put("device_id", MyFirebaseInstanceIDService.id_device);
                data.put("device_type_id", StorageUtil.getInstance(this).getFirebase());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ApiService.getInstance(this).post(this, Constants.BASE_URL + "updatetoken/", data, "UpdateTOKEN");
        }
    }


//    service callbacks

    @Override
    public void onSuccess(JSONObject object, String flag) {
        progress.setVisibility(View.GONE);
        Log.d(TAG, "onSuccess: " + object);
        if (flag.equals("GETTOKEN")) {
            try {
                String status = object.getString("status");
                if (status.equals("OK")) {
                    String token = object.getString("token");
                    int id = object.getInt("device");
                    MyFirebaseInstanceIDService.id_device = id;
                    StorageUtil.getInstance(this).putSession(token);
                    GoToMain1page();
                } else {
                    Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();

                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
            }
        } else if (flag.equals("UpdateTOKEN")) {
            String status = object.optString("status");
            if (status.equals("OK")) {
                GoToMain1page();
            }
        }

    }

    @Override
    public void onError(VolleyError e, String flag) {
        progress.setVisibility(View.GONE);
        Log.e(TAG, "onError: ", e);

        Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
