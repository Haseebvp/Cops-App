package com.rapidotask.rapidotask.network;

import com.android.volley.VolleyError;

import org.json.JSONObject;

/**
 * Created by haseeb on 30/9/17.
 */

public interface ApiCommunication {
    void onSuccess(JSONObject object, String flag);
    void onError(VolleyError e, String flag);
}
