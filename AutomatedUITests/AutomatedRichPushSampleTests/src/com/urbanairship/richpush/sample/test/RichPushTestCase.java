package com.urbanairship.richpush.sample.test;

import com.urbanairship.automatorutils.AutomatorUtils;

import java.util.HashMap;

/**
 * Automated testing of the Rich Push Sample application
 *
 */
public class RichPushTestCase extends BaseTestCase {

    HashMap<String, String> activityExtras;
    String apid;
    String richPushId;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        activityExtras  = new HashMap<String, String>();
        activityExtras.put("activity", "home");

        navigateToPreferences();

        // Enable push, add tag, set alias
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);
        preferences.setAlias(TEST_ALIAS_STRING);
        preferences.addTags(TEST_TAG_STRING);

        navigateBack();
        Thread.sleep(REGISTRATION_WAIT_TIME);

        navigateToPreferences();

        apid = preferences.getPreferenceSummary("APID");
        richPushId = preferences.getPreferenceSummary("USER_ID");

        // Verify our setting stuck
        assertEquals("Failed to set alias string", TEST_ALIAS_STRING, preferences.getPreferenceSummary("SET_ALIAS"));
        assertEquals("Failed to display tag string", TEST_TAG_STRING, preferences.getPreferenceSummary("ADD_TAGS"));
        assertNotSame("Failed to generate APID.", apid, "");
        assertNotSame("Failed to generate User Id.", richPushId, "");
    }

    /**
     * Test the sending and receiving of a rich push message
     * @throws Exception
     */
    public void testPush() throws Exception {
        navigateToInbox();
        int messageCount = getInboxCount();

        // Pull down the notification bar and clear notifications
        AutomatorUtils.openNotificationArea();
        AutomatorUtils.clearNotifications();

        // APIv1/v2: Send broadcast with extras
        String uniqueAlertId = pushSender.sendPushMessage(activityExtras);
        verifyRichPushNotification("Rich Push Message", uniqueAlertId, "API v1 broadcast to webview failed", ++messageCount);

        // APIv1/v2: Send Rich Push Message to User Id
        uniqueAlertId = pushSender.sendRichPushToUser(richPushId);
        verifyRichPushNotification(null, uniqueAlertId, "API v1 unicast to user ID failed", ++messageCount);

        // APIv3: Broadcast push with extras
        uniqueAlertId = pushSenderV3.sendPushMessage(activityExtras);
        verifyRichPushNotification("Rich Push Message", uniqueAlertId, "API v3 broadcast push failed", ++messageCount);

        // APIv3: Push to APID
        uniqueAlertId = pushSenderV3.sendPushToApid(apid);
        verifyRichPushNotification(null, uniqueAlertId, "API v3 unicast push failed", ++messageCount);

        // APIv3: Push to alias
        uniqueAlertId = pushSenderV3.sendPushToAlias(TEST_ALIAS_STRING);
        verifyRichPushNotification(null, uniqueAlertId, "API v3 alias push failed", ++messageCount);

        // APIv3: Push to tag
        uniqueAlertId = pushSenderV3.sendPushToTag(TEST_TAG_STRING);
        verifyRichPushNotification(null, uniqueAlertId, "API v3 tag push failed", ++messageCount);
    }
}
