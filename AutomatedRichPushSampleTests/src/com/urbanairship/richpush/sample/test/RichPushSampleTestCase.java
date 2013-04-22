package com.urbanairship.richpush.sample.test;

import android.os.RemoteException;

import com.android.uiautomator.core.UiCollection;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

public class RichPushSampleTestCase extends UiAutomatorTestCase {

    @Override
    public void setUp() throws Exception {
        // Open application
        openApp();

        // Navigate to home
        navigateToAppHome();
    }

    public void testRichPushNotification() throws Exception {
        // Make sure push is enabled
        goToPreferences();
        setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);
        this.getUiDevice().pressBack();

        // Wait a second for any push registration to take place
        Thread.sleep(5000);

        clearNotifications();

        SendPushUtils.sendRichPushMessage();

        // Wait for notification to arrive
        this.getUiDevice().waitForWindowUpdate(null, 10000);

        openRichPushNotification();

        // Wait for views to load
        this.getUiDevice().waitForWindowUpdate(null, 1000);

        // Check for notification being displayed (web view)
        UiObject webview = new UiObject(new UiSelector().className("android.webkit.WebView"));
        assertTrue("Failed to display notification in a webview", webview.exists());

        // Send push to main activity
        SendPushUtils.sendRichPushMessage("home");

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
        SendPushUtils.sendRichPushMessage();

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
        Thread.sleep(5000);

        // Count number of messages
        int originalMessageCount = 0;
        try {
            originalMessageCount = new UiCollection(new UiSelector().className("android.widget.ListView")).getChildCount();
        } catch (Exception ex) {
            // must not exist yet
        }

        // Send push
        SendPushUtils.sendRichPushMessage();
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

        int lastMessageCount = 0;
        try {
            lastMessageCount = new UiCollection(new UiSelector().className("android.widget.ListView")).getChildCount();
        } catch (Exception ex) {
            // must not exist anymore
        }

        // Check that we have one less message
        assertEquals(originalMessageCount, lastMessageCount);
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

    // Helpers

    private void setPreferenceCheckBoxEnabled(String setting, boolean enabled) throws UiObjectNotFoundException {
        UiObject preference = new UiObject(new UiSelector().description(setting));
        UiObject preferenceCheckBox =  preference.getChild(new UiSelector().className(android.widget.CheckBox.class));


        if (preferenceCheckBox.isChecked() != enabled) {
            preferenceCheckBox.click();
        }
    }

    private void goToPreferences() throws UiObjectNotFoundException {
        // Select the Preferences
        UiObject preferencesButton = new UiObject(new UiSelector().description("Preferences"));
        assertTrue("Unable to detect Preferences button.", preferencesButton.exists());
        preferencesButton.click();
    }

    private void clearNotifications() throws UiObjectNotFoundException {

        // Open notification area
        this.getUiDevice().swipe(50, 2, 50, this.getUiDevice().getDisplayHeight(), 5);

        // Click the clear all notifications button
        UiObject clearButton = new UiObject(new UiSelector().description("Clear all notifications."));

        if (clearButton.exists()) {
            clearButton.click();
        } else {
            this.getUiDevice().pressBack();
        }
    }

    private void assertPreferenceViewDisabled(String setting) throws UiObjectNotFoundException {
        UiObject preferenceView = new UiObject(new UiSelector().description(setting));
        assertFalse(preferenceView.isEnabled());
    }

    private void verifyTimePickerSetting(String setting) throws UiObjectNotFoundException {
        UiObject timePicker = new UiObject(new UiSelector().description(setting));
        UiObject okButton = new UiObject(new UiSelector().className("android.widget.Button").text("OK"));

        timePicker.click();

        // Change the time and capture
        for (int i = 0; i < 3; i++) {
            UiObject numberPicker = new UiObject(new UiSelector().className("android.widget.NumberPicker").index(i));
            UiObject button = numberPicker.getChild(new UiSelector().className("android.widget.Button"));
            button.click();
        }

        // Go in and out of the time picker to grab the set text.  The edit text is not available
        // if we do it right away...
        okButton.click();
        timePicker.click();

        // Capture the set time
        String capturedTime = "";
        for (int i = 0; i < 3; i++) {
            UiObject numberPicker = new UiObject(new UiSelector().className("android.widget.NumberPicker").index(i));
            UiObject editText = numberPicker.getChild(new UiSelector().className("android.widget.EditText"));
            capturedTime += editText.getText();
        }

        // Back out of activity
        okButton.click();
        this.getUiDevice().pressBack();

        // Go back into the time picker
        goToPreferences();
        timePicker.click();

        // Grab the current time
        String setTime = "";
        for (int i = 0; i < 3; i++) {
            UiObject numberPicker = new UiObject(new UiSelector().className("android.widget.NumberPicker").index(i));
            UiObject editText = numberPicker.getChild(new UiSelector().className("android.widget.EditText"));
            setTime += editText.getText();
        }

        okButton.click();

        assertEquals("Failed to set quiet times", capturedTime, setTime);
    }

