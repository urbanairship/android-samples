package com.urbanairship.richpush.sample;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;

// ActionBarSherlock does not support the new PreferenceFragment, so we fall back to using
// deprecated methods. See https://github.com/JakeWharton/ActionBarSherlock/issues/411
@SuppressWarnings("deprecation")
public class PushPreferencesActivity extends SherlockPreferenceActivity {

    private UASharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the actionBar to have up navigation
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(
                    ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
        }


        AirshipConfigOptions options = UAirship.shared().getAirshipConfigOptions();
        if (options.pushServiceEnabled) {
            this.addPreferencesFromResource(R.xml.push_preferences);
        }

        if (options.locationOptions.locationServiceEnabled) {
            this.addPreferencesFromResource(R.xml.location_preferences);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
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
        sharedPreferences.applyUAPreferences();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        if (sharedPreferences == null) {
            sharedPreferences = new UASharedPreferences() {
                @Override
                public String getPreferenceKey(UAPreferenceKey key) {
                    switch(key) {
                    case LOCATION_BACKGROUND_ENABLE:
                        return "background_location_preference";
                    case LOCATION_ENABLE:
                        return "location_preference";
                    case LOCATION_FOREGROUND_ENABLE:
                        return "foreground_location_preference";
                    case PUSH_ENABLE:
                        return "push_preference";
                    case QUIET_TIME_ENABLE:
                        return "quiet_time_enabled_preference";
                    case QUIET_TIME_END:
                        return "quiet_time_end_preference";
                    case QUIET_TIME_START:
                        return "quiet_time_start_preference";
                    case SOUND_ENABLE:
                        return "sound_preference";
                    case VIBRATE_ENABLE:
                        return "vibrate_preference";
                    }
                    return null;
                }

            };
        }

        return sharedPreferences;
    }
}
