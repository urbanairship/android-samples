package com.urbanairship.richpush.sample.test;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;

public class RichPushSampleTestCase extends RichPushSampleBaseTestCase {

    public void testRichPushNotification() throws Exception {
        // Make sure push is enabled
        goToPreferences();
        setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);
        this.getUiDevice().pressBack();

        // Wait a second for any push registration to take place
        Thread.sleep(3000);

        clearNotifications();

        sendRichPushMessage();

        // Wait for notification to arrive
        this.getUiDevice().waitForWindowUpdate(null, 5000);

        // Open notification area
        this.getUiDevice().swipe(50, 2, 50, this.getUiDevice().getDisplayHeight(), 5);

        // Check for notification
        UiObject notificationTitle = new UiObject(new UiSelector().text("Rich Push Sample"));
        UiObject notificationAlert = new UiObject(new UiSelector().text("Rich Push Alert"));
        assertTrue("Failed to receive rich push notification", notificationTitle.exists());
        assertTrue("Failed to receive rich push notification alert", notificationAlert.exists());

        // Open notification
        notificationAlert.click();

        // Wait for views to load
        this.getUiDevice().waitForWindowUpdate(null, 1000);

        // Check for notification being displayed (web view)
        UiObject webview = new UiObject(new UiSelector().className("android.webkit.WebView"));
        assertTrue("Failed to display notification in a webview", webview.exists());
    }

    public void testPushSettings() throws UiObjectNotFoundException {
        goToPreferences();

        // Test for parent push setting
        verifyCheckBoxSetting("PUSH_ENABLE");

        // The rest depend on having push enabled
        setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);

        // Test sound, vibrate, and quiet time enable preferences
        verifyCheckBoxSetting("SOUND_ENABLE");
        verifyCheckBoxSetting("VIBRATE_ENABLE");
        verifyCheckBoxSetting("QUIET_TIME_ENABLE");

        // Quiet times depend on having quiet time enabled
        setPreferenceCheckBoxEnabled("QUIET_TIME_ENABLE", true);

        // Test for quiet time start and end times
        verifyTimePickerSetting("QUIET_TIME_START");
        verifyTimePickerSetting("QUIET_TIME_END");

        // Disable quiet time enable
        setPreferenceCheckBoxEnabled("QUIET_TIME_ENABLE", false);

        // Make sure quiet time setting views are disabled
        assertPreferenceViewDisabled("QUIET_TIME_START");
        assertPreferenceViewDisabled("QUIET_TIME_END");

        // Disable push settings
        setPreferenceCheckBoxEnabled("PUSH_ENABLE", false);

        // Make sure the rest of the preference views are disabled
        assertPreferenceViewDisabled("SOUND_ENABLE");
        assertPreferenceViewDisabled("VIBRATE_ENABLE");
        assertPreferenceViewDisabled("QUIET_TIME_ENABLE");
    }
}
