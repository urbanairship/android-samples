package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.util.AttributeSet;

public class LocationEnablePreference extends UACheckBoxPreference {

    public LocationEnablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public UAPreferences.PreferenceType getPreferenceType() {
        return UAPreferences.PreferenceType.LOCATION_ENABLE;
    }
}
