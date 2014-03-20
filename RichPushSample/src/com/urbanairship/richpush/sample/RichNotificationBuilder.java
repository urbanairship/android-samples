/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

import com.urbanairship.UAirship;
import com.urbanairship.push.BasicPushNotificationBuilder;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;
import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;

import java.util.List;
import java.util.Map;

/**
 * A custom push notification builder to create inbox style notifications
 * for rich push messages.  In the case of standard push notifications, it will
 * fall back to the default behavior.
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class RichNotificationBuilder extends BasicPushNotificationBuilder {

    private static final int EXTRA_MESSAGES_TO_SHOW = 2;
    private static final int INBOX_NOTIFICATION_ID = 9000000;

    @Override
    public Notification buildNotification(String alert, Map<String, String> extras) {
        if (extras != null && RichPushManager.isRichPushMessage(extras)) {
            return createInboxNotification(alert);
        } else {
            return super.buildNotification(alert, extras);
        }
    }

    @Override
    public int getNextId(String alert, Map<String, String> extras) {
        if (extras != null && extras.containsKey(PushReceiver.EXTRA_MESSAGE_ID_KEY)) {
            return INBOX_NOTIFICATION_ID;
        } else {
            return super.getNextId(alert, extras);
        }
    }

    /**
     * Creates an inbox style notification summarizing the unread messages
     * in the inbox
     *
     * @param incomingAlert The alert message from an Urban Airship push
     * @return An inbox style notification
     */
    private Notification createInboxNotification(String incomingAlert) {
        Context context = UAirship.shared().getApplicationContext();

        List<RichPushMessage> unreadMessages = RichPushInbox.shared().getUnreadMessages();
        int totalUnreadCount = unreadMessages.size();

        Resources res = UAirship.shared().getApplicationContext().getResources();
        String title = res.getQuantityString(R.plurals.inbox_notification_title, totalUnreadCount, totalUnreadCount);

        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ua_launcher);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(
                new NotificationCompat.Builder(context)
                    .setDefaults(getNotificationDefaults())
                    .setContentTitle(title)
                    .setContentText(incomingAlert)
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(R.drawable.ua_notification_icon)
                    .setNumber(totalUnreadCount)
                    .setAutoCancel(true)
        );

        // Add the incoming alert as the first line in bold
        style.addLine(Html.fromHtml("<b>"+incomingAlert+"</b>"));

        // Add any extra messages to the notification style
        int extraMessages =  Math.min(EXTRA_MESSAGES_TO_SHOW, totalUnreadCount);
        for (int i = 0; i < extraMessages; i++) {
            style.addLine(unreadMessages.get(i).getTitle());
        }

        // If we have more messages to show then the EXTRA_MESSAGES_TO_SHOW, add a summary
        if (totalUnreadCount > EXTRA_MESSAGES_TO_SHOW) {
            style.setSummaryText(context.getString(R.string.inbox_summary, totalUnreadCount - EXTRA_MESSAGES_TO_SHOW));
        }

        return style.build();
    }

    /**
     * Dismisses the inbox style notification if it exists
     */
    public static void dismissInboxNotification() {
        NotificationManager manager = (NotificationManager) UAirship.shared().
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        manager.cancel(INBOX_NOTIFICATION_ID);
    }

    /**
     * Gets the notification defaults based on
     * the PushPreferences for quiet time, vibration enabled,
     * and sound enabled.
     *
     * @return Notification defaults
     */
    private int getNotificationDefaults() {
        PushPreferences prefs = PushManager.shared().getPreferences();
        int defaults = Notification.DEFAULT_LIGHTS;

        if (!prefs.isInQuietTime()) {
            if (prefs.isVibrateEnabled()) {
                defaults |= Notification.DEFAULT_VIBRATE;
            }

            if (prefs.isSoundEnabled()) {
                defaults |= Notification.DEFAULT_SOUND;
            }
        }

        return defaults;
    }
}
