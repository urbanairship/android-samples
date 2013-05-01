package com.urbanairship.richpush.sample.test;

import com.android.uiautomator.core.UiCollection;
import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

/**
 * Automated testing of the Rich Push Sample application
 *
 */
public class RichPushSampleTestCase extends UiAutomatorTestCase {

    // Time to wait for notifications to appear in milliseconds.
    private static int NOTIFICATION_WAIT_TIME = 60000; // 60 seconds - push to tags is slower than to user
    private static final String TEST_ALIAS_STRING = "TEST_ALIAS";
    private static final String TEST_FIRST_TAG_STRING = "TEST_FIRST_TAG";

    private PushSender pushSender;
    private Preferences preferences;
    private RichPushSampleNavigator appNavigator;

    /**
     * Prepare for testing, which includes getting the masterSecret and appKey
     */
    @Override
    public void setUp() throws Exception {
        // Create a push sender with the master secret and app key from the params
        String masterSecret = getParams().getString("MASTER_SECRET");
        String appKey = getParams().getString("APP_KEY");
        pushSender = new PushSender(masterSecret, appKey);

        preferences = new Preferences();
        appNavigator = new RichPushSampleNavigator();

        // Open application
        assertTrue("Failed to open Rich Push Sample", AutomatorUtils.openApp("Rich Push Sample", "com.urbanairship.richpush.sample"));

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

        // Wait a second for any push registration to take place
        Thread.sleep(5000);

        AutomatorUtils.clearNotifications();

        // Verify that we can send a push and open in a webview
        pushSender.sendRichPushMessage();
        verifyPushNotification(null);

        // Send push to main activity
        pushSender.sendRichPushMessage("home");
        verifyPushNotification("Rich push message dialog");

        this.getUiDevice().pressBack();

        // Disable push to verify we don't receive push notifications
        appNavigator.navigateToPreferences();
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", false);
        this.getUiDevice().pressBack();

        // Send a notification
        pushSender.sendRichPushMessage();

        AutomatorUtils.openNotificationArea();
        assertFalse("Received push notification when push is disabled", waitForNotificationToArrive());

        this.getUiDevice().pressBack();
    }

    public void testAliasAndTags() throws Exception {
        // The rest depend on having push enabled
        appNavigator.navigateToPreferences();
        preferences.setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);
        this.getUiDevice().pressBack();

        appNavigator.navigateToPreferences();

        // Send Rich Push Message to Rich Push User Id
        String richPushId = preferences.getPreferenceSummary("RICH_PUSH_USER_ID");
        pushSender.sendRichPushToUser(richPushId);
        verifyPushNotification(null);


        this.getUiDevice().pressBack();
        appNavigator.navigateToPreferences();

        preferences.setAlias(TEST_ALIAS_STRING);
        assertEquals("Failed to set alias string", TEST_ALIAS_STRING, preferences.getPreferenceSummary("SET_ALIAS"));

        // Wait a second for any push registration to take place
        Thread.sleep(1000);

        pushSender.sendRichPushToAlias(TEST_ALIAS_STRING);
        verifyPushNotification(null);

        UiDevice.getInstance().pressBack();
        appNavigator.navigateToPreferences();

        preferences.setTags(TEST_FIRST_TAG_STRING);
        assertEquals("Failed to display first tag string", TEST_FIRST_TAG_STRING, preferences.getPreferenceSummary("SET_TAGS"));

        // Wait a second for any push registration to take place
        Thread.sleep(1000);

        pushSender.sendRichPushToTag(TEST_FIRST_TAG_STRING);
        verifyPushNotification(null);

        this.getUiDevice().pressBack();
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

        // Wait a second for any push registration to take place
        Thread.sleep(5000);

        // Count number of messages
        int originalMessageCount = 0;
        try {
            originalMessageCount = new UiCollection(new UiSelector().className("android.widget.ListView")).getChildCount();
        } catch (Exception ex) {
            // must not exist yet
        }

        // Send push
        pushSender.sendRichPushMessage();

        // Wait for it to arrive
        AutomatorUtils.openNotificationArea();
        waitForNotificationToArrive();
        this.getUiDevice().pressBack();

        // Check that we have one more message
        assertEquals(originalMessageCount + 1, new UiCollection(new UiSelector().className("android.widget.ListView")).getChildCount());

        // grab 1st message, mark as read if not already read
        UiObject message = new UiObject(new UiSelector().description("Inbox message").index(0));
        UiObject messageCheckBox = message.getChild(new UiSelector().className("android.widget.CheckBox"));
        UiObject messageReadIndicator = message.getChild(new UiSelector().description("Message read"));
        UiObject messageUnreadIndicator =  message.getChild(new UiSelector().description("Message unread"));

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
     * Test the setting of all push, location and advanced preferences
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
     * @throws Exception
     */
    private void verifyCheckBoxSetting(String setting) throws Exception {
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
        UiObject notificationTitle = new UiObject(new UiSelector().text("Rich Push Sample"));
        UiObject notificationAlert = new UiObject(new UiSelector().text("Rich Push Alert"));

        return AutomatorUtils.waitForUiObjectsToExist(NOTIFICATION_WAIT_TIME, notificationTitle, notificationAlert);
    }

    private void verifyPushNotification(String description) throws InterruptedException, UiObjectNotFoundException {
        AutomatorUtils.openNotificationArea();
        waitForNotificationToArrive();

        UiObject notificationAlert = new UiObject(new UiSelector().text("Rich Push Alert"));

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
