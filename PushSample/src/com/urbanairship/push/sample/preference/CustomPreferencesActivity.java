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

package com.urbanairship.push.sample.preference;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;

import com.urbanairship.UAirship;
import com.urbanairship.analytics.InstrumentedActivity;
import com.urbanairship.location.LocationPreferences;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;
import com.urbanairship.push.sample.R;
import com.urbanairship.push.sample.R.id;
import com.urbanairship.push.sample.R.layout;

import java.util.Calendar;
import java.util.Date;

// This class represents the UI and implementation of the activity enabling users
// to set Quiet Time preferences.

public class CustomPreferencesActivity extends InstrumentedActivity {

    CheckBox pushEnabled;
    CheckBox soundEnabled;
    CheckBox vibrateEnabled;
    CheckBox quietTimeEnabled;
    CheckBox locationEnabled;
    CheckBox backgroundLocationEnabled;
    CheckBox foregroundLocationEnabled;

    TextView locationEnabledLabel;
    TextView backgroundLocationEnabledLabel;
    TextView foregroundLocationEnabledLabel;

    TimePicker startTime;
    TimePicker endTime;

    PushPreferences pushPrefs = PushManager.shared().getPreferences();
    LocationPreferences locPrefs = UALocationManager.shared().getPreferences();

    private void pushSettingsActive(boolean active) {
        soundEnabled.setEnabled(active);
        vibrateEnabled.setEnabled(active);
    }

    private void quietTimeSettingsActive(boolean active) {
        startTime.setEnabled(active);
        endTime.setEnabled(active);
    }

    private void backgroundLocationActive(boolean active) {
        backgroundLocationEnabled.setEnabled(active);
    }

    private void foregroundLocationActive(boolean active) {
        foregroundLocationEnabled.setEnabled(active);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window w = getWindow();
        w.requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.push_preferences_dialog);

        pushEnabled = (CheckBox) findViewById(R.id.push_enabled);
        soundEnabled = (CheckBox) findViewById(R.id.sound_enabled);
        vibrateEnabled = (CheckBox) findViewById(R.id.vibrate_enabled);
        quietTimeEnabled = (CheckBox) findViewById(R.id.quiet_time_enabled);
        locationEnabled = (CheckBox) findViewById(R.id.location_enabled);
        backgroundLocationEnabled = (CheckBox) findViewById(R.id.background_location_enabled);
        foregroundLocationEnabled = (CheckBox) findViewById(R.id.foreground_location_enabled);
        locationEnabledLabel = (TextView) findViewById(R.id.location_enabled_label);
        backgroundLocationEnabledLabel = (TextView) findViewById(R.id.background_location_enabled_label);
        foregroundLocationEnabledLabel = (TextView) findViewById(R.id.foreground_location_enabled_label);

        startTime = (TimePicker) findViewById(R.id.start_time);
        endTime = (TimePicker) findViewById(R.id.end_time);

        startTime.setIs24HourView(DateFormat.is24HourFormat(this));
        endTime.setIs24HourView(DateFormat.is24HourFormat(this));

