/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;

import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import com.urbanairship.Logger;
import com.urbanairship.location.LocationPreferences;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * An adapter to set Urban Airship preferences from Android preference screens without
 * saving values to the shared preferences
 *
 */
public class UAPreferenceAdapter {

    private PushPreferences pushPrefs = PushManager.shared().getPreferences();
    private LocationPreferences locPrefs = UALocationManager.shared().getPreferences();
    private Map<UAPreference.PreferenceType, Object> preferences = new HashMap<UAPreference.PreferenceType, Object>();

    /**
     * UAPreferenceAdapter constructor
     * @param screen PreferenceScreen that contains any UAPreferences.  Only UAPreferences will be affected.
     */
    public UAPreferenceAdapter(PreferenceScreen screen) {
        populatePreferences();

        checkForUAPreferences(screen);
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
            preferences.put(UAPreference.PreferenceType.PUSH_ENABLE, pushPrefs.isPushEnabled());
            preferences.put(UAPreference.PreferenceType.SOUND_ENABLE, pushPrefs.isSoundEnabled());
            preferences.put(UAPreference.PreferenceType.VIBRATE_ENABLE, pushPrefs.isVibrateEnabled());
            preferences.put(UAPreference.PreferenceType.QUIET_TIME_ENABLE, pushPrefs.isQuietTimeEnabled());


            Date[] quietDates = pushPrefs.getQuietTimeInterval();
            if (quietDates != null) {
                preferences.put(UAPreference.PreferenceType.QUIET_TIME_START, quietDates[0].getTime());
                preferences.put(UAPreference.PreferenceType.QUIET_TIME_END, quietDates[1].getTime());
            }


            if (locPrefs != null) {
                preferences.put(UAPreference.PreferenceType.LOCATION_ENABLE, locPrefs.isLocationEnabled());
                preferences.put(UAPreference.PreferenceType.LOCATION_BACKGROUND_ENABLE, locPrefs.isBackgroundLocationEnabled());
                preferences.put(UAPreference.PreferenceType.LOCATION_FOREGROUND_ENABLE, locPrefs.isForegroundLocationEnabled());
            }
        }
    }

    /**
     * Sets the Push Urban Airship preferences from the preference map
     */
    private void applyPushOptions() {
        boolean isPushEnabled = getBoolean(UAPreference.PreferenceType.PUSH_ENABLE, pushPrefs.isPushEnabled());
        boolean isSoundEnabled = getBoolean(UAPreference.PreferenceType.SOUND_ENABLE, pushPrefs.isSoundEnabled());
        boolean isVibrateEnabled = getBoolean(UAPreference.PreferenceType.VIBRATE_ENABLE, pushPrefs.isVibrateEnabled());
        boolean isQuietTimeEnabledInActivity = getBoolean(UAPreference.PreferenceType.QUIET_TIME_ENABLE, pushPrefs.isQuietTimeEnabled());

        if (isPushEnabled) {
            PushManager.enablePush();
        } else {
            PushManager.disablePush();
        }

        pushPrefs.setSoundEnabled(isSoundEnabled);
        pushPrefs.setVibrateEnabled(isVibrateEnabled);
        pushPrefs.setQuietTimeEnabled(isQuietTimeEnabledInActivity);

        if (isQuietTimeEnabledInActivity) {
            long startTimeMillis = getLong(UAPreference.PreferenceType.QUIET_TIME_START, -1);
            long endTimeMillis = getLong(UAPreference.PreferenceType.QUIET_TIME_END, -1);

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
        boolean isLocationEnabled = getBoolean(UAPreference.PreferenceType.LOCATION_ENABLE, locPrefs.isLocationEnabled());
        boolean isBackgroundEnabled = getBoolean(UAPreference.PreferenceType.LOCATION_BACKGROUND_ENABLE, locPrefs.isBackgroundLocationEnabled());
        boolean isForegroundEnabled = getBoolean(UAPreference.PreferenceType.LOCATION_FOREGROUND_ENABLE, locPrefs.isForegroundLocationEnabled());

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

    /**
     * Finds any UAPreference, sets its value, and listens for any
     * value changes
     * @param PreferenceGroup to check for preferences
     */
    private void checkForUAPreferences(PreferenceGroup group) {
        if (group == null) {
            return;
        }

        for (int i = 0; i < group.getPreferenceCount(); i ++) {
            Preference preference = group.getPreference(i);

            if(preference instanceof PreferenceGroup) {
                checkForUAPreferences((PreferenceGroup) preference);
            } else if (preference instanceof UAPreference) {
                preference.setPersistent(false);

                UAPreference uaPreference = (UAPreference) preference;
                UAPreference.PreferenceType preferenceType = uaPreference.getPreferenceType();
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
                        preferences.put(((UAPreference)preference).getPreferenceType(), newValue);
                        return true;
                    }
                });
            }
        }
    }

    /**
     * A helper method to retrieve values inside the preference map
     * 
     * @param preferenceType UAPreference.PreferenceType key into the preferences
     * @param defaultValue Default value if the preference is not stored in the preferences
     * @return Value inside the preferences map, or default value if it does not exist
     */
    private boolean getBoolean(UAPreference.PreferenceType preferenceType, boolean defaultValue) {
        Boolean value = (Boolean) preferences.get(preferenceType);
        return value == null ? defaultValue : value;
    }

    /**
     * A helper method to retrieve values inside the preference map
     * 
     * @param preferenceType UAPreference.PreferenceType key into the preferences
     * @param defaultValue Default value if the preference is not stored in the preferences
     * @return Value inside the preferences map, or default value if it does not exist
     */
    private long getLong(UAPreference.PreferenceType preferenceType, long defaultValue) {
        Long value = (Long) preferences.get(preferenceType);
        return value == null ? defaultValue : value;
    }
}
