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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.urbanairship.Logger;
import com.urbanairship.actions.ActionUtils;
import com.urbanairship.actions.DeepLinkAction;
import com.urbanairship.actions.LandingPageAction;
import com.urbanairship.actions.OpenExternalUrlAction;
import com.urbanairship.push.PushManager;
import com.urbanairship.richpush.sample.inbox.InboxActivity;
import com.urbanairship.richpush.sample.widget.RichPushWidgetUtils;
import com.urbanairship.util.UAStringUtil;

/**
 * Broadcast receiver to handle all push notifications
 *
 */
public class PushReceiver extends BroadcastReceiver {

    public static final String EXTRA_MESSAGE_ID_KEY = "_uamid";

    /**
     * Delay to refresh widget to give time to fetch the rich push message
     */
    private static final long WIDGET_REFRESH_DELAY_MS = 5000; //5 Seconds

    // A set of actions that launch activities when a push is opened.  Update
    // with any custom actions that also start activities when a push is opened.
    private static String[] ACTIVITY_ACTIONS = new String[] {
            DeepLinkAction.DEFAULT_REGISTRY_NAME,
            OpenExternalUrlAction.DEFAULT_REGISTRY_NAME,
            LandingPageAction.DEFAULT_REGISTRY_NAME
    };

    @Override
    public void onReceive(Context context, Intent intent) {

        // Refresh the widget after a push comes in
        if (PushManager.ACTION_PUSH_RECEIVED.equals(intent.getAction())) {
            RichPushWidgetUtils.refreshWidget(context, WIDGET_REFRESH_DELAY_MS);
        }

        // Only takes action when a notification is opened
        if (!PushManager.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            return;
        }

        // Only launch the main activity if the payload does not contain any
        // actions that might have already opened an activity
        if (ActionUtils.containsRegisteredActions(intent.getExtras(), ACTIVITY_ACTIONS)) {
            return;
        }

        Intent messageIntent = null;

        String messageId = intent.getStringExtra(EXTRA_MESSAGE_ID_KEY);
        if (UAStringUtil.isEmpty(messageId)) {
            messageIntent =  new Intent(context, MainActivity.class);
        } else {
            Logger.debug("Notified of a notification opened with id " + messageId);
            messageIntent =  new Intent(context, InboxActivity.class);
            messageIntent.putExtra(RichPushApplication.MESSAGE_ID_RECEIVED_KEY, messageId);
        }

        messageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(messageIntent);
    }
}
