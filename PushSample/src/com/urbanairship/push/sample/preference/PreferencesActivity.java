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

package com.urbanairship.push.sample.preference;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.preference.UAPreferenceAdapter;
import com.urbanairship.push.sample.R;

public class PreferencesActivity extends PreferenceActivity {

    private UAPreferenceAdapter preferenceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AirshipConfigOptions options = UAirship.shared().getAirshipConfigOptions();

        // Display the push preferences
        this.addPreferencesFromResource(R.xml.push_preferences);

        // Only add the location preferences if the locationServiceEnabled is true
        if (options.locationOptions.locationServiceEnabled) {
            this.addPreferencesFromResource(R.xml.location_preferences);
        }

        // Display the advanced settings
        this.addPreferencesFromResource(R.xml.advanced_preferences);

        // Creates the UAPreferenceAdapter with the entire preference screen
        preferenceAdapter = new UAPreferenceAdapter(getPreferenceScreen());
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
