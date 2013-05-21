package com.urbanairship.richpush.sample.test;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;

/**
 * Helper class to navigate through the app
 *
 */
public class RichPushSampleNavigator {

    /**
     * Navigate to the application's home screen
     * @throws Exception
     */
    public void navigateToAppHome() throws Exception {
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
        UiDevice.getInstance().waitForWindowUpdate(null, 1000);
    }

    /**
     * Navigate to the Inbox screen
     * @throws Exception
     */
    public void navigateToInbox() throws Exception {
        navigateToAppHome();
        UiObject spinner = new UiObject(new UiSelector().className("android.widget.Spinner"));
        AutomatorUtils.waitForUiObjectsToExist(1000, spinner);
        spinner.click();

        UiObject inbox = new UiObject(new UiSelector().text("Inbox"));
        AutomatorUtils.waitForUiObjectsToExist(1000, inbox);
        inbox.click();

        // Wait for activity
        UiDevice.getInstance().waitForWindowUpdate(null, 1000);
    }

    /**
     * Navigate to the Preferences screen
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    public void navigateToPreferences() throws UiObjectNotFoundException, InterruptedException {
        UiObject preferenceButton = new UiObject(new UiSelector().description("Preferences"));
        AutomatorUtils.waitForUiObjectsToExist(1000, preferenceButton);
        preferenceButton.click();

        // Wait for activity
        UiDevice.getInstance().waitForWindowUpdate(null, 5000);
    }
}
