package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.util.AttributeSet;

public class PushEnablePreference extends UACheckBoxPreference {

    public PushEnablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public UAPreferences.PreferenceType getPreferenceType() {
        return UAPreferences.PreferenceType.PUSH_ENABLE;
    }
}
