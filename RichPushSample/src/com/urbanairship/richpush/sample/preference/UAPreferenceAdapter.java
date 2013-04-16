package com.urbanairship.richpush.sample.preference;

import android.preference.Preference;
import android.preference.PreferenceGroup;

import com.urbanairship.Logger;
import com.urbanairship.location.LocationPreferences;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UAPreferenceAdapter {

    private PushPreferences pushPrefs = PushManager.shared().getPreferences();
    private LocationPreferences locPrefs = UALocationManager.shared().getPreferences();
    private Map<UAPreferences.PreferenceType, Object> preferences = new HashMap<UAPreferences.PreferenceType, Object>();

    public UAPreferenceAdapter() {
        populatePreferences();
    }

    /**
     * I NEED A GOOD NAME AND DESRCRIPTION FOR THIS.
     * @param group
     */
    public void setPreferenceGroup(PreferenceGroup group) {
        for (int i = 0; i < group.getPreferenceCount(); i ++) {
            Preference preference = group.getPreference(i);

            if(preference instanceof PreferenceGroup) {
                setPreferenceGroup((PreferenceGroup) preference);
            } else if (preference instanceof UAPreferences.Preference) {
                preference.setPersistent(false);

                UAPreferences.Preference uaPreference = (UAPreferences.Preference) preference;
                UAPreferences.PreferenceType preferenceType = uaPreference.getPreferenceType();
                if (preferenceType == null) {
                    Logger.warn("Ignoring preference " + preference.toString() + ".  Preference returned a null preference type.");
                    continue;
                }

                Object defaultValue = preferences.get(preferenceType);
                if (defaultValue != null) {
                    try {
                        uaPreference.setValue(defaultValue);
                    } catch (Exception ex) {
                        Logger.warn("Ignoring preference " + preference.toString() + ". Exception setting value " + defaultValue + " on preference.", ex);
                        continue;
                    }
                }

                preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        preferences.put(((UAPreferences.Preference)preference).getPreferenceType(), newValue);
                        return true;
                    }
                });
            }
        }
    }


    /**
     * Applies any preferences to UAirship preferences
     * 
     * This should be called on the onStop() method of a preference activity
     */
    public void applyUrbanAirshipPreferences() {
        if (pushPrefs != null) {
            applyPushOptions();
        }

        if (locPrefs != null) {
            applyLocationOptions();
        }
    }

    /**
     * Populates the preference map from the urban airship options
     */
    private void populatePreferences() {
        if (pushPrefs != null) {
            preferences.put(UAPreferences.PreferenceType.PUSH_ENABLE, pushPrefs.isPushEnabled());
            preferences.put(UAPreferences.PreferenceType.SOUND_ENABLE, pushPrefs.isSoundEnabled());
            preferences.put(UAPreferences.PreferenceType.VIBRATE_ENABLE, pushPrefs.isVibrateEnabled());
            preferences.put(UAPreferences.PreferenceType.QUIET_TIME_ENABLE, pushPrefs.isQuietTimeEnabled());


            Date[] quietDates = pushPrefs.getQuietTimeInterval();
            if (quietDates != null) {
                preferences.put(UAPreferences.PreferenceType.QUIET_TIME_START, quietDates[0].getTime());
                preferences.put(UAPreferences.PreferenceType.QUIET_TIME_END, quietDates[1].getTime());
            }


            if (locPrefs != null) {
                preferences.put(UAPreferences.PreferenceType.LOCATION_ENABLE, locPrefs.isLocationEnabled());
                preferences.put(UAPreferences.PreferenceType.LOCATION_BACKGROUND_ENABLE, locPrefs.isBackgroundLocationEnabled());
                preferences.put(UAPreferences.PreferenceType.LOCATION_FOREGROUND_ENABLE, locPrefs.isForegroundLocationEnabled());
            }
        }
    }

    /**
     * Sets the Push Urban Airship preferences from the preference map
     */
    private void applyPushOptions() {
        boolean isPushEnabled = getBoolean(UAPreferences.PreferenceType.PUSH_ENABLE, pushPrefs.isPushEnabled());
        boolean isSoundEnabled = getBoolean(UAPreferences.PreferenceType.SOUND_ENABLE, pushPrefs.isSoundEnabled());
        boolean isVibrateEnabled = getBoolean(UAPreferences.PreferenceType.VIBRATE_ENABLE, pushPrefs.isVibrateEnabled());
        boolean isQuietTimeEnabledInActivity = getBoolean(UAPreferences.PreferenceType.QUIET_TIME_ENABLE, pushPrefs.isQuietTimeEnabled());

        if (isPushEnabled) {
            PushManager.enablePush();
        } else {
            PushManager.disablePush();
        }

        pushPrefs.setSoundEnabled(isSoundEnabled);
        pushPrefs.setVibrateEnabled(isVibrateEnabled);
        pushPrefs.setQuietTimeEnabled(isQuietTimeEnabledInActivity);

        if (isQuietTimeEnabledInActivity) {
            long startTimeMillis = getLong(UAPreferences.PreferenceType.QUIET_TIME_START, -1);
            long endTimeMillis = getLong(UAPreferences.PreferenceType.QUIET_TIME_END, -1);

            Date startDate = startTimeMillis == -1 ? null : new Date(startTimeMillis);
            Date endDate = endTimeMillis == -1 ? null : new Date(endTimeMillis);

            if (startDate != null && endDate != null) {
                pushPrefs.setQuietTimeInterval(startDate, endDate);
            }
        }
    }

    /**
     * Sets the Location Urban Airship preferences from the preference map
     */
    private void applyLocationOptions() {
        boolean isLocationEnabled = getBoolean(UAPreferences.PreferenceType.LOCATION_ENABLE, locPrefs.isLocationEnabled());
        boolean isBackgroundEnabled = getBoolean(UAPreferences.PreferenceType.LOCATION_BACKGROUND_ENABLE, locPrefs.isBackgroundLocationEnabled());
        boolean isForegroundEnabled = getBoolean(UAPreferences.PreferenceType.LOCATION_FOREGROUND_ENABLE, locPrefs.isForegroundLocationEnabled());

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

    private boolean getBoolean(UAPreferences.PreferenceType preferenceType, boolean defaultValue) {
        Boolean value = (Boolean) preferences.get(preferenceType);
        return value == null ? defaultValue : value;
    }

    private long getLong(UAPreferences.PreferenceType preferenceType, long defaultValue) {
        Long value = (Long) preferences.get(preferenceType);
        return value == null ? defaultValue : value;
    }
}
