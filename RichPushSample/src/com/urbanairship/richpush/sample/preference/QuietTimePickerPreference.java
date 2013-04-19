/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Abstract DialogPreference that allows setting quiet time that implements UAPreference
 *
 */
abstract class QuietTimePickerPreference extends DialogPreference implements UAPreference {
    private TimePicker timePicker = null;
    private long currentTime = -1;

    public QuietTimePickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public QuietTimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        view.setContentDescription(getPreferenceType().toString());
        return view;
    }

    @Override
    protected View onCreateDialogView() {
        timePicker = new TimePicker(getContext());

        Calendar calendar = getCalendar();
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

        return timePicker;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
            calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());

            long time = calendar.getTimeInMillis();
            if (callChangeListener(time)) {
                currentTime = time;
                notifyChanged();
            }
        }
    }

    @Override
    public String getSummary() {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        return formatter.format(getCalendar().getTime());
    }

    /**
     * Helper to create a new calendar with the current time of the preference
     * @return Calendar of the current time of the preference
     */
    private Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();

        if (currentTime != -1) {
            calendar.setTimeInMillis(currentTime);
        }

        return calendar;
    }

    @Override
    public void setValue(Object value) {
        currentTime = (Long) value;
        notifyChanged();
    }


    @Override
    protected boolean shouldPersist() {
        return false;
    }

}