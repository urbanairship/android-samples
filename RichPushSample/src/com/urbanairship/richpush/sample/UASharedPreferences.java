package com.urbanairship.richpush.sample;

import android.content.SharedPreferences;

import com.urbanairship.Logger;
import com.urbanairship.location.LocationPreferences;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;

import java.util.Date;

public abstract class UASharedPreferences extends SharedPreferencesImpl{
    private PushPreferences pushPrefs = PushManager.shared().getPreferences();
    private LocationPreferences locPrefs = UALocationManager.shared().getPreferences();

    public enum UAPreferenceKey { PUSH_ENABLE, SOUND_ENABLE, VIBRATE_ENABLE, QUIET_TIME_ENABLE, QUIET_TIME_START, QUIET_TIME_END, LOCATION_ENABLE, LOCATION_FOREGROUND_ENABLE, LOCATION_BACKGROUND_ENABLE }

    public UASharedPreferences() {
        restorePreferences();
    }

    /**
     * Converts a UAPreferenceKey to a preference id in the preferences resource
     * @param key UAPreference key that needs to be converted
     * @return String preference id in the preferences resource
     */
    public abstract String getPreferenceKey(UAPreferenceKey key);


    /**
     * Helper method to call verify mapping of a UAPreferenceKey to a string
     * @param key UAPreferenceKey that needs to be mapped
     * @return String preference key in the preference xml
     */
    private String getKey(UAPreferenceKey key) {
        String preferenceKey = getPreferenceKey(key);
        if (preferenceKey == null || preferenceKey.length() == 0) {
            Logger.error("Preference key for " + key.toString() + " is empty or null, falling back to " + key.toString());
            return key.toString();
        }

        return preferenceKey;
    }

    /**
     * Applies the shared preferences values to the
     * Urban Airship internal settings.
     * 
     * This should be called on the onStop() method of a preference activity
     */
    public void applyUAPreferences() {
        if (pushPrefs != null) {
            setPushPreferences();
        }

        if (locPrefs != null) {
            setLocationPreferences();
        }
    }

    /**
     * Commits all the Urban Airship preferences to the
     * shared preferences
     */
    private void restorePreferences() {
        SharedPreferences.Editor editor = this.edit();

        if (pushPrefs != null) {
            editor.putBoolean(getKey(UAPreferenceKey.PUSH_ENABLE), pushPrefs.isPushEnabled());
            editor.putBoolean(getKey(UAPreferenceKey.SOUND_ENABLE), pushPrefs.isSoundEnabled());
            editor.putBoolean(getKey(UAPreferenceKey.VIBRATE_ENABLE), pushPrefs.isVibrateEnabled());
            editor.putBoolean(getKey(UAPreferenceKey.QUIET_TIME_ENABLE), pushPrefs.isQuietTimeEnabled());

            Date[] quietDates = pushPrefs.getQuietTimeInterval();
            if (quietDates != null) {
                editor.putLong(getKey(UAPreferenceKey.QUIET_TIME_START), quietDates[0].getTime());
                editor.putLong(getKey(UAPreferenceKey.QUIET_TIME_END), quietDates[1].getTime());
            }
        }

        if (locPrefs != null) {
            editor.putBoolean(getKey(UAPreferenceKey.LOCATION_ENABLE), locPrefs.isLocationEnabled());
            editor.putBoolean(getKey(UAPreferenceKey.LOCATION_BACKGROUND_ENABLE), locPrefs.isBackgroundLocationEnabled());
            editor.putBoolean(getKey(UAPreferenceKey.LOCATION_FOREGROUND_ENABLE), locPrefs.isForegroundLocationEnabled());
        }

        editor.apply();
    }


    /**
     * Sets the Push Urban Airship preferences from the android shared preferences
     */
    private void setPushPreferences() {
        boolean isPushEnabled = getBoolean(getKey(UAPreferenceKey.PUSH_ENABLE), pushPrefs.isPushEnabled());
        boolean isSoundEnabled = getBoolean(getKey(UAPreferenceKey.SOUND_ENABLE), pushPrefs.isSoundEnabled());
        boolean isVibrateEnabled = getBoolean(getKey(UAPreferenceKey.VIBRATE_ENABLE), pushPrefs.isVibrateEnabled());
        boolean isQuietTimeEnabledInActivity = getBoolean(getKey(UAPreferenceKey.QUIET_TIME_ENABLE), pushPrefs.isQuietTimeEnabled());

        if (isPushEnabled) {
            PushManager.enablePush();
        } else {
            PushManager.disablePush();
        }

        pushPrefs.setSoundEnabled(isSoundEnabled);
        pushPrefs.setVibrateEnabled(isVibrateEnabled);
        pushPrefs.setQuietTimeEnabled(isQuietTimeEnabledInActivity);

        if (isQuietTimeEnabledInActivity) {
            long startTimeMillis = getLong(getKey(UAPreferenceKey.QUIET_TIME_START), -1);
            long endTimeMillis = getLong(getKey(UAPreferenceKey.QUIET_TIME_END), -1);

            Date startDate = startTimeMillis == -1 ? null : new Date(startTimeMillis);
            Date endDate = endTimeMillis == -1 ? null : new Date(endTimeMillis);

            if (startDate != null && endDate != null) {
                pushPrefs.setQuietTimeInterval(startDate, endDate);
            }
        }
    }

    /**
     * Sets the Location Urban Airship preferences from the android shared preferences
     */
    private void setLocationPreferences() {
        boolean isLocationEnabled = getBoolean(getKey(UAPreferenceKey.LOCATION_ENABLE), locPrefs.isLocationEnabled());
        boolean isBackgroundEnabled = getBoolean(getKey(UAPreferenceKey.LOCATION_BACKGROUND_ENABLE), locPrefs.isBackgroundLocationEnabled());
        boolean isForegroundEnabled = getBoolean(getKey(UAPreferenceKey.LOCATION_FOREGROUND_ENABLE), locPrefs.isForegroundLocationEnabled());

        if (isLocationEnabled) {
            UALocationManager.enableLocation();
        } else {
            UALocationManager.disableLocation();
        }

        if (isBackgroundEnabled) {
            UALocationManager.enableBackgroundLocation();
        } else {
            UALocationManager.disableBackgroundLocation();
        }

        if (isForegroundEnabled) {
            UALocationManager.enableForegroundLocation();
        } else {
            UALocationManager.disableForegroundLocation();
        }
    }


}
