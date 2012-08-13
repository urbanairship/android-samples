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
        if (!intent.getAction().equals(PushManager.ACTION_NOTIFICATION_OPENED)) return;

        if (!RichPushManager.isRichPushMessage(intent.getExtras())) return;
        String messageId = intent.getStringExtra("_uamid");
        Logger.debug("Notified of a notification opened with id " + messageId);

        String activityName = intent.getStringExtra(ACTIVITY_NAME_KEY);
        // default to the Inbox
        Class intentClass = InboxActivity.class;
        if ("home".equals(activityName)) {
            intentClass = MainActivity.class;
        } else if ("preferences".equals(activityName)) {
            intentClass = PushPreferencesActivity.class;
        }
        Intent messageIntent = new Intent(context, intentClass);
        messageIntent.putExtra(RichPushApplication.MESSAGE_ID_RECEIVED_KEY, messageId);
        messageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(messageIntent);
    }

}
