/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.preference.UAPreferenceAdapter;
import com.urbanairship.richpush.sample.R;

public class PushPreferencesActivity extends PreferenceActivity {

    private UAPreferenceAdapter preferenceAdapter;

    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the actionBar to have up navigation if HoneyComb or higher.
        // PreferenceFragment or PreferenceActivity is not available in the support
        // library.  ActionBarSherlock provides a PreferenceActivity if you absolutely
        // need an action bar in the preferences on older devices.
        if (Build.VERSION.SDK_INT >= 11) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayOptions(
                        ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
            }
        }




        AirshipConfigOptions options = UAirship.shared().getAirshipConfigOptions();

        // Only add the push preferences if the pushServiceEnabled is true
        if (options.pushServiceEnabled) {
            this.addPreferencesFromResource(R.xml.push_preferences);
        }

        // Only add the location preferences if the locationServiceEnabled is true
        if (options.locationOptions.locationServiceEnabled) {
            this.addPreferencesFromResource(R.xml.location_preferences);
        }

        // Display the advanced settings
        if (options.pushServiceEnabled) {
            this.addPreferencesFromResource(R.xml.advanced_preferences);
        }

        // Creates the UAPreferenceAdapter with the entire preference screen
        preferenceAdapter = new UAPreferenceAdapter(getPreferenceScreen());
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
    protected void onStart() {
        super.onStart();

        // Activity instrumentation for analytic tracking
        UAirship.shared().getAnalytics().activityStarted(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Activity instrumentation for analytic tracking
        UAirship.shared().getAnalytics().activityStopped(this);

        // Apply any changed UA preferences from the preference screen
        preferenceAdapter.applyUrbanAirshipPreferences();
    }
}
