package com.urbanairship.richpush.sample;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimePickerPreference extends DialogPreference implements OnTimeChangedListener {
    public TimePickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TimePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        TimePicker timePicker = new TimePicker(getContext());
        timePicker.setOnTimeChangedListener(this);

        Calendar calendar = getCalendar();
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

        return timePicker;
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        this.persistLong(calendar.getTimeInMillis());
        this.setSummary(getSummary());
    }


    @Override
    public String getSummary() {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        return formatter.format(getCalendar().getTime());
    }

    private Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();

        long persistedTime = this.getPersistedLong(-1);
        if(persistedTime != -1) {
            calendar.setTimeInMillis(persistedTime);
        }

        return calendar;
    }

}