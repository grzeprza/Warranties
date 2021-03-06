package com.bachelors.grzeprza.warranties.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;
import static com.bachelors.grzeprza.warranties.data.ItemContract.ItemEntry.CONTENT_URI;

/**
 * Created by grzeprza on 02.01.2017.
 */

/**
 * Gathers method to manage notifications in app, as well as stores constant variables for easier usage.
 */
public class NotificationsManager {

    /**
     * Notification title
     */
    static final String NOTIFICATION_TITLE = "Warranty Alert!";

    /**
     * Enables easy access to ID tag, disables programmer fault while programming
     */
    static final String ID = "ID";
    /**
     * Enables easy access to ITEM NAME tag, disables programmer fault while programming
     */
    static final String ITEM_NAME = "ITEM_NAME";
    /**
     * Enables easy access to TIME TILL END, disables programmer fault while programming
     */
    static final String TIME_TILL_END = "TIME_TILL_END";

    /**
     * Enables easy interface to add new Notification. Notification is added after insert and update.
     */
    public static void addNotification(Context context, int id, String itemName, int timeTillEnd_Weeks, Uri imageUri) {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        Log.e("NOTIFY", "Dziś: "+cal.getTime().toString());
        if (timeTillEnd_Weeks > 0)
            cal.add(Calendar.DAY_OF_YEAR, (timeTillEnd_Weeks * 7) - 7);
        Log.e("NOTIFY", "Warranty timeleft: " + String.valueOf(timeTillEnd_Weeks));
        Log.e("NOTIFY", "Alarm set to: "+cal.getTime().toString());
        Intent resultIntent = new Intent(context, Receiver.class);
        resultIntent.putExtra(ID, id);
        resultIntent.putExtra(ITEM_NAME, itemName);
        resultIntent.putExtra(TIME_TILL_END, timeTillEnd_Weeks);
        resultIntent.setData(ContentUris.withAppendedId(CONTENT_URI, id));
        PendingIntent rePendingIntent = PendingIntent.getBroadcast(context, id, resultIntent, 0);

        if(timeTillEnd_Weeks >= 0)
        {
            //Sets alarm notification on week before deadline
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), rePendingIntent); //working just set calendar
            //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0,1000*60*1,rePendingIntent); //gives notification right now
        }
    }

    /**
     * Method deletes alarm manager for current item. It is used when user deletes an item from database.
     */
    public static void deleteNotification(Context context, int id) {
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, Receiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingIntent);
    }

}
