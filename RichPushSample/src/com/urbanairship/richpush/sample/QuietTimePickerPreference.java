package com.urbanairship.richpush.sample;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class QuietTimePickerPreference extends DialogPreference {
    private TimePicker timePicker = null;

    public QuietTimePickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public QuietTimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
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

            this.persistLong(calendar.getTimeInMillis());
            this.setSummary(getSummary());
        }
    }

    @Override
    public String getSummary() {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        return formatter.format(getCalendar().getTime());
    }

    private Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();

        long persistedTime = this.getPersistedLong(-1);
        if (persistedTime != -1) {
            calendar.setTimeInMillis(persistedTime);
        }

        return calendar;
    }

}