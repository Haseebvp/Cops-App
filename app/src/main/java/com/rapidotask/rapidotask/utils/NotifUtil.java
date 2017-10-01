package com.rapidotask.rapidotask.utils;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.rapidotask.rapidotask.MainActivity;
import com.rapidotask.rapidotask.R;

import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by haseeb on 29/9/17.
 */

public class NotifUtil {

    public static void NotifyForVote(String eventKey, Context context) {
        Intent upVote = new Intent(context, MainActivity.class);
        upVote.putExtra("key", eventKey);
        upVote.putExtra("param", "upvote");
        upVote.setAction("upvote");

        Intent downVote = new Intent(context, MainActivity.class);
        downVote.putExtra("key", eventKey);
        downVote.putExtra("param", "downvote");
        downVote.setAction("downvote");

        PendingIntent uIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, upVote, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent dIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, downVote, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action1 = new NotificationCompat.Action.Builder(R.drawable.ic_upvote, "upvote", uIntent).build();
        NotificationCompat.Action action2 = new NotificationCompat.Action.Builder(R.drawable.ic_downvote, "downvote", dIntent).build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round));
        builder.setContentTitle("Rapido update!");
        builder.setStyle(new NotificationCompat.InboxStyle());
        builder.setWhen(System.currentTimeMillis());
        builder.setContentText("Record your votes...");
        builder.setDefaults(Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND);
        builder.addAction(action1);
        builder.addAction(action2);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, builder.build());
    }

    public static void showNotification(Context context, String title, String message, PendingIntent resultPendingIntent) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        inboxStyle.addLine(message);

        Notification notification;
        notification = mBuilder.setSmallIcon(R.mipmap.ic_launcher_round).setTicker(title).setWhen(0)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setStyle(inboxStyle)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentText(message)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
        playNotificationSound(context);
    }

    public static void CancelNotif(int id, Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(id);
    }

    public static void playNotificationSound(Context mContext) {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(mContext, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

}
