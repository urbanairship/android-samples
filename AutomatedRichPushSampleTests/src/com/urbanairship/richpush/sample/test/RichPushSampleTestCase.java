package com.urbanairship.richpush.sample.test;

import com.android.uiautomator.core.UiCollection;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

/**
 * Automated testing of the Rich Push Sample application
 *
 */
public class RichPushSampleTestCase extends UiAutomatorTestCase {

    private static int REGISTRATION_WAIT_TIME = 60000; // 60 seconds

    // Time to wait for notifications to appear in milliseconds.
    private static int NOTIFICATION_WAIT_TIME = 90000; // 90 seconds - push to tags is slower than to user
    private static final String APP_NAME = "Rich Push Sample";
    private static final String PACKAGE_NAME = "com.urbanairship.richpush.sample";
    private static final String TEST_ALIAS_STRING = "TEST_RICH_PUSH_SAMPLE_ALIAS";
    private static final String TEST_FIRST_TAG_STRING = "TEST_RICH_PUSH_SAMPLE_FIRST_TAG";
    private static final String RICH_PUSH_BROADCAST_URL = "https://go.urbanairship.com/api/airmail/send/broadcast/";
    private static final String RICH_PUSH_URL = "https://go.urbanairship.com/api/airmail/send/";

    private PushSender pushSender;
    private PreferencesHelper preferences;
    private RichPushSampleNavigator appNavigator;

    private String uniqueAlertId;
    /**
     * Prepare for testing, which includes getting the masterSecret and appKey
     */
    @Override
    public void setUp() throws Exception {
        // Create a push sender with the master secret and app key from the params
        String masterSecret = getParams().getString("MASTER_SECRET");
        String appKey = getParams().getString("APP_KEY");
        uniqueAlertId = AutomatorUtils.generateUniqueAlertId();

        pushSender = new PushSender(masterSecret, appKey, APP_NAME, RICH_PUSH_BROADCAST_URL, RICH_PUSH_URL, uniqueAlertId);
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
        assertNotSame(apid, "");

        // Pull down the notification bar and clear notifications
        AutomatorUtils.clearNotifications();

        // Verify that we can send a push and open in a webview
        pushSender.sendPushMessage();
        verifyPushNotification(null);

        // Send push to main activity
        pushSender.sendPushMessage("home");
        verifyPushNotification("Rich push message dialog");

        this.getUiDevice().pressBack();

        // Disable push to verify we don't receive push notifications
        appNavigator.navigateToPreferences();
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", false);
        this.getUiDevice().pressBack();

        // Send a notification that we expect not to receive due to push being disabled
        pushSender.sendPushMessage();

        AutomatorUtils.openNotificationArea();
        assertFalse("Received push notification when push is disabled", waitForNotificationToArrive());

        this.getUiDevice().pressBack();
    }

    /**
     * Test the setting of an alias and a tag. Test the sending and receiving
     * push messages to a user, alias and tag.
     * @throws Exception
     */
    public void testAliasAndTags() throws Exception {
        // The rest depend on having push enabled
        appNavigator.navigateToPreferences();

        // Enable push, disable push, then enable push
        // Possible workaround to PHONE_REGISTRATION_ERROR on emulator
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);
        this.getUiDevice().pressBack();

        appNavigator.navigateToPreferences();
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", false);
        this.getUiDevice().pressBack();

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

        // Set tag
        preferences.setTags(TEST_FIRST_TAG_STRING);
        assertEquals("Failed to display first tag string", TEST_FIRST_TAG_STRING, preferences.getPreferenceSummary("SET_TAGS"));
        this.getUiDevice().pressBack();

        // Wait any for push registration to take place
        Thread.sleep(REGISTRATION_WAIT_TIME);

        // Pull down the notification bar and clear notifications
        AutomatorUtils.clearNotifications();

        appNavigator.navigateToPreferences();

        // Send Rich Push Message to User Id
        String richPushId = preferences.getPreferenceSummary("USER_ID");
        pushSender.sendRichPushToUser(richPushId);
        verifyPushNotification(null);

        // Send push to alias
        pushSender.sendPushToAlias(TEST_ALIAS_STRING);
        verifyPushNotification(null);

        // Send push to tag
        pushSender.sendPushToTag(TEST_FIRST_TAG_STRING);
        verifyPushNotification(null);
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
        assertNotSame(apid, "");

        this.getUiDevice().pressBack();

        // Count number of messages
        int originalMessageCount = 0;
        try {
            originalMessageCount = new UiCollection(new UiSelector().className("android.widget.ListView")).getChildCount();
        } catch (Exception ex) {
            // must not exist yet
        }

        // Send broadcast push
        pushSender.sendPushMessage();

        // Wait for it to arrive
        AutomatorUtils.openNotificationArea();
        waitForNotificationToArrive();
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

        // mark as read and check indicator
        messageCheckBox.click();
        UiObject markReadAction = new UiObject(new UiSelector().description("Mark Read"));
        markReadAction.click();
        this.getUiDevice().waitForWindowUpdate(null, 5000);

        assertTrue(messageReadIndicator.exists());
        assertFalse(messageUnreadIndicator.exists());

        // mark as unread and check indicator
        messageCheckBox.click();
        UiObject markUnreadAction = new UiObject(new UiSelector().description("Mark Unread"));
        markUnreadAction.click();
        this.getUiDevice().waitForWindowUpdate(null, 5000);

        assertTrue(messageUnreadIndicator.exists());
        assertFalse(messageReadIndicator.exists());

        // delete message and compare count of messages
        messageCheckBox.click();
        UiObject deleteAction = new UiObject(new UiSelector().description("Delete"));
        deleteAction.click();
        this.getUiDevice().waitForWindowUpdate(null, 5000);

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
     * @return <code>true</code> if a notification exists, otherwise <code>false</code>
     * @throws InterruptedException
     */
    private boolean waitForNotificationToArrive() throws InterruptedException {
        UiObject notificationTitle = new UiObject(new UiSelector().text(APP_NAME));
        UiObject notificationAlert = new UiObject(new UiSelector().textContains(uniqueAlertId));

        return AutomatorUtils.waitForUiObjectsToExist(NOTIFICATION_WAIT_TIME, notificationTitle, notificationAlert);
    }

    /**
     * Verify the notification alert is received.
     * Assert if the notification alert does not exist or the notification failed to display in a webview.
     * @param description The content description string
     * @throws InterruptedException
     * @throws UiObjectNotFoundException
     */
    private void verifyPushNotification(String description) throws InterruptedException, UiObjectNotFoundException {
        AutomatorUtils.openNotificationArea();
        waitForNotificationToArrive();

        UiObject notificationAlert = new UiObject(new UiSelector().textContains(uniqueAlertId));

        assertTrue("No push notifications to open",  notificationAlert.exists());

        // Wait a second for any messsage retrieval to take place
        Thread.sleep(1000);

        notificationAlert.click();

        // Make sure we have a dialog fragment and web view in main activity
        UiSelector webViewSelector = new UiSelector().className("android.webkit.WebView");
        if (description != null) {
            webViewSelector.description(description);
        }

        UiObject richPushDialog = new UiObject(webViewSelector);
        assertTrue("Failed to display notification in a webview",  AutomatorUtils.waitForUiObjectsToExist(20000, richPushDialog));
    }
}
