/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.app.Application;
import android.os.Build;

import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessageJavaScriptInterface;

public class RichPushApplication extends Application {

    public static final String MESSAGE_ID_RECEIVED_KEY = "com.urbanairship.richpush.sample.MESSAGE_ID_RECEIVED";
    public static final String HOME_ACTIVITY = "Home";
    public static final String INBOX_ACTIVITY = "Inbox";
    public static final String[] navList = new String[] {
        HOME_ACTIVITY, INBOX_ACTIVITY
    };

    @Override
    public void onCreate() {
        UAirship.takeOff(this);
        PushManager.shared().setIntentReceiver(PushReceiver.class);
        RichPushManager.setJavascriptInterface(RichPushMessageJavaScriptInterface.class);

        // If running on Jelly Bean or higher, then use the inbox style notification builder
        if (Build.VERSION.SDK_INT >= 16) {
            PushManager.shared().setNotificationBuilder(new RichNotificationBuilder());
        }
    }
}
