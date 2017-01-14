package com.bachelors.grzeprza.warranties.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.bachelors.grzeprza.warranties.EditorActivity;
import com.bachelors.grzeprza.warranties.R;

import static com.bachelors.grzeprza.warranties.notification.NotificationsManager.ID;
import static com.bachelors.grzeprza.warranties.notification.NotificationsManager.ITEM_NAME;
import static com.bachelors.grzeprza.warranties.notification.NotificationsManager.NOTIFICATION_TITLE;
import static com.bachelors.grzeprza.warranties.notification.NotificationsManager.TIME_TILL_END;

/**
 * Created by grzeprza on 02.01.2017.
 */

/**
 * Handles alarm manager request
 */
public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        showNotification(context, intent);
    }

    /**
     * Shows notification based on passed intent, and creates reference intent based on item notification
     */
    public void showNotification(Context context, Intent intent) {

        int id = 0;
        String name = "";
        int weeksLeft = 0;

        //Checks passed intent, whether it has required information to display
        if (intent.getExtras() != null) {
            id = intent.getExtras().getInt(ID);
            name = intent.getExtras().getString(ITEM_NAME);
            weeksLeft = intent.getExtras().getInt(TIME_TILL_END);
        }
        //Sets reference Intent
        Intent editIntent = new Intent(context, EditorActivity.class);
        editIntent.setData(intent.getData());

        PendingIntent pi = PendingIntent.getActivity(context, id, editIntent, 0);
        //Building notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(NOTIFICATION_TITLE)
                .setContentText(name +"'s warranty expires in on week!");
        mBuilder.setContentIntent(pi);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, mBuilder.build());
    }
}