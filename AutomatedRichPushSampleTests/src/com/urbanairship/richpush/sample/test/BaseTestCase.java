package com.urbanairship.richpush.sample.test;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;
import com.urbanairship.automatorutils.AutomatorUtils;
import com.urbanairship.automatorutils.PreferencesHelper;
import com.urbanairship.automatorutils.RichPushSender;
import com.urbanairship.automatorutils.RichPushSenderApiV3;

/**
 * Automated testing of the Rich Push Sample application
 *
 */
public class BaseTestCase extends UiAutomatorTestCase {

    static int REGISTRATION_WAIT_TIME = 60000; // 60 seconds
    static int UNREGISTER_WAIT_TIME = 10000; // 10 seconds
    static int WINDOW_UPDATE_WAIT_TIME = 5000;  // 5 seconds
    static int MESSAGE_RETRIEVAL_WAIT_TIME = 5000;  // 5 seconds
    static int RICH_PUSH_DIALOG_WAIT_TIME = 20000;  // 20 seconds
    static int SETTING_PUSH_PREFERENCE_WAIT_TIME = 2000;  // 2 seconds

    // Time to wait for notifications to appear in milliseconds.
    static int NOTIFICATION_WAIT_TIME = 120000; // 120 seconds - push to tags is slower than to user
    static final String APP_NAME = "Rich Push Sample";
    static final String PACKAGE_NAME = "com.urbanairship.richpush.sample";
    static final String TEST_ALIAS_STRING = AutomatorUtils.generateUniqueAlertId();
    static final String TEST_TAG_STRING = "TEST_RICH_PUSH_SAMPLE_TAG";

    RichPushSender pushSender;
    RichPushSenderApiV3 pushSenderV3;
    PreferencesHelper preferences;
    RichPushSampleNavigator appNavigator;

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
     * Wait for the notification alert to arrive by polling the notification area
     * @param uniqueAlertId The string used to identify push messages
     * @return <code>true</code> if a notification exists, otherwise <code>false</code>
     * @throws InterruptedException
     */
    boolean waitForNotificationToArrive(String uniqueAlertId) throws InterruptedException {
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
    void verifyRichPushNotification(String description, String uniqueAlertId, String failureMessage) throws InterruptedException, UiObjectNotFoundException {
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
