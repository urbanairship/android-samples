package com.urbanairship.richpush.sample.test;

import com.android.uiautomator.core.UiCollection;
import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;
import com.urbanairship.automatorutils.AutomatorUtils;
import com.urbanairship.automatorutils.PreferencesHelper;
import com.urbanairship.automatorutils.RichPushSender;
import com.urbanairship.automatorutils.RichPushSenderApiV3;

/**
 * Base test case for automated testing of the Rich Push Sample application
 *
 */
public class BaseTestCase extends UiAutomatorTestCase {

    static int REGISTRATION_WAIT_TIME = 30000; // 30 seconds
    static int WINDOW_UPDATE_WAIT_TIME = 5000;  // 5 seconds
    static int MESSAGE_RETRIEVAL_WAIT_TIME = 5000;  // 5 seconds
    static int RICH_PUSH_DIALOG_WAIT_TIME = 20000;  // 20 seconds
    static int UI_OBJECTS_WAIT_TIME = 1000;  // 1 second

    // Time to wait for notifications to appear in milliseconds.
    static int NOTIFICATION_WAIT_TIME = 120000; // 120 seconds - push to tags is slower than to user
    static final String APP_NAME = "Rich Push Sample";
    static final String PACKAGE_NAME = "com.urbanairship.richpush.sample";
    static final String TEST_ALIAS_STRING = AutomatorUtils.generateUniqueAlertId();
    static final String TEST_NAMED_USER_STRING = AutomatorUtils.generateUniqueAlertId();
    static final String TEST_TAG_STRING = "TEST_RICH_PUSH_SAMPLE_TAG";

    //RichPushSender pushSender;
    RichPushSenderApiV3 pushSenderV3;
    PreferencesHelper preferences;

    /**
     * Prepare for testing, which includes getting the masterSecret and appKey.
     * Also generates the uniqueAlertId.
     */
    @Override
    public void setUp() throws Exception {
        // Create a push sender with the master secret and app key from the params
        String masterSecret = getParams().getString("MASTER_SECRET");
        String appKey = getParams().getString("APP_KEY");

        //pushSender = new RichPushSender(masterSecret, appKey);
        pushSenderV3 = new RichPushSenderApiV3(masterSecret, appKey);
        preferences = new PreferencesHelper();

        // Open application
        assertTrue("Failed to open Rich Push Sample", AutomatorUtils.openApp(APP_NAME, PACKAGE_NAME));

        // Navigate to home
        navigateToAppHome();
    }

    /**
     * Cleanup - disable user notifications.
     */
    @Override
    public void tearDown() throws Exception {
        // Disable push
        navigateToPreferences();
        preferences.setPreferenceCheckBoxEnabled("USER_NOTIFICATIONS_ENABLED", false);

        navigateBack();
    }


    /**
     * Verify the rich notification alert is received and can be opened directly to a message.
     * Assert if the notification alert does not exist or the notification failed to display in a webview.
     * @param description The content description string
     * @param uniqueAlertId The string used to identify push messages
     * @param failureMessage A string to include in the failure message
     * @param expectedInboxCount expected number of messages in the inbox
     * @throws Exception
     */
    void verifyRichPushNotification(String description, String uniqueAlertId, String failureMessage, int expectedInboxCount) throws Exception {
        AutomatorUtils.openNotificationArea();
        assertTrue(failureMessage + ": No push notifications to open",  waitForNotificationToArrive(uniqueAlertId));

        // Wait for any messsage retrieval to take place
        Thread.sleep(MESSAGE_RETRIEVAL_WAIT_TIME);

        UiObject notificationAlert = new UiObject(new UiSelector().textContains(uniqueAlertId));
        notificationAlert.click();
        getUiDevice().waitForWindowUpdate(PACKAGE_NAME, 5000);

        UiSelector webViewSelector = new UiSelector().className("android.view.View");
        if (description != null) {
            webViewSelector.descriptionContains(uniqueAlertId);
        }

        UiObject richPushDialog = new UiObject(webViewSelector);
        assertTrue(failureMessage + ": Failed to display notification in a webview",  AutomatorUtils.waitForUiObjectsToExist(RICH_PUSH_DIALOG_WAIT_TIME, richPushDialog));

        navigateToInbox();
        refreshInbox();

        assertSame("Unexpected inbox count.", expectedInboxCount, getInboxCount());
    }

    /**
     * Fetches inbox messages.
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    void refreshInbox() throws UiObjectNotFoundException, InterruptedException {
        UiSelector inboxSelector = new UiSelector().className("android.view.View").instance(2);
        UiObject inbox = new UiObject(inboxSelector);
        inbox.swipeDown(50);
        getUiDevice().waitForWindowUpdate(PACKAGE_NAME, 5000);
    }

    /**
     * Get the count of messages in the inbox.
     * @return The count of inbox messages.
     */
    int getInboxCount() {
        try {
            UiSelector message = new UiSelector().className("android.widget.RelativeLayout");
            return new UiCollection(new UiSelector().className("android.widget.ListView")).getChildCount(message);
        } catch (Exception ex) {
            // must not exist yet
            return 0;
        }
    }

