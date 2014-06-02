package com.urbanairship.richpush.sample.test;

import com.urbanairship.automatorutils.AutomatorUtils;

/**
 * Test the Rich Push sample applications preferences
 */
public class SettingsTestCase extends BaseTestCase {
    private static final int DISABLED_PUSH_WAIT_TIME = 30000; // 30 seconds
    /**
     * Test the setting of all push and location preferences
     * @throws Exception
     */
    public void testPreferences() throws Exception {
        navigateToPreferences();

        // Push Settings

        // Test for parent push setting
        verifyCheckBoxSetting("PUSH_ENABLE");

        // The rest depend on having push enabled
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);

        // Test sound, vibrate, and quiet time enable preferences
        verifyCheckBoxSetting("SOUND_ENABLE");
        verifyCheckBoxSetting("VIBRATE_ENABLE");
        verifyCheckBoxSetting("QUIET_TIME_ENABLE");

        // Quiet times depend on having quiet time enabled
        preferences.setPreferenceCheckBoxEnabled("QUIET_TIME_ENABLE", true);

        // Test for quiet time start and end times
        verifyTimePickerSetting("QUIET_TIME_START");
        verifyTimePickerSetting("QUIET_TIME_END");

        // Disable quiet time enable
        preferences.setPreferenceCheckBoxEnabled("QUIET_TIME_ENABLE", false);

        // Make sure quiet time setting views are disabled
        assertFalse(preferences.isPreferenceViewEnabled("QUIET_TIME_START"));
        assertFalse(preferences.isPreferenceViewEnabled("QUIET_TIME_END"));

        // Disable push settings
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", false);

        // Make sure the rest of the push preference views are disabled
        assertFalse(preferences.isPreferenceViewEnabled("SOUND_ENABLE"));
        assertFalse(preferences.isPreferenceViewEnabled("VIBRATE_ENABLE"));
        assertFalse(preferences.isPreferenceViewEnabled("QUIET_TIME_ENABLE"));

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

        navigateBack();
    }

    /**
     * Test disabling push does not send notifications
     * @throws Exception
     */
    public void testPushDisabled() throws Exception {
        navigateToPreferences();
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);

        navigateBack();

        // Wait for any push registration to take place
        Thread.sleep(REGISTRATION_WAIT_TIME);

        navigateToPreferences();

        String apid = preferences.getPreferenceSummary("APID");
        assertNotSame("Failed to generate APID.", apid, "");

        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", false);

        navigateBack();
        navigateToPreferences();

        apid = preferences.getPreferenceSummary("APID");
        assertSame("Failed to clear the APID.", apid, "");

        navigateBack();

        String uniqueAlertId = pushSenderV3.sendPushMessage();
        AutomatorUtils.openNotificationArea();
        assertFalse("Received push notification when push is disabled",
                waitForNotificationToArrive(uniqueAlertId, DISABLED_PUSH_WAIT_TIME));
        navigateBack();
    }
}
