package com.urbanairship.richpush.sample.test;

import com.android.uiautomator.core.UiCollection;
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
        this.getUiDevice().waitForWindowUpdate(null, 10000);

        openRichPushNotification();

        // Wait for views to load
        this.getUiDevice().waitForWindowUpdate(null, 1000);

        // Check for notification being displayed (web view)
        UiObject webview = new UiObject(new UiSelector().className("android.webkit.WebView"));
        assertTrue("Failed to display notification in a webview", webview.exists());

        // Send push to main activity
        sendRichPushMessage("home");

        // Wait for notification to arrive
        this.getUiDevice().waitForWindowUpdate(null, 10000);

        openRichPushNotification();

        // Make sure we have a dialog fragment and web view in main activity
        UiObject richPushDialog = new UiObject(new UiSelector().className("android.webkit.WebView").description("Rich push message dialog"));
        assertTrue("Failed to display notification in a webview", richPushDialog.exists());

        this.getUiDevice().pressBack();

        // Disable push to verify we don't receive push notifications
        goToPreferences();
        setPreferenceCheckBoxEnabled("PUSH_ENABLE", false);
        this.getUiDevice().pressBack();

        // Send a notification
        sendRichPushMessage();

        // Give time for a notification to arrive
        this.getUiDevice().waitForWindowUpdate(null, 10000);

        openNotificationArea();

        assertFalse("Received push notification when push is disabled", richPushNotificationExists());
    }

    public void testInbox() throws Exception {

        navigateToInbox();

        // Enable push
        goToPreferences();
        setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);
        this.getUiDevice().pressBack();

        // Wait a second for any push registration to take place
        Thread.sleep(3000);

        // Count number of messages
        int originalMessageCount = new UiCollection(new UiSelector().className("android.widget.ListView")).getChildCount();

        // Send push
        sendRichPushMessage();
        this.getUiDevice().waitForWindowUpdate(null, 10000);

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

        // Check that we have one less message
        assertEquals(originalMessageCount, new UiCollection(new UiSelector().className("android.widget.ListView")).getChildCount());
    }

    public void testPreferences() throws UiObjectNotFoundException {
        goToPreferences();

        // Push Settings

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

        // Make sure the rest of the push preference views are disabled
        assertPreferenceViewDisabled("SOUND_ENABLE");
        assertPreferenceViewDisabled("VIBRATE_ENABLE");
        assertPreferenceViewDisabled("QUIET_TIME_ENABLE");

        // Location Settings

        // Test for parent location setting
        verifyCheckBoxSetting("LOCATION_ENABLE");

        // The other location preferences depend on having location enabled
        setPreferenceCheckBoxEnabled("LOCATION_ENABLE", true);

        // Test foreground and background enable preferences
        verifyCheckBoxSetting("LOCATION_FOREGROUND_ENABLE");
        verifyCheckBoxSetting("LOCATION_BACKGROUND_ENABLE");

        // Disable location settings
        setPreferenceCheckBoxEnabled("LOCATION_ENABLE", false);

        // Make sure the rest of the location preference views are disabled
        assertPreferenceViewDisabled("LOCATION_FOREGROUND_ENABLE");
        assertPreferenceViewDisabled("LOCATION_BACKGROUND_ENABLE");
    }
}
