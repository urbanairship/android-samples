package com.urbanairship.push.sample.test;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;

/**
 * Automated testing of the Push Sample application
 *
 */
public class SettingsTestCase extends BaseTestCase {
    /**
     * Test the setting of all push and location preferences
     * @throws Exception
     */
    public void testPreferences() throws Exception {

        UiObject preferencesButton = new UiObject(new UiSelector().text("Preferences"));
        preferencesButton.click();

        // Push Settings

        // Test user notifications
        verifyCheckBoxSetting("USER_NOTIFICATIONS_ENABLED");

        // The rest depend on having user notifications enabled
        preferences.setPreferenceCheckBoxEnabled("USER_NOTIFICATIONS_ENABLED", true);

        // Test sound, vibrate, and quiet time enable preferences
        verifyCheckBoxSetting("SOUND_ENABLED");
        verifyCheckBoxSetting("VIBRATE_ENABLED");
        verifyCheckBoxSetting("QUIET_TIME_ENABLED");

        // Quiet times depend on having quiet time enabled
        preferences.setPreferenceCheckBoxEnabled("QUIET_TIME_ENABLED", true);

        // Disable quiet time enable
        preferences.setPreferenceCheckBoxEnabled("QUIET_TIME_ENABLED", false);

        // Make sure quiet time setting views are disabled
        assertFalse(preferences.isPreferenceViewEnabled("QUIET_TIME_START"));
        assertFalse(preferences.isPreferenceViewEnabled("QUIET_TIME_END"));

        // Disable user notifications
        preferences.setPreferenceCheckBoxEnabled("USER_NOTIFICATIONS_ENABLED", false);

        // Make sure the rest of the push preference views are disabled
        assertFalse(preferences.isPreferenceViewEnabled("SOUND_ENABLED"));
        assertFalse(preferences.isPreferenceViewEnabled("VIBRATE_ENABLED"));
        assertFalse(preferences.isPreferenceViewEnabled("QUIET_TIME_ENABLED"));

        // Location Settings

        // Test for parent location setting
        verifyCheckBoxSetting("LOCATION_UPDATES_ENABLED");

        // The other location preferences depend on having location enabled
        preferences.setPreferenceCheckBoxEnabled("LOCATION_UPDATES_ENABLED", true);

        // Test background enable preferences
        verifyCheckBoxSetting("LOCATION_BACKGROUND_UPDATES_ALLOWED");

        // Disable location settings
        preferences.setPreferenceCheckBoxEnabled("LOCATION_UPDATES_ENABLED", false);

        // Make sure the rest of the location preference views are disabled
        assertFalse(preferences.isPreferenceViewEnabled("LOCATION_BACKGROUND_UPDATES_ALLOWED"));

        this.getUiDevice().pressBack();
    }


    // Helpers
    /**
     * Verify the checkbox state of the preference
     * @param setting The specified preference
     * @throws UiObjectNotFoundException
     * @throws Exception
     */
    private void verifyCheckBoxSetting(String setting) throws UiObjectNotFoundException, Exception {
        boolean originalValue = preferences.getCheckBoxSetting(setting);

        // Toggle it
        preferences.setPreferenceCheckBoxEnabled(setting, !originalValue);

        // Reopen settings
        this.getUiDevice().pressBack();
        UiObject preferencesButton = new UiObject(new UiSelector().text("Preferences"));
        preferencesButton.click();

        // Make sure its the toggled value
        assertEquals("Setting " + setting + " did not toggle correctly", originalValue, preferences.getCheckBoxSetting(setting));

        // Set it back to the original value
        preferences.setPreferenceCheckBoxEnabled(setting, originalValue);

        // Reopen settings
        this.getUiDevice().pressBack();
        preferencesButton.click();

        assertEquals("Setting " + setting + " did not toggle correctly", originalValue, preferences.getCheckBoxSetting(setting));
    }
}