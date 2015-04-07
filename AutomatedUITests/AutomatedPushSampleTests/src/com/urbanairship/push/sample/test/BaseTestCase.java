package com.urbanairship.push.sample.test;

// Import the uiautomator libraries
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;
import com.urbanairship.automatorutils.AutomatorUtils;
import com.urbanairship.automatorutils.PreferencesHelper;
import com.urbanairship.automatorutils.PushSender;
import com.urbanairship.automatorutils.PushSenderApiV3;

/**
 * Automated testing of the Push Sample application
 *
 */
public class BaseTestCase extends UiAutomatorTestCase {

    static int REGISTRATION_WAIT_TIME = 30000; // 30 seconds

    // Time to wait for notifications to appear in milliseconds.
    static int NOTIFICATION_WAIT_TIME = 120000; // 120 seconds - push to tags is slower than to user
    static final String APP_NAME = "Push Sample";
    static final String PACKAGE_NAME = "com.urbanairship.push.sample";
    static final String TEST_ALIAS_STRING = AutomatorUtils.generateUniqueAlertId();
    static final String TEST_NAMED_USER_STRING = AutomatorUtils.generateUniqueAlertId();
    static final String TEST_TAG_STRING = "TEST_PUSH_SAMPLE_TAG";

    PushSenderApiV3 pushSender;
    PreferencesHelper preferences;


    /**
     * Prepare for testing, which includes getting the appKey and masterSecret.
     * Also generates the uniqueAlertId.
     */
    @Override
    public void setUp() throws Exception {

        // Get the app key and master secret from the params
        String appKey = getParams().getString("APP_KEY");
        String masterSecret = getParams().getString("MASTER_SECRET");

        pushSender = new PushSenderApiV3(masterSecret, appKey);
        preferences = new PreferencesHelper();

        // Open application
        assertTrue("Failed to open Push Sample", AutomatorUtils.openApp(APP_NAME, PACKAGE_NAME));
    }

    /**
     * Cleanup - disable user notifications.
     */
    @Override
    public void tearDown() throws Exception {
        // Disable push
        UiObject preferencesButton = new UiObject(new UiSelector().text("Preferences"));
        preferencesButton.click();
        preferences.setPreferenceCheckBoxEnabled("USER_NOTIFICATIONS_ENABLED", false);

        // Go out and back to apply settings
        this.getUiDevice().pressBack();
    }

    // Helpers
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
}