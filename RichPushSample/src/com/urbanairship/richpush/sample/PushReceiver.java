/*
 * Copyright 2012 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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

        Intent messageIntent = null;
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
