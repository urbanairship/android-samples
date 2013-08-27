package com.urbanairship.richpush.sample.test;

import com.android.uiautomator.core.UiCollection;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;
import com.urbanairship.automatorutils.AutomatorUtils;
import com.urbanairship.automatorutils.PreferencesHelper;
import com.urbanairship.automatorutils.RichPushSender;
import com.urbanairship.automatorutils.RichPushSenderApiV3;

import java.util.HashMap;

/**
 * Automated testing of the Rich Push Sample application
 *
 */
public class RichPushSampleTestCase extends UiAutomatorTestCase {

    private static int REGISTRATION_WAIT_TIME = 60000; // 60 seconds
    private static int UNREGISTER_WAIT_TIME = 10000; // 10 seconds
    private static int WINDOW_UPDATE_WAIT_TIME = 5000;  // 5 seconds
    private static int MESSAGE_RETRIEVAL_WAIT_TIME = 5000;  // 5 seconds
    private static int RICH_PUSH_DIALOG_WAIT_TIME = 20000;  // 20 seconds
    private static int SETTING_PUSH_PREFERENCE_WAIT_TIME = 2000;  // 2 seconds

    // Time to wait for notifications to appear in milliseconds.
    private static int NOTIFICATION_WAIT_TIME = 120000; // 120 seconds - push to tags is slower than to user
    private static final String APP_NAME = "Rich Push Sample";
    private static final String PACKAGE_NAME = "com.urbanairship.richpush.sample";
    private static final String TEST_ALIAS_STRING = AutomatorUtils.generateUniqueAlertId();
    private static final String TEST_TAG_STRING = "TEST_RICH_PUSH_SAMPLE_TAG";

    private RichPushSender pushSender;
    private RichPushSenderApiV3 pushSenderV3;
    private PreferencesHelper preferences;
    private RichPushSampleNavigator appNavigator;

    /**
     * Prepare for testing, which includes getting the masterSecret and appKey.
     * Also generates the uniqueAlertId.
     */
    @Override
    public void setUp() throws Exception {
        // Create a push sender with the master secret and app key from the params
        String masterSecret = getParams().getString("MASTER_SECRET");
        String appKey = getParams().getString("APP_KEY");

        pushSender = new RichPushSender(masterSecret, appKey);
        pushSenderV3 = new RichPushSenderApiV3(masterSecret, appKey);
        preferences = new PreferencesHelper();
        appNavigator = new RichPushSampleNavigator();

        // Open application
        assertTrue("Failed to open Rich Push Sample", AutomatorUtils.openApp(APP_NAME, PACKAGE_NAME));

        // Navigate to home
        appNavigator.navigateToAppHome();
    }

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

    /**
     * Tests the UI for receiving a rich push message, marking it read, unread and deleting it
     * @throws Exception
     */
    public void testInbox() throws Exception {
        appNavigator.navigateToInbox();

        // Enable push
        appNavigator.navigateToPreferences();
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);
        this.getUiDevice().pressBack();

        // Wait for any push registration to take place
        Thread.sleep(REGISTRATION_WAIT_TIME);

        // Verify registration complete by checking for apid
        appNavigator.navigateToPreferences();
        String apid = preferences.getPreferenceSummary("APID");
        assertNotSame("Failed to display the APID. GCM registration may have failed.", apid, "");

        this.getUiDevice().pressBack();

        // Count number of messages
        int originalMessageCount = 0;
        try {
            originalMessageCount = new UiCollection(new UiSelector().className("android.widget.ListView")).getChildCount();
        } catch (Exception ex) {
            // must not exist yet
        }

        // Send broadcast push
        String uniqueAlertId = pushSender.sendPushMessage();

        // Wait for it to arrive
        AutomatorUtils.openNotificationArea();
        waitForNotificationToArrive(uniqueAlertId);
        this.getUiDevice().pressBack();

        // Check that we have one more message
        assertEquals(originalMessageCount + 1, new UiCollection(new UiSelector().className("android.widget.ListView")).getChildCount());

        // grab 1st message, mark as read if not already read
        UiObject message = new UiObject(new UiSelector().description("Inbox message").index(0));
        UiObject messageCheckBox = message.getChild(new UiSelector().className("android.widget.CheckBox"));
        UiObject messageReadIndicator = message.getChild(new UiSelector().description("Message is read"));
        UiObject messageUnreadIndicator =  message.getChild(new UiSelector().description("Message is unread"));

        assertTrue(messageUnreadIndicator.exists());
        assertFalse(messageReadIndicator.exists());

        // Mark as read and check indicator
        messageCheckBox.click();
        UiObject markReadAction = new UiObject(new UiSelector().description("Mark Read"));
        markReadAction.click();
        this.getUiDevice().waitForWindowUpdate(null, WINDOW_UPDATE_WAIT_TIME);

        assertTrue(messageReadIndicator.exists());
        assertFalse(messageUnreadIndicator.exists());

        // Mark as unread and check indicator
        messageCheckBox.click();
        UiObject markUnreadAction = new UiObject(new UiSelector().description("Mark Unread"));
        markUnreadAction.click();
        this.getUiDevice().waitForWindowUpdate(null, WINDOW_UPDATE_WAIT_TIME);

        assertTrue(messageUnreadIndicator.exists());
        assertFalse(messageReadIndicator.exists());

