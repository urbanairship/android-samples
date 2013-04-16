package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.util.AttributeSet;

public class SoundEnablePreference extends UACheckBoxPreference {

    public SoundEnablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public UAPreferences.PreferenceType getPreferenceType() {
        return UAPreferences.PreferenceType.SOUND_ENABLE;
    }
}
