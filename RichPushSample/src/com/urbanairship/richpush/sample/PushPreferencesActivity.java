package com.urbanairship.richpush.sample;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.urbanairship.location.LocationPreferences;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;

import java.util.Date;

// ActionBarSherlock does not support the new PreferenceFragment, so we fall back to using
// deprecated methods. See https://github.com/JakeWharton/ActionBarSherlock/issues/411
@SuppressWarnings("deprecation")
public class PushPreferencesActivity extends SherlockPreferenceActivity {

    private PushPreferences pushPrefs = PushManager.shared().getPreferences();
    private LocationPreferences locPrefs = UALocationManager.shared().getPreferences();
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the actionBar to have up navigation
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(
                    ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        }

        if (pushPrefs != null) {
            this.addPreferencesFromResource(R.xml.push_preferences);
        }

        if (locPrefs != null) {
            this.addPreferencesFromResource(R.xml.location_preferences);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        this.sharedPreferences = this.getPreferenceManager().getSharedPreferences();

        // Sets the shared Preferences to what we currently have stored
        // in the urban airship preferences
        restorePreferences();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onStop() {
        super.onStop();

        // We only want to sync the preferences
        // after we are done changing them so services do
        // not repeatedly start and stop.
        setPreferences();
    }

    /**
     * Restores the Urban Airship preferences into the built in android shared preferences
     */
    private void restorePreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if(pushPrefs != null) {
            editor.putBoolean("push_preference", pushPrefs.isPushEnabled());
            editor.putBoolean("sound_preference", pushPrefs.isSoundEnabled());
            editor.putBoolean("vibrate_preference", pushPrefs.isVibrateEnabled());
            editor.putBoolean("quite_time_enabled_preference", pushPrefs.isQuietTimeEnabled());

            Date[] quiteDates = pushPrefs.getQuietTimeInterval();
            if( quiteDates != null) {
                editor.putLong("quite_time_start_preference", quiteDates[0].getTime());
                editor.putLong("quite_time_end_preference", quiteDates[1].getTime());
            }
        }

        if (locPrefs != null) {
            editor.putBoolean("location_preference", locPrefs.isLocationEnabled());
            editor.putBoolean("background_location_preference", locPrefs.isBackgroundLocationEnabled());
            editor.putBoolean("foreground_location_preference", locPrefs.isForegroundLocationEnabled());
        }

        editor.apply();
    }

    /**
     * Tries to set both push preferences and location preferences if available
     */
    private void setPreferences() {
        if (pushPrefs != null) {
            setPushPreferences();
        }

        if (locPrefs != null) {
            setLocationPreferences();
        }
    }

    /**
     * Sets the Push Urban Airship preferences from the android shared preferences
     */
    private void setPushPreferences() {
        boolean isPushEnabled = sharedPreferences.getBoolean("push_preference", pushPrefs.isPushEnabled());
        boolean isSoundEnabled = sharedPreferences.getBoolean("sound_preference", pushPrefs.isSoundEnabled());
        boolean isVibrateEnabled = sharedPreferences.getBoolean("vibrate_preference", pushPrefs.isVibrateEnabled());
        boolean isQuiteTimeEnabledInActivity = sharedPreferences.getBoolean("quite_time_enabled_preference", pushPrefs.isQuietTimeEnabled());

        if(isPushEnabled) {
            PushManager.enablePush();
        } else {
            PushManager.disablePush();
        }

        pushPrefs.setSoundEnabled(isSoundEnabled);
        pushPrefs.setVibrateEnabled(isVibrateEnabled);
        pushPrefs.setQuietTimeEnabled(isQuiteTimeEnabledInActivity);

        if (isQuiteTimeEnabledInActivity) {
            long startTimeMillis = sharedPreferences.getLong("quite_time_start_preference", -1);
            long endTimeMillis = sharedPreferences.getLong("quite_time_end_preference", -1);

            Date startDate = startTimeMillis == -1 ? null : new Date(startTimeMillis);
            Date endDate = endTimeMillis == -1 ? null : new Date(endTimeMillis);

            if (startDate != null && endDate != null) {
                pushPrefs.setQuietTimeInterval(startDate, endDate);
            }
        }
    }

    /**
     * Sets the Location Urban Airship preferences from the android shared preferences
     */
    private void setLocationPreferences() {
        boolean isLocationEnabled = sharedPreferences.getBoolean("location_preference", locPrefs.isLocationEnabled());
        boolean isBackgroundEnabled = sharedPreferences.getBoolean("background_location_preference", locPrefs.isBackgroundLocationEnabled());
        boolean isForegroundEnabled = sharedPreferences.getBoolean("foreground_location_preference", locPrefs.isForegroundLocationEnabled());

        if (isLocationEnabled) {
            UALocationManager.enableLocation();
        } else {
            UALocationManager.disableLocation();
        }

        if (isBackgroundEnabled) {
            UALocationManager.enableBackgroundLocation();
        } else {
            UALocationManager.disableBackgroundLocation();
        }

        if (isForegroundEnabled) {
            UALocationManager.enableForegroundLocation();
        } else {
            UALocationManager.disableForegroundLocation();
        }
    }
}
