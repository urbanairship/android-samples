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

        // Test for quiet time start and end times
        verifyTimePickerSetting("QUIET_TIME_START");
        verifyTimePickerSetting("QUIET_TIME_END");

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

        navigateBack();
    }

    /**
     * Test disabling user notifications does not send notifications
     * @throws Exception
     */
    public void testPushDisabled() throws Exception {
        navigateToPreferences();
        preferences.setPreferenceCheckBoxEnabled("USER_NOTIFICATIONS_ENABLED", true);

        navigateBack();

        // Wait for any push registration to take place
        Thread.sleep(REGISTRATION_WAIT_TIME);

        navigateToPreferences();

        String channelId = preferences.getPreferenceSummary("CHANNEL_ID");
        assertNotSame("Failed to generate Channel ID.", channelId, "");

        preferences.setPreferenceCheckBoxEnabled("USER_NOTIFICATIONS_ENABLED", false);

        navigateBack();

        String uniqueAlertId = pushSenderV3.sendPushMessage();
        AutomatorUtils.openNotificationArea();
        assertFalse("Received push notification when push is disabled",
                waitForNotificationToArrive(uniqueAlertId, DISABLED_PUSH_WAIT_TIME));
        navigateBack();
    }
}
