/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

/**
 * Abstract CheckBoxPreference that implements UAPreference
 *
 */
abstract class UACheckBoxPreference extends CheckBoxPreference implements UAPreference {
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