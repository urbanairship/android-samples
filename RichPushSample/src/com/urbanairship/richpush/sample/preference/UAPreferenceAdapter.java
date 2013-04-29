/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;

import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.location.LocationPreferences;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;
import com.urbanairship.richpush.RichPushManager;

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
        checkForUAPreferences(screen);
    }

    /**
     * Applies any preferences to UAirship preferences
     * 
     * This should be called on the onStop() method of a preference activity
     */
    public void applyUrbanAirshipPreferences() {
        for (UAPreference.PreferenceType preferenceType : preferences.keySet()) {
            Object value = preferences.get(preferenceType);
            if (value == null) {
                continue;
            }

            try {
                setInternalPreference(preferenceType, value);
            } catch (Exception ex) {
                Logger.warn("Unable to set " + preferenceType + ", invalid value " + value, ex);
            }
        }
    }

    /**
     * Gets the internal UAirship preferences
     * 
     * @return Object value of the internal preference
     */
    private Object getInternalPreference(UAPreference.PreferenceType preferenceType) {
        Date[] quietTimes = null;
        Object value = null;

        switch (preferenceType) {
        case LOCATION_BACKGROUND_ENABLE:
            value = locPrefs.isBackgroundLocationEnabled();
            break;
        case LOCATION_ENABLE:
            value = locPrefs.isLocationEnabled();
            break;
        case LOCATION_FOREGROUND_ENABLE:
            value = locPrefs.isForegroundLocationEnabled();
            break;
        case PUSH_ENABLE:
            value = pushPrefs.isPushEnabled();
            break;
        case QUIET_TIME_ENABLE:
            value = pushPrefs.isQuietTimeEnabled();
            break;
        case QUIET_TIME_END:
            quietTimes = pushPrefs.getQuietTimeInterval();
            value = quietTimes != null ? quietTimes[1].getTime() : null;
            break;
        case QUIET_TIME_START:
            quietTimes = pushPrefs.getQuietTimeInterval();
            value = quietTimes != null ? quietTimes[0].getTime() : null;
            break;
        case SOUND_ENABLE:
            value = pushPrefs.isSoundEnabled();
            break;
        case VIBRATE_ENABLE:
            value = pushPrefs.isVibrateEnabled();
            break;
        case APID:
            value = PushManager.shared().getAPID();
            break;
        case RICH_PUSH_USER_ID:
            value = RichPushManager.shared().getRichPushUser().getId();
            break;
        }

        return value;
    }


    /**
     * Sets the internal UAirship preferences
     * 
     * @param preferenceType UAPreference.PreferenceType type of preference to set
     * @param value Object Value of the preference
     */
    private void setInternalPreference(UAPreference.PreferenceType preferenceType, Object value) {
        Date[] quietTimes = null;

        switch (preferenceType) {
        case LOCATION_BACKGROUND_ENABLE:
            if ((Boolean) value) {
                UALocationManager.enableBackgroundLocation();
            } else {
                UALocationManager.disableBackgroundLocation();
            }
            break;
        case LOCATION_ENABLE:
            if ((Boolean) value) {
                UALocationManager.enableLocation();
            } else {
                UALocationManager.disableLocation();
            }
            break;
        case LOCATION_FOREGROUND_ENABLE:
            if ((Boolean) value) {
                UALocationManager.enableForegroundLocation();
            } else {
                UALocationManager.disableForegroundLocation();
            }
            break;
        case PUSH_ENABLE:
            if ((Boolean) value) {
                PushManager.enablePush();
            } else {
                PushManager.disablePush();
            }
            break;
        case QUIET_TIME_ENABLE:
            pushPrefs.setQuietTimeEnabled((Boolean) value);
            break;
        case SOUND_ENABLE:
            pushPrefs.setSoundEnabled((Boolean) value);
            break;
        case VIBRATE_ENABLE:
            pushPrefs.setVibrateEnabled((Boolean) value);
            break;
        case QUIET_TIME_END:
            quietTimes = pushPrefs.getQuietTimeInterval();
            Date start = quietTimes != null ? quietTimes[0] : new Date();
            pushPrefs.setQuietTimeInterval(start, new Date((Long)value));
            break;
        case QUIET_TIME_START:
            quietTimes = pushPrefs.getQuietTimeInterval();
            Date end = quietTimes != null ? quietTimes[1] : new Date();
            pushPrefs.setQuietTimeInterval(new Date((Long)value), end);
            break;
        case APID:
        case RICH_PUSH_USER_ID:
            // do nothing
            break;
        default:
            break;
        }
    }

    /**
     * Finds any UAPreference, sets its value, and listens for any
     * value changes
     * 
     * @param PreferenceGroup to check for preferences
     */
    private void checkForUAPreferences(PreferenceGroup group) {
        if (group == null) {
            return;
        }

        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference preference = group.getPreference(i);

            if (preference instanceof PreferenceGroup) {
                checkForUAPreferences((PreferenceGroup) preference);
            } else if (preference instanceof UAPreference) {
                trackPreference((UAPreference) preference);
            }
        }
    }


    /**
     * Tries to track a UAPreference if the service it depends on is enabled,
     * it has a valid preference type, and is able to have its value set
     * 
     * @param preference UAPreference to track
     */
    private void trackPreference(UAPreference preference) {
        final UAPreference.PreferenceType preferenceType = preference.getPreferenceType();

        if (preferenceType == null) {
            Logger.warn("Preference returned a null preference type. " + "Ignoring preference " + preference);
            return;
        }

        // Check that the service is enabled for this preference type
        switch (preferenceType) {
        case LOCATION_BACKGROUND_ENABLE:
        case LOCATION_ENABLE:
        case LOCATION_FOREGROUND_ENABLE:
            if (locPrefs == null) {
                Logger.warn("Unable to modify preference " + preferenceType + " because the locationService is not enabled. Ignoring preference");
                return;
            }
            break;
        case PUSH_ENABLE:
        case QUIET_TIME_ENABLE:
        case QUIET_TIME_END:
        case QUIET_TIME_START:
        case SOUND_ENABLE:
        case VIBRATE_ENABLE:
        case APID:
            if (pushPrefs == null) {
                Logger.warn("Unable to modify preference " + preferenceType + " because the pushService is not enabled");
                return;
            }
            break;
        case RICH_PUSH_USER_ID:
            if (pushPrefs == null || !UAirship.shared().getAirshipConfigOptions().richPushEnabled) {
                return;
            }
        }

        // Try to set the initial value if its not null
        Object defaultValue =  getInternalPreference(preferenceType);
        if (defaultValue != null) {
            try {
                preference.setValue(defaultValue);
            } catch (Exception ex) {
                Logger.warn("Exception setting value " + defaultValue + ". Ignoring preference " + preference, ex);
                return;
            }
        }

        // Track any changes to the preference
        ((Preference) preference).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                preferences.put(preferenceType, newValue);
                return true;
            }
        });
    }
}
