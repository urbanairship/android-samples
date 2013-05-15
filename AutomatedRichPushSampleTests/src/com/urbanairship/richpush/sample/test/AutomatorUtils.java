package com.urbanairship.richpush.sample.test;

import android.os.RemoteException;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;

/**
 * Utility class for automation
 *
 */
public class AutomatorUtils {
    /**
     * Open the notification area
     */
    public static void openNotificationArea() {
        UiDevice device = UiDevice.getInstance();
        device.swipe(50, 2, 50, device.getDisplayHeight(), 5);
    }

    /**
     * Waits for UiObjects to exist
     * @param timeInMilliseconds Time to wait for ui objects to exist
     * @param uiObjects UiObjects to check for
     * @return <code>true</code> if all ui objects exist, otherwise <code>false</code>
     * @throws InterruptedException
     */
    public static boolean waitForUiObjectsToExist(int timeInMilliseconds, UiObject... uiObjects) throws InterruptedException {
        if (uiObjects == null || uiObjects.length == 0) {
            return false;
        }

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeInMilliseconds) {
            boolean allExist = true;
            for (UiObject uiObject : uiObjects) {
                if (!uiObject.exists()) {
                    allExist = false;
                }
            }

            if (allExist) {
                return true;
            } else {
                Thread.sleep(1000);
            }
        }

        return false;
    }

    /**
     * Find and open the app
     * @param appName The name of the app to open
     * @param packageName The package name of the app to open
     * @return <code>true</code> if app was opened, otherwise <code>false</code>
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    public static boolean openApp(String appName, String packageName) throws UiObjectNotFoundException, InterruptedException {
        UiDevice device = UiDevice.getInstance();

        try {
            device.wakeUp();
        } catch (RemoteException e1) {
            // We're probably doomed, but leave a note.
            e1.printStackTrace();
        }

        // Simulate a short press on the HOME button.
        device.pressHome();

        // Hit it a few times to bypass the welcome screen
        device.pressHome();
        device.pressHome();

        // If a semi-transparent welcome help overlay exists,
        // click the OK button to dismiss it.
        UiObject okDismissHelpOverlayButton = new UiObject(new UiSelector().text("OK"));
        if (okDismissHelpOverlayButton.exists()) {
            okDismissHelpOverlayButton.click();
        }

        // We're now in the home screen. Next, we want to simulate
        // a user bringing up the All Apps screen.
        // If you use the uiautomatorviewer tool to capture a snapshot
        // of the Home screen, notice that the All Apps button's
        // content-description property has the value "Apps".  We can
        // use this property to create a UiSelector to find the button.
        UiObject allAppsButton = new UiObject(new UiSelector().description("Apps"));
        waitForUiObjectsToExist(5000, allAppsButton);
        // Simulate a click to bring up the All Apps screen.
        allAppsButton.clickAndWaitForNewWindow();

        // In the All Apps screen, the Settings app is located in
        // the Apps tab. To simulate the user bringing up the Apps tab,
        // we create a UiSelector to find a tab with the text
        // label "Apps".
        UiObject appsTab = new UiObject(new UiSelector().text("Apps"));

        // Simulate a click to enter the Apps tab.
        appsTab.click();

        // If a semi-transparent welcome help overlay exists,
        // click the OK button to dismiss it.
        okDismissHelpOverlayButton = new UiObject(new UiSelector().text("OK"));
        if (okDismissHelpOverlayButton.exists()) {
            okDismissHelpOverlayButton.click();
        }

        // Next, in the apps tabs, we can simulate a user swiping until
        // they come to the Settings app icon.  Since the container view
        // is scrollable, we can use a UiScrollable object.
        UiScrollable appViews = new UiScrollable(new UiSelector().scrollable(true));

        // Set the swiping mode to horizontal (the default is vertical)
        appViews.setAsHorizontalList();

        // Create a UiSelector to find the Settings app and simulate
        // a user click to launch the app.
        UiObject settingsApp = appViews.getChildByText(new UiSelector().className(android.widget.TextView.class.getName()), appName);
        settingsApp.clickAndWaitForNewWindow();

        // Validate that the package name is the expected one
        UiObject pushSampleValidation = new UiObject(new UiSelector().packageName(packageName));
        return pushSampleValidation.exists();
    }



    /**
     * Clears all the notifications in the notification area
     * @throws UiObjectNotFoundException
     */
    public static void clearNotifications() throws UiObjectNotFoundException {
        UiDevice device = UiDevice.getInstance();

        // Open notification area
        device.swipe(50, 2, 50, device.getDisplayHeight(), 5);

        // Click the clear all notifications button
        UiObject clearButton = new UiObject(new UiSelector().description("Clear all notifications."));

        if (clearButton.exists()) {
            clearButton.click();
        } else {
            device.pressBack();
        }
    }
}
