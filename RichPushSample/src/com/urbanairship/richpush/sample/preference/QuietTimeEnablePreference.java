/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.util.AttributeSet;

/**
 * CheckboxPreference to enable/disable quiet time
 *
 */
public class QuietTimeEnablePreference extends UACheckBoxPreference {

    public QuietTimeEnablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public UAPreference.PreferenceType getPreferenceType() {
        return UAPreference.PreferenceType.QUIET_TIME_ENABLE;
    }
}
