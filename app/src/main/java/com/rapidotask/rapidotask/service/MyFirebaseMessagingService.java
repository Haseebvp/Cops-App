package com.rapidotask.rapidotask.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rapidotask.rapidotask.MainActivity;
import com.rapidotask.rapidotask.models.Events;
import com.rapidotask.rapidotask.utils.NotifUtil;
import com.rapidotask.rapidotask.utils.StorageUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by haseeb on 1/10/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage == null)
            return;

        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody() + " , " + remoteMessage.getNotification().getTitle() + " , " + remoteMessage.getData());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(remoteMessage.getNotification().getBody() , remoteMessage.getNotification().getTitle(),json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }

        }


    }


    private void handleDataMessage(String body, String message,JSONObject json) {
        Log.e(TAG, "push json: " + json.toString());

        try {
            JSONObject events = json.getJSONObject("event");

            Events event = new Events();
            event.setDownvote(events.getInt("downvote"));
            event.setUpvote(events.getInt("upvote"));
            event.setPercentage(events.getInt("percentage"));
            event.setEventKey(events.getString("eventKey"));
            event.setLocation(new LatLng(events.getJSONArray("location").getDouble(0), events.getJSONArray("location").getDouble(1)));

            List<Events> data = StorageUtil.getInstance(this).getEvents();
            data.add(event);
            StorageUtil.getInstance(this).putEvents(data);

//            if (!NotifUtil.isAppIsInBackground(getApplicationContext())) {
//                Toast.makeText(this, body, Toast.LENGTH_LONG).show();
//
//                Intent intent = new Intent("Firebase");
//                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//
//                NotifUtil.playNotificationSound(this);
//
//
//            } else {
                // app is in background, show the notification in notification tray
                Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                PendingIntent dIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                resultIntent.putExtra("message", message);
                NotifUtil.showNotification(this, message, body, dIntent);
//            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

}