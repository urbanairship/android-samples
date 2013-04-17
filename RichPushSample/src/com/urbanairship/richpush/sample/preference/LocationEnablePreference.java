/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.util.AttributeSet;

/**
 * CheckboxPreference to enable/disable location tracking
 *
 */
public class LocationEnablePreference extends UACheckBoxPreference {

    public LocationEnablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public PreferenceType getPreferenceType() {
        return PreferenceType.LOCATION_ENABLE;
    }
}
