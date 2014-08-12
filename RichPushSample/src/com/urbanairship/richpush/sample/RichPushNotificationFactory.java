/*
Copyright 2009-2014 Urban Airship Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.urbanairship.richpush.sample;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

import com.urbanairship.UAirship;
import com.urbanairship.push.PushMessage;
import com.urbanairship.push.notifications.DefaultNotificationFactory;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.util.NotificationIDGenerator;
import com.urbanairship.util.UAStringUtil;

import java.util.List;

/**
 * A custom push notification builder to create inbox style notifications
 * for rich push messages.  In the case of standard push notifications, it will
 * fall back to the default behavior.
 */
public class RichPushNotificationFactory extends DefaultNotificationFactory {

    private static final int EXTRA_MESSAGES_TO_SHOW = 2;
    private static final int INBOX_NOTIFICATION_ID = 9000000;

    public RichPushNotificationFactory(Context context) {
        super(context);
    }

    @Override
    public Notification createNotification(PushMessage message, int notificationId) {
        if (!UAStringUtil.isEmpty(message.getRichPushMessageId())) {
            return createInboxNotification(message, notificationId);
        } else {
            return super.createNotification(message, notificationId);
        }
    }

    @Override
    public int getNextId(PushMessage pushMessage) {
        if (!UAStringUtil.isEmpty(pushMessage.getRichPushMessageId())) {
            return INBOX_NOTIFICATION_ID;
        } else {
            return NotificationIDGenerator.nextID();
        }
    }

    /**
     * Creates an inbox style notification summarizing the unread messages
     * in the inbox
     *
     * @param message The push message from an Urban Airship push
     * @param notificationId The push notification id
     * @return An inbox style notification
     */
    private Notification createInboxNotification(PushMessage message, int notificationId) {
        Context context = UAirship.getApplicationContext();
        String incomingAlert = message.getAlert();
        List<RichPushMessage> unreadMessages = RichPushManager.shared().getRichPushInbox().getUnreadMessages();
        int totalUnreadCount = unreadMessages.size();

        // If we do not have any unread messages (message already read or they failed to fetch)
        // show a normal notification.
        if (totalUnreadCount == 0) {
            return createNotification(message, notificationId);
        }

        Resources res = context.getResources();
        String title = res.getQuantityString(R.plurals.inbox_notification_title, totalUnreadCount, totalUnreadCount);

        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ua_launcher);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .addLine(Html.fromHtml("<b>" + incomingAlert + "</b>"));


        // Add any extra messages to the notification style
        int extraMessages = Math.min(EXTRA_MESSAGES_TO_SHOW, totalUnreadCount);
        for (int i = 0; i < extraMessages; i++) {
            inboxStyle.addLine(unreadMessages.get(i).getTitle());
        }

        // If we have more messages to show then the EXTRA_MESSAGES_TO_SHOW, add a summary
        if (totalUnreadCount > EXTRA_MESSAGES_TO_SHOW) {
            inboxStyle.setSummaryText(context.getString(R.string.inbox_summary, totalUnreadCount - EXTRA_MESSAGES_TO_SHOW));
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(message.getAlert())
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ua_notification_icon)
                .setNumber(totalUnreadCount)
                .setAutoCancel(true)
                .setStyle(inboxStyle);

        // Notification actions
        builder.extend(createNotificationActionsExtender(message, notificationId));

        return builder.build();
    }

    /**
     * Dismisses the inbox style notification if it exists
     */
    public static void dismissInboxNotification() {
        NotificationManager manager = (NotificationManager) UAirship.shared().
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        manager.cancel(INBOX_NOTIFICATION_ID);
    }
}
