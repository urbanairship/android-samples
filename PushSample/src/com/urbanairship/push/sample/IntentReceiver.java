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

package com.urbanairship.push.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.urbanairship.UAirship;
import com.urbanairship.actions.ActionUtils;
import com.urbanairship.actions.DeepLinkAction;
import com.urbanairship.actions.LandingPageAction;
import com.urbanairship.actions.OpenExternalUrlAction;
import com.urbanairship.push.GCMConstants;
import com.urbanairship.push.PushManager;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class IntentReceiver extends BroadcastReceiver {

    private static final String TAG = "PushSample";

    // A set of actions that launch activities when a push is opened.  Update
    // with any custom actions that also start activities when a push is opened.
    private static String[] ACTIVITY_ACTIONS = new String[]{
            DeepLinkAction.DEFAULT_REGISTRY_NAME,
            OpenExternalUrlAction.DEFAULT_REGISTRY_NAME,
            LandingPageAction.DEFAULT_REGISTRY_NAME
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received intent: " + intent.toString());
        String action = intent.getAction();

        if (action == null) {
            return;
        }

        if (action.equals(PushManager.ACTION_PUSH_RECEIVED)) {

            int id = intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID, 0);

            Log.i(TAG, "Received push notification. Alert: "
                    + intent.getStringExtra(PushManager.EXTRA_ALERT)
                    + " [NotificationID=" + id + "]");

            logPushExtras(intent);

        } else if (action.equals(PushManager.ACTION_NOTIFICATION_OPENED)) {
            Log.i(TAG, "User clicked notification. Message: " + intent.getStringExtra(PushManager.EXTRA_ALERT));
            logPushExtras(intent);

            // Only launch the main activity if the payload does not contain any
            // actions that might have already opened an activity
            if (!ActionUtils.containsRegisteredActions(intent.getExtras(), ACTIVITY_ACTIONS)) {
                Intent launch = new Intent(Intent.ACTION_MAIN);
                launch.setClass(UAirship.shared().getApplicationContext(), MainActivity.class);
                launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                UAirship.shared().getApplicationContext().startActivity(launch);
            }

        } else if (action.equals(PushManager.ACTION_REGISTRATION_SUCCEEDED)) {
            Log.i(TAG, "Registration complete. Channel Id:" + intent.getStringExtra(PushManager.EXTRA_CHANNEL) + ".");
        } else if (action.equals(GCMConstants.ACTION_GCM_DELETED_MESSAGES)) {
            Log.i(TAG, "The GCM service deleted " + intent.getStringExtra(GCMConstants.EXTRA_GCM_TOTAL_DELETED) + " messages.");
        }

        // Notify any app-specific listeners using the local broadcast receiver to avoid
        // leaking any sensitive information.  This sends out all push and location intents
        // to the rest of the application.
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Log the values sent in the payload's "extra" dictionary.
     *
     * @param intent A PushManager.ACTION_NOTIFICATION_OPENED or ACTION_PUSH_RECEIVED intent.
     */
    private void logPushExtras(Intent intent) {
        Set<String> keys = intent.getExtras().keySet();
        for (String key : keys) {

            //ignore standard C2DM extra keys
            List<String> ignoredKeys = (List<String>) Arrays.asList(
                    "collapse_key",//c2dm collapse key
                    "from",//c2dm sender
                    PushManager.EXTRA_NOTIFICATION_ID,//int id of generated notification (ACTION_PUSH_RECEIVED only)
                    PushManager.EXTRA_PUSH_ID,//internal UA push id
                    PushManager.EXTRA_ALERT);//ignore alert
            if (ignoredKeys.contains(key)) {
                continue;
            }
            Log.i(TAG, "Push Notification Extra: [" + key + " : " + intent.getStringExtra(key) + "]");
        }
    }
}
