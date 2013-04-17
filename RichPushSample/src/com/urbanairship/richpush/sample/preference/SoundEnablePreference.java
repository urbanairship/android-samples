/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.util.AttributeSet;

/**
 * CheckboxPreference to enable/disable push notification sounds
 *
 */
public class SoundEnablePreference extends UACheckBoxPreference {

    public SoundEnablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public UAPreference.PreferenceType getPreferenceType() {
        return UAPreference.PreferenceType.SOUND_ENABLE;
    }
}
