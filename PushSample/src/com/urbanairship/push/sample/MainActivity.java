/*
Copyright 2009-2011 Urban Airship Inc. All rights reserved.

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.analytics.InstrumentedActivity;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.sample.preference.CustomPreferencesActivity;
import com.urbanairship.push.sample.preference.PreferencesActivity;

public class MainActivity extends InstrumentedActivity {


    Button locationButton;

    IntentFilter boundServiceFilter;
    IntentFilter channelIdUpdateFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        locationButton = (Button)findViewById(R.id.location_button);
        locationButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), LocationActivity.class));
            }

        });

        // Set up custom preference screen style button
        Button customPreferencesButton = (Button)findViewById(R.id.push_custom_preferences_button);
        customPreferencesButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), CustomPreferencesActivity.class));
            }

        });

        // Set up android built-in preference screen style button
        Button preferencesButton = (Button)findViewById(R.id.push_preferences_button);
        preferencesButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), PreferencesActivity.class));
            }

        });

        boundServiceFilter = new IntentFilter();
        boundServiceFilter.addAction(UALocationManager.getLocationIntentAction(UALocationManager.ACTION_SUFFIX_LOCATION_SERVICE_BOUND));
        boundServiceFilter.addAction(UALocationManager.getLocationIntentAction(UALocationManager.ACTION_SUFFIX_LOCATION_SERVICE_UNBOUND));

        channelIdUpdateFilter = new IntentFilter();
        channelIdUpdateFilter.addAction(UAirship.getPackageName() + IntentReceiver.CHANNEL_ID_UPDATED_ACTION_SUFFIX);
    }

    @Override
    public void onResume() {
        super.onResume();

        // OPTIONAL! The following block of code removes all notifications from the status bar.
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        handleLocationButton();

        registerReceiver(boundServiceReceiver, boundServiceFilter);
        registerReceiver(channelIdUpdateReceiver, channelIdUpdateFilter);
        updateChannelIdField();
    }

    private void handleLocationButton() {
        if (UALocationManager.isServiceBound()) {
            Logger.info("LocationService is bound to MainActivity");
            locationButton.setEnabled(true);
        } else {
            Logger.info("LocationService is not bound to MainActivity");
            locationButton.setEnabled(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(boundServiceReceiver);
            unregisterReceiver(channelIdUpdateReceiver);
        } catch (IllegalArgumentException e) {
            Logger.error(e.getMessage());
        }
    }

    private BroadcastReceiver boundServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UALocationManager.getLocationIntentAction(UALocationManager.ACTION_SUFFIX_LOCATION_SERVICE_BOUND).equals(intent.getAction())) {
                locationButton.setEnabled(true);
            } else {
                locationButton.setEnabled(false);
            }
        }
    };

    private BroadcastReceiver channelIdUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateChannelIdField();
        }
    };

    private void updateChannelIdField() {
        String channelIdString = PushManager.shared().getChannelId();
        if (!PushManager.shared().getPreferences().isPushEnabled() || channelIdString == null) {
            channelIdString = "";
        }

        // fill in channel ID text
        EditText channelIdTextField = (EditText)findViewById(R.id.channelIdText);
        if (!channelIdString.equals(channelIdTextField.getText())) {
            channelIdTextField.setText(channelIdString);
        }
    }
}
