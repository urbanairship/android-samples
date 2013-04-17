/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.util.AttributeSet;

public class LocationBackgroundEnablePreference extends UACheckBoxPreference {

    public LocationBackgroundEnablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public UAPreferences.PreferenceType getPreferenceType() {
        return UAPreferences.PreferenceType.LOCATION_BACKGROUND_ENABLE;
    }
}
