/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.util.AttributeSet;

/**
 * CheckboxPreference to enable/disable push notifications
 *
 */
public class PushEnablePreference extends UACheckBoxPreference {

    public PushEnablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public UAPreference.PreferenceType getPreferenceType() {
        return UAPreference.PreferenceType.PUSH_ENABLE;
    }
}
