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

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.urbanairship.analytics.InstrumentedActivity;
import com.urbanairship.google.PlayServicesUtils;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.sample.preference.PreferencesActivity;
import com.urbanairship.util.UAStringUtil;

public class MainActivity extends InstrumentedActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Location
        Button locationButton = (Button)findViewById(R.id.location_button);
        locationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), LocationActivity.class));
            }
        });

        // Preferences
        Button preferencesButton = (Button)findViewById(R.id.push_preferences_button);
        preferencesButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), PreferencesActivity.class));
            }

        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Handle any Google Play services errors
        if (PlayServicesUtils.isGooglePlayStoreAvailable()) {
            PlayServicesUtils.handleAnyPlayServicesError(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // OPTIONAL! The following block of code removes all notifications from the status bar.
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        LocalBroadcastManager locationBroadcastManager = LocalBroadcastManager.getInstance(this);

        // Use local broadcast manager to receive registration events to update the channel
        IntentFilter channelIdUpdateFilter;
        channelIdUpdateFilter = new IntentFilter();
        channelIdUpdateFilter.addAction(IntentReceiver.ACTION_UPDATE_CHANNEL);
        locationBroadcastManager.registerReceiver(channelIdUpdateReceiver, channelIdUpdateFilter);

        // Update the channel field
        updateChannelIdField();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager locationBroadcastManager = LocalBroadcastManager.getInstance(this);
        locationBroadcastManager.unregisterReceiver(channelIdUpdateReceiver);
    }

    private BroadcastReceiver channelIdUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateChannelIdField();
        }
    };

    private void updateChannelIdField() {
        String channelIdString = PushManager.shared().getChannelId();
        channelIdString = UAStringUtil.isEmpty(channelIdString) ? "" : channelIdString;

        // fill in channel ID text
        EditText channelIdTextField = (EditText)findViewById(R.id.channelIdText);
        if (!channelIdString.equals(channelIdTextField.getText())) {
            channelIdTextField.setText(channelIdString);
        }
    }
}
