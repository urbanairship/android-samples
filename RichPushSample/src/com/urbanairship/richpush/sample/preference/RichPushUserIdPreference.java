package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class RichPushUserIdPreference extends Preference implements UAPreference {

    public RichPushUserIdPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        view.setContentDescription(getPreferenceType().toString());
        return view;
    }

    @Override
    public PreferenceType getPreferenceType() {
        return PreferenceType.RICH_PUSH_USER_ID;
    }

    @Override
    public void setValue(Object value) {
        setSummary((String) value);
    }

}
