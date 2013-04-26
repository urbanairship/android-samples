/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.util.AttributeSet;

/**
 * CheckboxPreference to enable/disable foreground location tracking
 *
 */
public class LocationForegroundEnablePreference extends UACheckBoxPreference {

    public LocationForegroundEnablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public PreferenceType getPreferenceType() {
        return PreferenceType.LOCATION_FOREGROUND_ENABLE;
    }
}
