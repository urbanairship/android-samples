package com.urbanairship.richpush.sample;

import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.sample.preference.UAPreferenceAdapter;

// ActionBarSherlock does not support the new PreferenceFragment, so we fall back to using
// deprecated methods. See https://github.com/JakeWharton/ActionBarSherlock/issues/411
@SuppressWarnings("deprecation")
public class PushPreferencesActivity extends SherlockPreferenceActivity {

    private UAPreferenceAdapter preferenceAdapter = new UAPreferenceAdapter();

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

        preferenceAdapter.setPreferenceGroup(getPreferenceScreen());
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

        // Apply any changed UA preferences from the preference screen
        preferenceAdapter.applyUrbanAirshipPreferences();
    }
}
