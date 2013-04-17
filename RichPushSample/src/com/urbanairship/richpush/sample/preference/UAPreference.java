/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;


/**
 * UAPreference interface
 */
public interface UAPreference {

    public enum PreferenceType {
        /**
         * Push enable preference
         */
        PUSH_ENABLE,

        /**
         * Sound enable preference
         */
        SOUND_ENABLE,

        /**
         * Vibrate enable preference
         */
        VIBRATE_ENABLE,

        /**
         * Quiet time enable preference
         */
        QUIET_TIME_ENABLE,

        /**
         * Quiet time's start preference
         */
        QUIET_TIME_START,

        /**
         * Quiet time's end preference
         */
        QUIET_TIME_END,

        /**
         * Location enable preference
         */
        LOCATION_ENABLE,

        /**
         * Location foreground tracking preference
         */
        LOCATION_FOREGROUND_ENABLE,


        /**
         * Location background tracking preference
         */
        LOCATION_BACKGROUND_ENABLE
    }

    /**
     * @return PreferenceType type of UAPrference
     */
    public PreferenceType getPreferenceType();

    /**
     * Sets the current value of the preference
     * @param value The value of the preference
     */
    public void setValue(Object value);
}
