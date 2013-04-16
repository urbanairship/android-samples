package com.urbanairship.richpush.sample.preference;


public class UAPreferences {
    public enum PreferenceType {
        PUSH_ENABLE,
        SOUND_ENABLE,
        VIBRATE_ENABLE,
        QUIET_TIME_ENABLE,
        QUIET_TIME_START,
        QUIET_TIME_END,
        LOCATION_ENABLE,
        LOCATION_FOREGROUND_ENABLE,
        LOCATION_BACKGROUND_ENABLE
    }

    public interface Preference {
        public UAPreferences.PreferenceType getPreferenceType();
        public void setValue(Object value);
    }
}
