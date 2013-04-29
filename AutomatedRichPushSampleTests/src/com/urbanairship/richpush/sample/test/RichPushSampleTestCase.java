package com.urbanairship.richpush.sample.test;

import android.os.RemoteException;

import com.android.uiautomator.core.UiCollection;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
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
    private static final String TEST_SECOND_TAG_STRING = "TEST_SECOND_TAG";

    private PushSender pushSender;

    /**
     * Prepare for testing, which includes getting the masterSecret and appKey
     */
    @Override
    public void setUp() throws Exception {
        // Create a push sender with the master secret and app key from the params
        String masterSecret = getParams().getString("MASTER_SECRET");
        String appKey = getParams().getString("APP_KEY");
        pushSender = new PushSender(masterSecret, appKey);

        // Open application
        openApp();

        // Navigate to home
        navigateToAppHome();
    }

    /**
     * Test the sending and receiving of a rich push message
     * @throws Exception
     */
    public void testRichPushNotification() throws Exception {
        // Make sure push is enabled
        goToPreferences();
        setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);
        this.getUiDevice().pressBack();

        // Wait a second for any push registration to take place
        Thread.sleep(5000);

        clearNotifications();

        pushSender.sendRichPushMessage();

        openNotificationArea();
        waitForNotificationToArrive();
        openRichPushNotification();

        // Check for notification being displayed (web view)
        UiObject webview = new UiObject(new UiSelector().className("android.webkit.WebView"));
        assertTrue("Failed to display notification in a webview", waitForUiObjectsToExist(10000, webview));

        // Send push to main activity
        pushSender.sendRichPushMessage("home");

        openNotificationArea();
        waitForNotificationToArrive();
        openRichPushNotification();

        // Make sure we have a dialog fragment and web view in main activity
        UiObject richPushDialog = new UiObject(new UiSelector().className("android.webkit.WebView").description("Rich push message dialog"));
        assertTrue("Failed to display notification in a webview",  waitForUiObjectsToExist(10000, richPushDialog));

        this.getUiDevice().pressBack();

        // Disable push to verify we don't receive push notifications
        goToPreferences();
        setPreferenceCheckBoxEnabled("PUSH_ENABLE", false);
        this.getUiDevice().pressBack();

        // Send a notification
        pushSender.sendRichPushMessage();

        openNotificationArea();
        assertFalse("Received push notification when push is disabled", waitForNotificationToArrive());


        this.getUiDevice().pressBack();
    }



    /**
     * Tests the UI for receiving a rich push message, marking it read, unread and deleting it
     * @throws Exception
     */
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
        pushSender.sendRichPushMessage();

        // Wait for it to arrive
        openNotificationArea();
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
     * Test the setting of all push and location preferences
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    public void testPreferences() throws UiObjectNotFoundException, InterruptedException {
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

        // Advanced Settings

        // Test for parent push setting
        verifyCheckBoxSetting("PUSH_ENABLE");

        // The rest depend on having push enabled
        setPreferenceCheckBoxEnabled("PUSH_ENABLE", true);

        // Test set alias
        // Scroll to the preference if its not visible in the list
        UiScrollable listView = new UiScrollable(new UiSelector().className("android.widget.ListView"));
        listView.scrollDescriptionIntoView("SET_ALIAS");

        UiObject setAlias = new UiObject(new UiSelector().description("SET_ALIAS"));
        UiObject aliasStringDisplayed = new UiObject(new UiSelector().text(TEST_ALIAS_STRING));
        boolean aliasExist = false;
        if (aliasStringDisplayed.exists()) {
            aliasExist = true;
        }

        setAlias.click();

        if (aliasExist) {
            UiObject aliasEditText = new UiObject(new UiSelector().text(TEST_ALIAS_STRING));
            aliasEditText.click();
            UiObject deleteAlias = new UiObject(new UiSelector().text("Delete"));
            if (deleteAlias.exists()) {
                // Alias exist, so clear it
                deleteAlias.click();
            }
        }

        UiObject setAliasText = new UiObject(new UiSelector().className("android.widget.EditText"));
        setAliasText.click();

        // Set the alias
        setAliasText.setText(TEST_ALIAS_STRING);

        // save
        UiObject okButton = new UiObject(new UiSelector().text("OK"));
        okButton.click();

        // Check alias string is displayed
        listView.scrollDescriptionIntoView(TEST_ALIAS_STRING);
        aliasStringDisplayed = new UiObject(new UiSelector().text(TEST_ALIAS_STRING));
        assertTrue("Failed to display alias string", waitForUiObjectsToExist(10000, aliasStringDisplayed));

        // Test set tags
        // Scroll to the preference if its not visible in the list
        listView.scrollDescriptionIntoView("SET_TAGS");

        UiObject setTags = new UiObject(new UiSelector().description("SET_TAGS"));
        setTags.click();

        // Set tags
        UiObject setTagsText = new UiObject(new UiSelector().className("android.widget.EditText"));

        // Add first tag
        setTagsText.click();

        // Check if a tag exist
        UiObject listViewOfTags = new UiObject(new UiSelector().className("android.widget.ListView"));
        if (listViewOfTags.exists()) {
            // Tags exist, so clear them
            // TODO
        }

        setTagsText.setText(TEST_FIRST_TAG_STRING);
        UiObject addTagButton = new UiObject(new UiSelector().className("android.widget.ImageButton"));
        addTagButton.click();

        // Save first tag
        okButton = new UiObject(new UiSelector().text("OK"));
        okButton.click();

        // Check first added tag is displayed
        listView.scrollDescriptionIntoView(TEST_FIRST_TAG_STRING);
        UiObject firstTagStringDisplayed = new UiObject(new UiSelector().text(TEST_FIRST_TAG_STRING));
        assertTrue("Failed to display first tag string", waitForUiObjectsToExist(10000, firstTagStringDisplayed));
    }

    // Helpers
    /**
     * Set the specified preference setting
     * @param setting The specified preference to be set
     * @param enabled Boolean to enable or disable the specified setting
     * @throws UiObjectNotFoundException
     */
    private void setPreferenceCheckBoxEnabled(String setting, boolean enabled) throws UiObjectNotFoundException {
        // Scroll to the preference if its not visible in the list
        UiScrollable listView = new UiScrollable(new UiSelector().className("android.widget.ListView"));
        listView.scrollDescriptionIntoView(setting);

        UiObject preference = new UiObject(new UiSelector().description(setting));
        UiObject preferenceCheckBox =  preference.getChild(new UiSelector().className(android.widget.CheckBox.class));

        if (preferenceCheckBox.isChecked() != enabled) {
            preferenceCheckBox.click();
        }
    }

    /**
     * Navigate to the Preferences screen
     * @throws UiObjectNotFoundException
     */
    private void goToPreferences() throws UiObjectNotFoundException {
        // Select the Preferences
        UiObject preferencesButton = new UiObject(new UiSelector().description("Preferences"));
        assertTrue("Unable to detect Preferences button.", preferencesButton.exists());
        preferencesButton.click();
    }

    /**
     * Clears all the notifications in the notification area
     * @throws UiObjectNotFoundException
     */
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

    /**
     * Check specified preference view is disabled
     * @param setting The specified preference setting
     * @throws UiObjectNotFoundException
     */
    private void assertPreferenceViewDisabled(String setting) throws UiObjectNotFoundException {
        UiObject preferenceView = new UiObject(new UiSelector().description(setting));
        assertFalse(preferenceView.isEnabled());
    }

    /**
     * Check the time picker setting was set
     * @param setting The specified time picker
     * @throws UiObjectNotFoundException
     */
    private void verifyTimePickerSetting(String setting) throws UiObjectNotFoundException {
        // Scroll to the preference if its not visible in the list
        UiScrollable listView = new UiScrollable(new UiSelector().className("android.widget.ListView"));
        listView.scrollDescriptionIntoView(setting);

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

        // Scroll to the preference if its not visible in the list
        listView.scrollDescriptionIntoView(setting);

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

    /**
     * Verify the checkbox state of the preference
     * @param setting The specified preference
     * @throws UiObjectNotFoundException
     */
    private void verifyCheckBoxSetting(String setting) throws UiObjectNotFoundException {
        boolean isEnabled;

        // Scroll to the preference if its not visible in the list
        UiScrollable listView = new UiScrollable(new UiSelector().className("android.widget.ListView"));
        listView.scrollDescriptionIntoView(setting);

        UiObject settingCheckBox = new UiObject(new UiSelector().description(setting));

        settingCheckBox.click();
        isEnabled = settingCheckBox.isChecked();
        this.getUiDevice().pressBack();
        goToPreferences();

        // Scroll to the preference if its not visible in the list
        listView.scrollDescriptionIntoView(setting);

        assertEquals("Setting " + setting + " did not toggle correctly", isEnabled, settingCheckBox.isChecked());

        settingCheckBox.click();
        isEnabled = settingCheckBox.isChecked();
        this.getUiDevice().pressBack();
        goToPreferences();

        // Scroll to the preference if its not visible in the list
        listView.scrollDescriptionIntoView(setting);

        assertEquals("Setting " + setting + " did not toggle correctly", isEnabled, settingCheckBox.isChecked());
    }

    /**
     * Navigate to the application's home screen
     * @throws Exception
     */
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

    /**
     * Navigate to the Inbox screen
     * @throws Exception
     */
    private void navigateToInbox() throws Exception {
        navigateToAppHome();
        UiObject spinner = new UiObject(new UiSelector().className("android.widget.Spinner"));
        spinner.click();

        UiObject inbox = new UiObject(new UiSelector().text("Inbox"));
        this.waitForUiObjectsToExist(1000, inbox);
        inbox.click();

        // Wait for activity
        this.getUiDevice().waitForWindowUpdate(null, 1000);
    }

    /**
     * Find and open the Rich Push Sample app
     * @throws UiObjectNotFoundException
     */
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

    /**
     * Open the rich push notification from the notification area
     * @throws UiObjectNotFoundException
     */
    private void openRichPushNotification() throws UiObjectNotFoundException {
        // Open notification
        UiObject notificationAlert = new UiObject(new UiSelector().text("Rich Push Alert"));

        assertTrue("No push notifications to open",  notificationAlert.exists());
        notificationAlert.click();
    }

    /**
     * Open the notification area
     */
    private void openNotificationArea() {
        this.getUiDevice().swipe(50, 2, 50, this.getUiDevice().getDisplayHeight(), 5);
    }

    /**
     * Wait for the notification alert to arrive by polling the notification area
     * @return <code>true</code> if a notification exists, otherwise <code>false</code>
     * @throws InterruptedException
     */
    private boolean waitForNotificationToArrive() throws InterruptedException {
        UiObject notificationTitle = new UiObject(new UiSelector().text("Rich Push Sample"));
        UiObject notificationAlert = new UiObject(new UiSelector().text("Rich Push Alert"));

        return waitForUiObjectsToExist(NOTIFICATION_WAIT_TIME, notificationTitle, notificationAlert);
    }

    /**
     * Waits for UiObjects to exist
     * @param timeInMilliseconds Time to wait for ui objects to exist
     * @param uiObjects UiObjects to check for
     * @return <code>true</code> if all ui objects exist, otherwise <code>false</code>
     * @throws InterruptedException
     */
    private boolean waitForUiObjectsToExist(int timeInMilliseconds, UiObject... uiObjects) throws InterruptedException {
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
}
