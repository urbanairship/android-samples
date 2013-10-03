package com.urbanairship.richpush.sample.test;

import com.urbanairship.automatorutils.AutomatorUtils;

import java.util.HashMap;

/**
 * Automated testing of the Rich Push Sample application
 *
 */
public class PushTestCase extends BaseTestCase {

    /**
     * Test the sending and receiving of a rich push message
     * @throws Exception
     */
    public void testRichPushNotification() throws Exception {
        // Make sure push is enabled
        appNavigator.navigateToPreferences();
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);
        this.getUiDevice().pressBack();

        // Wait for any push registration to take place
        Thread.sleep(REGISTRATION_WAIT_TIME);

        // Verify registration complete by checking for apid
        appNavigator.navigateToPreferences();
        String apid = preferences.getPreferenceSummary("APID");
        assertNotSame("Failed to display the APID. GCM registration may have failed.", apid, "");

        // Pull down the notification bar and clear notifications
        AutomatorUtils.clearNotifications();

        // APIv1/v2: Verify that we can send a broadcast push and open in a webview
        String uniqueAlertId = pushSender.sendPushMessage();
        verifyRichPushNotification(null, uniqueAlertId, "API v1 broadcast to webview failed");

        // APIv1/v2: Send a broadcast push to main activity
        HashMap<String, String> extras = new HashMap<String, String>();
        extras.put("activity", "home");
        uniqueAlertId = pushSender.sendPushMessage(extras);
        verifyRichPushNotification("Rich push message dialog", uniqueAlertId, "API v1 broadcast failed to open in main activity");

        this.getUiDevice().pressBack();
        appNavigator.navigateToPreferences();

        // APIv1/v2: Send Rich Push Message to User Id
        String richPushId = preferences.getPreferenceSummary("USER_ID");
        uniqueAlertId = pushSender.sendRichPushToUser(richPushId);
        verifyRichPushNotification(null, uniqueAlertId, "API v1 unicast to user ID failed");

        this.getUiDevice().pressBack();

        // APIv3: Broadcast push and open in a webview
        uniqueAlertId = pushSenderV3.sendPushMessage();
        verifyRichPushNotification(null, uniqueAlertId, "API v3 broadcast push failed");

        // APIv3: Push to APID
        uniqueAlertId = pushSenderV3.sendPushToApid(apid);
        verifyRichPushNotification(null, uniqueAlertId, "API v3 unicast push failed");

        this.getUiDevice().pressBack();
        appNavigator.navigateToPreferences();

        // APIv1/v2: Disable push to verify we don't receive push notifications
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", false);
        this.getUiDevice().pressBack();

        // Send a notification that we expect not to receive due to push being disabled
        uniqueAlertId = pushSender.sendPushMessage();

        AutomatorUtils.openNotificationArea();
        assertFalse("Received push notification when push is disabled", waitForNotificationToArrive(uniqueAlertId));
    }

    /**
     * Test the setting of an alias and a tag with APIv3. Test the sending and receiving
     * push messages to a user, alias and tag.
     * @throws Exception
     */
    public void testAliasAndTagsApiV3() throws Exception {
        // The rest depend on having push enabled
        appNavigator.navigateToPreferences();

        // Enable push, disable push, then enable push
        // Possible workaround to PHONE_REGISTRATION_ERROR on emulator
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);
        this.getUiDevice().pressBack();
        Thread.sleep(SETTING_PUSH_PREFERENCE_WAIT_TIME);

        appNavigator.navigateToPreferences();
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", false);
        this.getUiDevice().pressBack();
        Thread.sleep(SETTING_PUSH_PREFERENCE_WAIT_TIME);

        appNavigator.navigateToPreferences();
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);
        this.getUiDevice().pressBack();

        // Wait for any push registration to take place
        Thread.sleep(REGISTRATION_WAIT_TIME);

        // Verify registration complete by checking for apid
        appNavigator.navigateToPreferences();
        String apid = preferences.getPreferenceSummary("APID");
        assertNotSame("Failed to display the APID. GCM registration may have failed.", apid, "");

        // Set alias
        preferences.setAlias(TEST_ALIAS_STRING);
        assertEquals("Failed to set alias string", TEST_ALIAS_STRING, preferences.getPreferenceSummary("SET_ALIAS"));

        // Add tag
        preferences.addTags(TEST_TAG_STRING);
        assertEquals("Failed to display tag string", TEST_TAG_STRING, preferences.getPreferenceSummary("ADD_TAGS"));
        this.getUiDevice().pressBack();

        // Wait any for push registration to take place
        Thread.sleep(REGISTRATION_WAIT_TIME);

        // Pull down the notification bar and clear notifications
        AutomatorUtils.clearNotifications();

        appNavigator.navigateToPreferences();

        // Send push to alias
        String uniqueAlertId = pushSenderV3.sendPushToAlias(TEST_ALIAS_STRING);
        verifyRichPushNotification(null, uniqueAlertId, "API v3 alias push failed");

        // Send push to tag
        uniqueAlertId = pushSenderV3.sendPushToTag(TEST_TAG_STRING);
        verifyRichPushNotification(null, uniqueAlertId, "API v3 tag push failed");
    }
}