    private void verifyCheckBoxSetting(String setting) throws UiObjectNotFoundException {
        boolean isEnabled;
        UiObject settingCheckBox = new UiObject(new UiSelector().description(setting));

        settingCheckBox.click();
        isEnabled = settingCheckBox.isChecked();
        this.getUiDevice().pressBack();
        goToPreferences();

        assertEquals("Setting " + setting + " did not toggle correctly", isEnabled, settingCheckBox.isChecked());

        settingCheckBox.click();
        isEnabled = settingCheckBox.isChecked();
        this.getUiDevice().pressBack();
        goToPreferences();

        assertEquals("Setting " + setting + " did not toggle correctly", isEnabled, settingCheckBox.isChecked());
    }

    private void navigateToAppHome() throws Exception {
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
    }

    private void navigateToInbox() throws Exception {
        navigateToAppHome();
        UiObject spinner = new UiObject(new UiSelector().className("android.widget.Spinner"));
        spinner.click();

        this.getUiDevice().waitForWindowUpdate(null, 1000);

        UiObject inbox = new UiObject(new UiSelector().text("Inbox"));
        inbox.click();

        // Wait for activity
        this.getUiDevice().waitForWindowUpdate(null, 1000);
    }

    private void openApp() throws UiObjectNotFoundException {
        try {
            getUiDevice().wakeUp();
        } catch (RemoteException e1) {
            // We're probably doomed, but leave a note.
            e1.printStackTrace();
        }

        // Simulate a short press on the HOME button.
        getUiDevice().pressHome();

        // Hit it a few times to bypass the welcome screen
        getUiDevice().pressHome();
        getUiDevice().pressHome();

        // We're now in the home screen. Next, we want to simulate
        // a user bringing up the All Apps screen.
        // If you use the uiautomatorviewer tool to capture a snapshot
        // of the Home screen, notice that the All Apps button's
        // content-description property has the value "Apps".  We can
        // use this property to create a UiSelector to find the button.
        UiObject allAppsButton = new UiObject(new UiSelector().description("Apps"));

        // Simulate a click to bring up the All Apps screen.
        allAppsButton.clickAndWaitForNewWindow();

        // In the All Apps screen, the Settings app is located in
        // the Apps tab. To simulate the user bringing up the Apps tab,
        // we create a UiSelector to find a tab with the text
        // label "Apps".
        UiObject appsTab = new UiObject(new UiSelector().text("Apps"));

        // Simulate a click to enter the Apps tab.
        appsTab.click();

        // Next, in the apps tabs, we can simulate a user swiping until
        // they come to the Settings app icon.  Since the container view
        // is scrollable, we can use a UiScrollable object.
        UiScrollable appViews = new UiScrollable(new UiSelector().scrollable(true));

        // Set the swiping mode to horizontal (the default is vertical)
        appViews.setAsHorizontalList();

        // Create a UiSelector to find the Settings app and simulate
        // a user click to launch the app.
        UiObject settingsApp = appViews.getChildByText(new UiSelector().className(android.widget.TextView.class.getName()), "Rich Push Sample");
        settingsApp.clickAndWaitForNewWindow();

        // Validate that the package name is the expected one
        UiObject pushSampleValidation = new UiObject(new UiSelector().packageName("com.urbanairship.richpush.sample"));
        assertTrue("Unable to detect Rich Push Sample", pushSampleValidation.exists());
    }

    private void openRichPushNotification() throws UiObjectNotFoundException {
        // Open notification area
        openNotificationArea();

        assertTrue("No push notifications to open",  richPushNotificationExists());

        // Open notification
        UiObject notificationAlert = new UiObject(new UiSelector().text("Rich Push Alert"));
        notificationAlert.click();
    }

    private void openNotificationArea() {
        this.getUiDevice().swipe(50, 2, 50, this.getUiDevice().getDisplayHeight(), 5);
    }

    private boolean richPushNotificationExists() {
        // Check for notification
        UiObject notificationTitle = new UiObject(new UiSelector().text("Rich Push Sample"));
        UiObject notificationAlert = new UiObject(new UiSelector().text("Rich Push Alert"));

        return notificationTitle.exists() && notificationAlert.exists();
    }
}
