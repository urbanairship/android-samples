/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class RichPushWidgetUtils {

    public static void refreshWidget(Context context) {
        refreshWidget(context, 0);
    }

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