        // Delete message and compare count of messages
        messageCheckBox.click();
        UiObject deleteAction = new UiObject(new UiSelector().description("Delete"));
        deleteAction.click();
        this.getUiDevice().waitForWindowUpdate(null, WINDOW_UPDATE_WAIT_TIME);

        int lastMessageCount = 0;
        try {
            lastMessageCount = new UiCollection(new UiSelector().className("android.widget.ListView")).getChildCount();
        } catch (Exception ex) {
            // must not exist anymore
        }

        // Check that we have one less message
        assertEquals(originalMessageCount, lastMessageCount);
    }

    /**
     * Test the setting of all push and location preferences
     * @throws Exception
     */
    public void testPreferences() throws Exception {
        appNavigator.navigateToPreferences();

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
        verifyCheckBoxSetting("LOCATION_ENABLE");

        // The other location preferences depend on having location enabled
        preferences.setPreferenceCheckBoxEnabled("LOCATION_ENABLE", true);

        // Test foreground and background enable preferences
        verifyCheckBoxSetting("LOCATION_FOREGROUND_ENABLE");
        verifyCheckBoxSetting("LOCATION_BACKGROUND_ENABLE");

        // Disable location settings
        preferences.setPreferenceCheckBoxEnabled("LOCATION_ENABLE", false);

        // Make sure the rest of the location preference views are disabled
        assertFalse(preferences.isPreferenceViewEnabled("LOCATION_FOREGROUND_ENABLE"));
        assertFalse(preferences.isPreferenceViewEnabled("LOCATION_BACKGROUND_ENABLE"));
    }

    /**
     * This final test disables push, deleting the APID and unregisters from GCM.
     * This test begins with 'testZ' to ensure it will be the last test to be executed.
     * @throws Exception
     */
    public void testZCleanup () throws Exception {
        // Disable push
        appNavigator.navigateToPreferences();
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", false);
        this.getUiDevice().pressBack();

        // Wait for any GCM unregistration to take place
        Thread.sleep(UNREGISTER_WAIT_TIME);

        // Verify unregistration complete by checking apid
        appNavigator.navigateToPreferences();
        String apid = preferences.getPreferenceSummary("APID");
        assertEquals("Failed to delete APID. GCM unregistration failed.", apid, "");
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
        appNavigator.navigateToPreferences();

        // Make sure its the toggled value
        assertEquals("Setting " + setting + " did not toggle correctly", originalValue, preferences.getCheckBoxSetting(setting));

        // Set it back to the original value
        preferences.setPreferenceCheckBoxEnabled(setting, originalValue);

        // Reopen settings
        this.getUiDevice().pressBack();
        appNavigator.navigateToPreferences();

        assertEquals("Setting " + setting + " did not toggle correctly", originalValue, preferences.getCheckBoxSetting(setting));
    }

    /**
     * Check the time picker setting was set
     * @param setting The specified time picker
     * @throws Exception
     */
    private void verifyTimePickerSetting(String setting) throws Exception {
        String originalTime = preferences.getPreferenceSummary(setting);

        // Change the current time preference
        preferences.changeTimePreferenceValue(setting);

        // Capture the value
        String changedTime = preferences.getPreferenceSummary(setting);

        //verify it actually changed
        assertNotSame(originalTime, changedTime);

        // Reopen settings
        this.getUiDevice().pressBack();
        appNavigator.navigateToPreferences();

        // Verify its still the captured value
        assertEquals(changedTime, preferences.getPreferenceSummary(setting));
    }

    /**
     * Wait for the notification alert to arrive by polling the notification area
     * @param uniqueAlertId The string used to identify push messages
     * @return <code>true</code> if a notification exists, otherwise <code>false</code>
     * @throws InterruptedException
     */
    private boolean waitForNotificationToArrive(String uniqueAlertId) throws InterruptedException {
        // Verify the alert notification with the uniqueAlertId
        UiObject notificationAlert = new UiObject(new UiSelector().textContains(uniqueAlertId));

        return AutomatorUtils.waitForUiObjectsToExist(NOTIFICATION_WAIT_TIME, notificationAlert);
    }

    /**
     * Verify the rich notification alert is received and can be opened directly to a message.
     * Assert if the notification alert does not exist or the notification failed to display in a webview.
     * @param description The content description string
     * @param uniqueAlertId The string used to identify push messages
     * @param failureMessage A string to include in the failure message
     * @throws InterruptedException
     * @throws UiObjectNotFoundException
     */
    private void verifyRichPushNotification(String description, String uniqueAlertId, String failureMessage) throws InterruptedException, UiObjectNotFoundException {
        AutomatorUtils.openNotificationArea();
        assertTrue(failureMessage + ": No push notifications to open",  waitForNotificationToArrive(uniqueAlertId));

        // Wait for any messsage retrieval to take place
        Thread.sleep(MESSAGE_RETRIEVAL_WAIT_TIME);

        UiObject notificationAlert = new UiObject(new UiSelector().textContains(uniqueAlertId));
        notificationAlert.click();

        // Make sure we have a dialog fragment and web view in main activity
        UiSelector webViewSelector = new UiSelector().className("android.webkit.WebView");
        if (description != null) {
            webViewSelector.description(description);
        }

        UiObject richPushDialog = new UiObject(webViewSelector);
        assertTrue(failureMessage + ": Failed to display notification in a webview",  AutomatorUtils.waitForUiObjectsToExist(RICH_PUSH_DIALOG_WAIT_TIME, richPushDialog));
    }
}
