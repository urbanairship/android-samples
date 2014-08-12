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

import android.app.Application;

import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

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
        PushManager.shared().setNotificationFactory(new RichPushNotificationFactory(this));
    }
}
