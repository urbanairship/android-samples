/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.util.AttributeSet;

/**
 * DialogPreference to set the quiet time end
 *
 */
public class QuietTimeEndPreference extends QuietTimePickerPreference {

    public QuietTimeEndPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public UAPreference.PreferenceType getPreferenceType() {
        return UAPreference.PreferenceType.QUIET_TIME_END;
    }
}
