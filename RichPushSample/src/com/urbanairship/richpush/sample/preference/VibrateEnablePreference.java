/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.util.AttributeSet;


/**
 * CheckboxPreference to enable/disable push notification vibration
 *
 */
public class VibrateEnablePreference extends UACheckBoxPreference {

    public VibrateEnablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public UAPreference.PreferenceType getPreferenceType() {
        return UAPreference.PreferenceType.VIBRATE_ENABLE;
    }
}
