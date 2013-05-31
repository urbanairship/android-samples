package com.urbanairship.richpush.sample.test;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;
import com.urbanairship.automatorutils.AutomatorUtils;

/**
 * Helper class to navigate through the app
 *
 */
public class RichPushSampleNavigator {

    private static int WINDOW_UPDATE_WAIT_TIME = 5000;  // 5 seconds
    private static int UI_OBJECTS_WAIT_TIME = 1000;  // 1 second

    /**
     * Navigate to the application's home screen
     * @throws Exception
     */
    public void navigateToAppHome() throws Exception {
        UiObject inbox = new UiObject(new UiSelector().text("Inbox"));
        if (inbox.exists()) {
            UiDevice.getInstance().pressBack();
        }

        UiObject navigateHomeButton = new UiObject(new UiSelector().description("Navigate home"));
        UiObject navigateUpButton = new UiObject(new UiSelector().description("Navigate up"));
        if (navigateHomeButton.exists()) {
            navigateHomeButton.click();
        } else if (navigateUpButton.exists()) {
            navigateUpButton.click();
            navigateHomeButton.click();
        } else {
            throw new Exception("Where are we?");
        }

        // Wait for activity
        UiDevice.getInstance().waitForWindowUpdate(null, WINDOW_UPDATE_WAIT_TIME);
    }

    /**
     * Navigate to the Inbox screen
     * @throws Exception
     */
    public void navigateToInbox() throws Exception {
        navigateToAppHome();
        UiObject spinner = new UiObject(new UiSelector().className("android.widget.Spinner"));
        AutomatorUtils.waitForUiObjectsToExist(UI_OBJECTS_WAIT_TIME, spinner);
        spinner.click();

        UiObject inbox = new UiObject(new UiSelector().text("Inbox"));
        AutomatorUtils.waitForUiObjectsToExist(UI_OBJECTS_WAIT_TIME, inbox);
        inbox.click();

        // Wait for activity
        UiDevice.getInstance().waitForWindowUpdate(null, WINDOW_UPDATE_WAIT_TIME);
    }

    /**
     * Navigate to the Preferences screen
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    public void navigateToPreferences() throws UiObjectNotFoundException, InterruptedException {
        UiObject preferenceButton = new UiObject(new UiSelector().description("Preferences"));
        AutomatorUtils.waitForUiObjectsToExist(UI_OBJECTS_WAIT_TIME, preferenceButton);
        preferenceButton.click();

        // Wait for activity
        UiDevice.getInstance().waitForWindowUpdate(null, WINDOW_UPDATE_WAIT_TIME);
    }
}