        pushEnabled.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                pushSettingsActive(((CheckBox)v).isChecked());
            }

        });

        quietTimeEnabled.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                quietTimeSettingsActive(((CheckBox)v).isChecked());
            }
        });

        locationEnabled.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundLocationActive(((CheckBox)v).isChecked());
                foregroundLocationActive(((CheckBox)v).isChecked());
            }
        });

    }

    // When the activity starts, we need to fetch and display the user's current
    // Push preferences in the view, if applicable.
    @Override
    public void onStart() {
        super.onStart();

        boolean isPushEnabled = pushPrefs.isPushEnabled();
        pushEnabled.setChecked(isPushEnabled);
        soundEnabled.setChecked(pushPrefs.isSoundEnabled());
        vibrateEnabled.setChecked(pushPrefs.isVibrateEnabled());
        pushSettingsActive(isPushEnabled);

        boolean isQuietTimeEnabled = pushPrefs.isQuietTimeEnabled();
        quietTimeEnabled.setChecked(isQuietTimeEnabled);
        quietTimeSettingsActive(isQuietTimeEnabled);

        if (!UAirship.shared().getAirshipConfigOptions().locationOptions.locationServiceEnabled) {
            locationEnabled.setVisibility(View.GONE);
            backgroundLocationEnabled.setVisibility(View.GONE);
            foregroundLocationEnabled.setVisibility(View.GONE);
            locationEnabledLabel.setVisibility(View.GONE);
            backgroundLocationEnabledLabel.setVisibility(View.GONE);
            foregroundLocationEnabledLabel.setVisibility(View.GONE);

        } else {
            locationEnabled.setChecked(locPrefs.isLocationEnabled());
            backgroundLocationEnabled.setChecked(locPrefs.isBackgroundLocationEnabled());
            foregroundLocationEnabled.setChecked(locPrefs.isForegroundLocationEnabled());
        }

        //this will be null if a quiet time interval hasn't been set
        Date[] interval = pushPrefs.getQuietTimeInterval();
        if(interval != null) {
            startTime.setCurrentHour(interval[0].getHours());
            startTime.setCurrentMinute(interval[0].getMinutes());
            endTime.setCurrentHour(interval[1].getHours());
            endTime.setCurrentMinute(interval[1].getMinutes());
        }
    }

    // When the activity is closed, save the user's Push preferences
    @Override
    public void onStop() {
        super.onStop();

        boolean isPushEnabledInActivity = pushEnabled.isChecked();
        boolean isQuietTimeEnabledInActivity = quietTimeEnabled.isChecked();

        if(isPushEnabledInActivity) {
            PushManager.enablePush();
        }
        else {
            PushManager.disablePush();
        }

        pushPrefs.setSoundEnabled(soundEnabled.isChecked());
        pushPrefs.setVibrateEnabled(vibrateEnabled.isChecked());

        pushPrefs.setQuietTimeEnabled(isQuietTimeEnabledInActivity);

        if(isQuietTimeEnabledInActivity) {

            // Grab the start date.
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, startTime.getCurrentHour());
            cal.set(Calendar.MINUTE, startTime.getCurrentMinute());
            Date startDate = cal.getTime();

            // Prepare the end date.
            cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, endTime.getCurrentHour());
            cal.set(Calendar.MINUTE, endTime.getCurrentMinute());
            Date endDate = cal.getTime();

            pushPrefs.setQuietTimeInterval(startDate, endDate);
        }

        this.handleLocation();

    }

    private void handleLocation() {
        if (!UAirship.shared().getAirshipConfigOptions().locationOptions.locationServiceEnabled) {
            return;
        }
        boolean isLocationEnabledInActivity = locationEnabled.isChecked();
        boolean isBackgroundLocationEnabledInActivity = backgroundLocationEnabled.isChecked();
        boolean isForegroundLocationEnabledInActivity = foregroundLocationEnabled.isChecked();

        // Set the location enable preference first because it will be used
        // in the logic to enable/disable background and foreground locations.
        if (isLocationEnabledInActivity) {
            UALocationManager.enableLocation();
        } else {
            UALocationManager.disableLocation();
        }
        handleBackgroundLocationPreference(isBackgroundLocationEnabledInActivity);
        handleForegroundLocationPreference(isForegroundLocationEnabledInActivity);
    }

    private void handleBackgroundLocationPreference(boolean backgroundLocationEnabled) {
        if (backgroundLocationEnabled) {
            UALocationManager.enableBackgroundLocation();
        } else {
            UALocationManager.disableBackgroundLocation();
        }
    }

    private void handleForegroundLocationPreference(boolean foregroundLocationEnabled) {
        if (foregroundLocationEnabled) {
            UALocationManager.enableForegroundLocation();
        } else {
            UALocationManager.disableForegroundLocation();
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // DO NOT REMOVE, just having it here seems to fix a weird issue with
        // Time picker where the fields would go blank on rotation.
    }

}
