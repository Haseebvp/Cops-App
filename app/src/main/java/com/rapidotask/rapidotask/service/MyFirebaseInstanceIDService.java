package com.rapidotask.rapidotask.service;

import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.rapidotask.rapidotask.network.ApiCommunication;
import com.rapidotask.rapidotask.network.ApiService;
import com.rapidotask.rapidotask.utils.Constants;
import com.rapidotask.rapidotask.utils.StorageUtil;
import com.rapidotask.rapidotask.utils.TaskUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by haseeb on 1/10/17.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService implements ApiCommunication{
    private static final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();
    public static int id_device = 0;

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onTokenRefresh: "+refreshedToken);

        // Saving reg id to shared preferences
        storeRegIdInPref(refreshedToken);

        // sending reg id to your server
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(final String token) {
            JSONObject data = new JSONObject();
            try {
                data.put("device_id", TaskUtil.getDeviceId(this));
                data.put("device_type_id", token);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            StorageUtil.getInstance(this).putFirebase(token);
            ApiService.getInstance(this).post(this, Constants.BASE_URL + "gettoken/", data, "GETTOKEN");
    }

    private void storeRegIdInPref(String token) {
        StorageUtil.getInstance(this).putFirebase(token);
    }

    @Override
    public void onSuccess(JSONObject object, String flag) {
        Log.d(TAG, "onSuccess: "+object);
        try {
            String status = object.getString("status");
            if (status.equals("OK")){
                String token = object.getString("token");
                StorageUtil.getInstance(this).putSession(token);

            }
            else {
                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();

            Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onError(VolleyError e, String flag) {
        Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
    }
}
