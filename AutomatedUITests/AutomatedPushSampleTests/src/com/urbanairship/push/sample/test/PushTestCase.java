package com.urbanairship.push.sample.test;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiSelector;
import com.urbanairship.automatorutils.AutomatorUtils;

/**
 * Automated testing of the Push Sample application
 *
 */
public class PushTestCase extends BaseTestCase {

    /**
     * Test push to Channel ID, tag, alias, named user and broadcast
     * @throws Exception
     */
    public void testPush() throws Exception {
        UiObject preferencesButton = new UiObject(new UiSelector().text("Preferences"));

        // Open preferences
        preferencesButton.click();

        // Enable user notifications
        preferences.setPreferenceCheckBoxEnabled("USER_NOTIFICATIONS_ENABLE", true);

        // Set alias
        preferences.setAlias(TEST_ALIAS_STRING);
        assertEquals("Failed to set alias string", TEST_ALIAS_STRING, preferences.getPreferenceSummary("SET_ALIAS"));

        // Set named user
        preferences.setNamedUser(TEST_NAMED_USER_STRING);
        assertEquals("Failed to set named user string", TEST_NAMED_USER_STRING, preferences.getPreferenceSummary("SET_NAMED_USER"));

        // Add a tag
        preferences.addTags(TEST_TAG_STRING);
        assertEquals("Failed to display tag string", TEST_TAG_STRING, preferences.getPreferenceSummary("ADD_TAGS"));

        // Back and wait for registration to take place
        this.getUiDevice().pressBack();
        Thread.sleep(REGISTRATION_WAIT_TIME);

        // Back into preferences
        preferencesButton.click();

        // Verify registration complete by checking for Channel ID
        String channelId = preferences.getPreferenceSummary("CHANNEL_ID");
        assertNotSame("Failed to display the Channel ID. Registration may have failed.", channelId, "");

        // Back to main activity
        getUiDevice().pressBack();

        // Go home
        getUiDevice().pressHome();

        // Pull down the notification bar and clear notifications
        AutomatorUtils.openNotificationArea();
        AutomatorUtils.clearNotifications();

        // Send push message to Channel ID
        String channelIdAlertId = pushSender.sendPushToChannelId(channelId);
        String aliasAlertId = pushSender.sendPushToAlias(TEST_ALIAS_STRING);
        String namedUserAlertId = pushSender.sendPushToNamedUser(TEST_NAMED_USER_STRING);
        String tagAlertId = pushSender.sendPushToTag(TEST_TAG_STRING);
        String broadcastAlertId = pushSender.sendPushMessage();

        AutomatorUtils.openNotificationArea();

        // Wait for push notifications to arrive
        assertTrue("Failed to receive push from unicast.", waitForNotificationToArrive(channelIdAlertId));
        assertTrue("Failed to receive push from alias.", waitForNotificationToArrive(aliasAlertId));
        assertTrue("Failed to receive push from named user.", waitForNotificationToArrive(namedUserAlertId));
        assertTrue("Failed to receive push from tag.", waitForNotificationToArrive(tagAlertId));
        assertTrue("Failed to receive push from broadcast.", waitForNotificationToArrive(broadcastAlertId));

        // Open a notification
        UiObject notification = new UiObject( new UiSelector().textContains(channelIdAlertId));
        notification.clickAndWaitForNewWindow();

        // Verify opening the notification opens the app
        UiObject pushSampleValidation = new UiObject(new UiSelector().packageName(PACKAGE_NAME));
        assertTrue("Unable to detect Push Sample", AutomatorUtils.waitForUiObjectsToExist(NOTIFICATION_WAIT_TIME, pushSampleValidation));
    }
}