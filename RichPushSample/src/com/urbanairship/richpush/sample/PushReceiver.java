/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.urbanairship.Logger;
import com.urbanairship.push.PushManager;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.sample.widget.RichPushWidgetUtils;

/**
 * Broadcast receiver to handle all push notifications
 *
 */
public class PushReceiver extends BroadcastReceiver {

    public static final String ACTIVITY_NAME_KEY = "activity";

    public static final String ACTION_WIDGET_MESSAGE_OPEN = "com.urbanairship.richpush.sample.widget.OPEN";

    public static final String EXTRA_MESSAGE_ID_KEY = "_uamid";

    /**
     * Delay to refresh widget to give time to fetch the rich push message
     */
    private static final long WIDGET_REFRESH_DELAY_MS = 5000; //5 Seconds


    @Override
    public void onReceive(Context context, Intent intent) {

        // Refresh the widget after a push comes in
        if (PushManager.ACTION_PUSH_RECEIVED.equals(intent.getAction())) {
            RichPushWidgetUtils.refreshWidget(context, WIDGET_REFRESH_DELAY_MS);
        }

        // Only takes action when a notification is opened
        if (!PushManager.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())
                && !PushReceiver.ACTION_WIDGET_MESSAGE_OPEN.equals(intent.getAction())) {
            return;
        }

        // Ignore any non rich push notifications
        if (!RichPushManager.isRichPushMessage(intent.getExtras())) {
            return;
        }

        String messageId = intent.getStringExtra(EXTRA_MESSAGE_ID_KEY);
        Logger.debug("Notified of a notification opened with id " + messageId);

        Intent messageIntent = null;

        // Set the activity to receive the intent
        if ("home".equals(intent.getStringExtra(ACTIVITY_NAME_KEY))) {
            messageIntent = new Intent(context, MainActivity.class);
        } else {
            // default to the Inbox
            messageIntent =  new Intent(context, InboxActivity.class);
        }

        messageIntent.putExtra(RichPushApplication.MESSAGE_ID_RECEIVED_KEY, messageId);
        messageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(messageIntent);
    }
}
