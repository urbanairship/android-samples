package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.util.AttributeSet;

public class LocationForegroundEnablePreference extends UACheckBoxPreference {

    public LocationForegroundEnablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public UAPreferences.PreferenceType getPreferenceType() {
        return UAPreferences.PreferenceType.LOCATION_FOREGROUND_ENABLE;
    }
}
