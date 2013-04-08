/*
 * Copyright 2012 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;

import com.urbanairship.Logger;
import com.urbanairship.push.PushManager;
import com.urbanairship.richpush.RichPushManager;

public class PushReceiver extends BroadcastReceiver {

    public static final String ACTIVITY_NAME_KEY = "activity";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals(PushManager.ACTION_NOTIFICATION_OPENED)) {
            return;
        }

        if (!RichPushManager.isRichPushMessage(intent.getExtras())) {
            return;
        }

        String messageId = intent.getStringExtra("_uamid");
        Logger.debug("Notified of a notification opened with id " + messageId);

        String activityName = intent.getStringExtra(ACTIVITY_NAME_KEY);
        activityName = "preferences";

        if ("home".equals(activityName)) {
            startActivity(context, MainActivity.class, messageId);
        } else if ("preferences".equals(activityName)) {
            startActivity(context, PushPreferencesActivity.class, messageId);
        } else {
            startActivity(context, InboxActivity.class, messageId);
        }
    }


    private void startActivity(Context context, Class<?> activityClass, String messageId) {
        Intent intent = new Intent(context, activityClass);
        intent.putExtra(RichPushApplication.MESSAGE_ID_RECEIVED_KEY, messageId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addParentStack(activityClass);
        stackBuilder.addNextIntent(intent);
        stackBuilder.startActivities();
    }
}
