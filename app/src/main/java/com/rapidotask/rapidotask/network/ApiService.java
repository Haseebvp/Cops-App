package com.rapidotask.rapidotask.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Api;
import com.rapidotask.rapidotask.utils.StorageUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by haseeb on 30/9/17.
 */

public class ApiService {

    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";
    private static final String SESSION_COOKIE = "token";
    public static ApiService instance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    public ApiService(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized ApiService getInstance(Context context) {
        if (instance == null) {
            instance = new ApiService(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            Cache cache = new DiskBasedCache(mCtx.getCacheDir(), 10 * 1024 * 1024);
            Network network = new BasicNetwork(new HurlStack());
            mRequestQueue = new RequestQueue(cache, network);
            // Don't forget to start the volley request queue
            mRequestQueue.start();
        }
        return mRequestQueue;
    }


    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public void get(final ApiCommunication listener, String url, final String flag) {
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onSuccess(response, flag);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError(error, flag);
            }

        });
        addToRequestQueue(jsonObjReq);
    }


    public void post(final ApiCommunication listener, String url, JSONObject data, final String flag) {
        Log.d(TAG, "post: "+StorageUtil.getInstance(mCtx).getSession());
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, data,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        listener.onSuccess(response, flag);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError(error, flag);
            }
        }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<String, String>();

//                String sessionId = StorageUtil.getInstance(mCtx).getSession();
//                if (sessionId.length() > 0) {
//                    StringBuilder builder = new StringBuilder();
//                    builder.append(SESSION_COOKIE);
//                    builder.append("=");
//                    builder.append(sessionId);
//                    if (headers.containsKey(COOKIE_KEY)) {
//                        builder.append("; ");
//                        builder.append(headers.get(COOKIE_KEY));
//                    }
//                    headers.put(COOKIE_KEY, builder.toString());
////                params.put("HTTP_TOKEN", StorageUtil.getInstance(mCtx).getSession());
//                }

                headers.put("token", StorageUtil.getInstance(mCtx).getSession());
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        addToRequestQueue(jsonObjReq);
    }

}