    /**
     * Verify the checkbox state of the preference
     * @param setting The specified preference
     * @throws UiObjectNotFoundException
     * @throws Exception
     */
    void verifyCheckBoxSetting(String setting) throws UiObjectNotFoundException, Exception {
        boolean originalValue = preferences.getCheckBoxSetting(setting);

        // Toggle it
        preferences.setPreferenceCheckBoxEnabled(setting, !originalValue);

        // Reopen settings
        this.getUiDevice().pressBack();
        navigateToPreferences();

        // Make sure its the toggled value
        assertEquals("Setting " + setting + " did not toggle correctly", originalValue, preferences.getCheckBoxSetting(setting));

        // Set it back to the original value
        preferences.setPreferenceCheckBoxEnabled(setting, originalValue);

        // Reopen settings
        navigateBack();
        navigateToPreferences();

        assertEquals("Setting " + setting + " did not toggle correctly", originalValue, preferences.getCheckBoxSetting(setting));
    }

    /**
     * Check the time picker setting was set
     * @param setting The specified time picker
     * @throws Exception
     */
    void verifyTimePickerSetting(String setting) throws Exception {
        String originalTime = preferences.getPreferenceSummary(setting);

        // Change the current time preference
        preferences.changeTimePreferenceValue(setting);

        // Capture the value
        String changedTime = preferences.getPreferenceSummary(setting);

        //verify it actually changed
        assertNotSame(originalTime, changedTime);

        // Reopen settings
        navigateBack();
        navigateToPreferences();

        // Verify its still the captured value
        assertEquals(changedTime, preferences.getPreferenceSummary(setting));
    }

    /**
     * Wait for the notification alert to arrive by polling the notification area
     * @param uniqueAlertId The string used to identify push messages
     * @return <code>true</code> if a notification exists, otherwise <code>false</code>
     * @throws InterruptedException
     */
    boolean waitForNotificationToArrive(String uniqueAlertId) throws InterruptedException {
        return waitForNotificationToArrive(uniqueAlertId, NOTIFICATION_WAIT_TIME);
    }

    /**
     * Wait for the notification alert to arrive by polling the notification area
     * @param uniqueAlertId The string used to identify push messages
     * @param waitTime Time to wait for the notification to arrive
     * @return <code>true</code> if a notification exists, otherwise <code>false</code>
     * @throws InterruptedException
     */
    boolean waitForNotificationToArrive(String uniqueAlertId, int waitTime) throws InterruptedException {
        // Verify the alert notification with the uniqueAlertId
        UiObject notificationAlert = new UiObject(new UiSelector().textContains(uniqueAlertId));

        return AutomatorUtils.waitForUiObjectsToExist(waitTime, notificationAlert);
    }

    /**
     * Navigate to the application's home screen
     * @throws Exception
     */
    void navigateToAppHome() throws Exception {
        UiObject navigateUpButton = new UiObject(new UiSelector().description("Navigate up"));
        if (navigateUpButton.exists()) {
            navigateUpButton.click();
        }

        UiObject home = new UiObject(new UiSelector().text("Home"));
        if (home.exists()) {
            home.click();
        }

        // Wait for activity
        UiDevice.getInstance().waitForWindowUpdate(null, WINDOW_UPDATE_WAIT_TIME);
    }

    /**
     * Navigate to the Inbox screen
     * @throws Exception
     */
    void navigateToInbox() throws Exception {
        UiObject navigateUpButton = new UiObject(new UiSelector().description("Navigate up"));
        if (navigateUpButton.exists()) {
            navigateUpButton.click();
        }

        UiObject inbox = new UiObject(new UiSelector().text("Inbox"));
        if (inbox.exists()) {
            inbox.click();
        }

        // Wait for activity
        UiDevice.getInstance().waitForWindowUpdate(null, WINDOW_UPDATE_WAIT_TIME);
    }

    /**
     * Navigate to the Preferences screen
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    void navigateToPreferences() throws UiObjectNotFoundException, InterruptedException {
        UiObject preferenceButton = new UiObject(new UiSelector().description("Preferences"));
        AutomatorUtils.waitForUiObjectsToExist(UI_OBJECTS_WAIT_TIME, preferenceButton);
        preferenceButton.click();

        // Wait for activity
        UiDevice.getInstance().waitForWindowUpdate(null, WINDOW_UPDATE_WAIT_TIME);
    }

    void navigateBack() {
        getUiDevice().pressBack();
    }
}
