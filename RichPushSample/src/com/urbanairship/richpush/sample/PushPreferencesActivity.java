/*
 * Copyright 2012 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.urbanairship.UAirship;
import com.urbanairship.location.LocationPreferences;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;
import com.urbanairship.util.UAStringUtil;

import java.util.Calendar;
import java.util.Date;

// This class represents the UI and implementation of the activity enabling users
// to set Quiet Time preferences.

public class PushPreferencesActivity extends SherlockFragmentActivity {

    CheckBox pushEnabled;
    CheckBox soundEnabled;
    CheckBox vibrateEnabled;
    CheckBox quietTimeEnabled;
    CheckBox locationEnabled;
    CheckBox backgroundLocationEnabled;

    TextView locationEnabledLabel;
    TextView backgroundLocationEnabledLabel;

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
        locationEnabledLabel = (TextView) findViewById(R.id.location_enabled_label);
        backgroundLocationEnabledLabel = (TextView) findViewById(R.id.background_location_enabled_label);

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
            }
        });

    }

    // When the activity starts, we need to fetch and display the user's current
    // Push preferences in the view, if applicable.
    @Override
    public void onStart() {
        super.onStart();
        UAirship.shared().getAnalytics().activityStarted(this);

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
            locationEnabledLabel.setVisibility(View.GONE);
            backgroundLocationEnabledLabel.setVisibility(View.GONE);

        } else {
            locationEnabled.setChecked(locPrefs.isLocationEnabled());
            backgroundLocationEnabled.setChecked(locPrefs.isBackgroundLocationEnabled());
        }

        //this will be null if a quiet time interval hasn't been set
        Date[] interval = pushPrefs.getQuietTimeInterval();
        if(interval != null) {
            startTime.setCurrentHour(interval[0].getHours());
            startTime.setCurrentMinute(interval[0].getMinutes());
            endTime.setCurrentHour(interval[1].getHours());
            endTime.setCurrentMinute(interval[1].getMinutes());
        }

        this.displayMessageIfNecessary();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.dismissMessageIfNecessary();
    }

    // When the activity is closed, save the user's Push preferences
    @Override
    public void onStop() {
        super.onStop();
        UAirship.shared().getAnalytics().activityStopped(this);

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

    // helpers

    private void displayMessageIfNecessary() {
        String messageId = this.getIntent().getStringExtra(RichPushApplication.MESSAGE_ID_RECEIVED_KEY);
        if (!UAStringUtil.isEmpty(messageId)) {
            MessageFragment message = MessageFragment.newInstance(messageId);
            message.show(this.getSupportFragmentManager(), R.id.floating_message_pane, "message");
            this.findViewById(R.id.floating_message_pane).setVisibility(View.VISIBLE);
        }
    }

    private void dismissMessageIfNecessary() {
        MessageFragment message = (MessageFragment) this.getSupportFragmentManager()
                .findFragmentByTag("message");
        if (message != null) {
            message.dismiss();
            this.findViewById(R.id.floating_message_pane).setVisibility(View.INVISIBLE);
        }
    }

    private void handleLocation() {
        if (!UAirship.shared().getAirshipConfigOptions().locationOptions.locationServiceEnabled) {
            return;
        }
        boolean isLocationEnabledInActivity = locationEnabled.isChecked();
        boolean isBackgroundLocationEnabledInActivity = backgroundLocationEnabled.isChecked();

        if (isLocationEnabledInActivity) {
            UALocationManager.enableLocation();
            handleBackgroundLocationPreference(isBackgroundLocationEnabledInActivity);
        } else {
            handleBackgroundLocationPreference(isBackgroundLocationEnabledInActivity);
            UALocationManager.disableLocation();
        }

    }

    private void handleBackgroundLocationPreference(boolean backgroundLocationEnabled) {
        if (backgroundLocationEnabled) {
            UALocationManager.enableBackgroundLocation();
        } else {
            UALocationManager.disableBackgroundLocation();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // DO NOT REMOVE, just having it here seems to fix a weird issue with
        // Time picker where the fields would go blank on rotation.
    }

}
