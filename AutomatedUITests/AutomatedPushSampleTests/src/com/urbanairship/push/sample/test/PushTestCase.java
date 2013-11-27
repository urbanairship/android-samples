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
     * Test push to apid, tag, alias, and broadcast
     * @throws Exception
     */
    public void testPush() throws Exception {
        UiObject preferencesButton = new UiObject(new UiSelector().text("Preferences"));

        // Open preferences
        preferencesButton.click();

        // Enable push
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);

        // Set alias
        preferences.setAlias(TEST_ALIAS_STRING);
        assertEquals("Failed to set alias string", TEST_ALIAS_STRING, preferences.getPreferenceSummary("SET_ALIAS"));

        // Add a tag
        preferences.addTags(TEST_TAG_STRING);
        assertEquals("Failed to display tag string", TEST_TAG_STRING, preferences.getPreferenceSummary("ADD_TAGS"));

        // Back and wait for registration to take place
        this.getUiDevice().pressBack();
        Thread.sleep(REGISTRATION_WAIT_TIME);

        // Back into preferences
        preferencesButton.click();

        // Verify registration complete by checking for apid
        String apid = preferences.getPreferenceSummary("APID");
        assertNotSame("Failed to display the APID. GCM registration may have failed.", apid, "");

        // Back to main activity
        getUiDevice().pressBack();

        // Go home
        getUiDevice().pressHome();

        // Pull down the notification bar and clear notifications
        AutomatorUtils.openNotificationArea();
        AutomatorUtils.clearNotifications();

        // Send push message to apid
        String apidAlertId = pushSender.sendPushToApid(apid);
        String aliasAlertId = pushSender.sendPushToAlias(TEST_ALIAS_STRING);
        String tagAlertId = pushSender.sendPushToTag(TEST_TAG_STRING);
        String broadcastAlertId = pushSender.sendPushMessage();

        AutomatorUtils.openNotificationArea();

        // Wait for push notifications to arrive
        assertTrue("Failed to receive push from unicast.", waitForNotificationToArrive(apidAlertId));
        assertTrue("Failed to receive push from alias.", waitForNotificationToArrive(aliasAlertId));
        assertTrue("Failed to receive push from tag.", waitForNotificationToArrive(tagAlertId));
        assertTrue("Failed to receive push from broadcast.", waitForNotificationToArrive(broadcastAlertId));

        // Open a notification
        UiObject notification = new UiObject( new UiSelector().textContains(apidAlertId));
        notification.clickAndWaitForNewWindow();

        // Verify opening the notification opens the app
        UiObject pushSampleValidation = new UiObject(new UiSelector().packageName(PACKAGE_NAME));
        assertTrue("Unable to detect Push Sample", AutomatorUtils.waitForUiObjectsToExist(NOTIFICATION_WAIT_TIME, pushSampleValidation));
    }
}