/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Utility class to help refresh the rich push inbox widget
 *
 */
public class RichPushWidgetUtils {

    /**
     * Sends a request to the rich push message to refresh
     * @param context Application context
     */
    public static void refreshWidget(Context context) {
        refreshWidget(context, 0);
    }

    /**
     * Sends a request to the rich push message to ic_refresh with a delay
     * @param context Application context
     * @param delayInMs Delay to wait in milliseconds before sending the request
     */
    public static void refreshWidget(Context context, long delayInMs) {
        Intent refreshIntent = new Intent(context, RichPushWidgetProvider.class);
        refreshIntent.setAction(RichPushWidgetProvider.REFRESH_ACTION);

        if (delayInMs > 0) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, 0);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delayInMs, pendingIntent);
        } else {
            context.sendBroadcast(refreshIntent);
        }
    }
}
