package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

public abstract class UACheckBoxPreference extends CheckBoxPreference implements UAPreferences.Preference {
    public UACheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setValue(Object value) {
        this.setChecked((Boolean) value);
    }

    @Override
    protected boolean shouldPersist() {
        return false;
    }
}