package com.urbanairship.richpush.sample.test;

import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class RichPushSampleBaseTestCase extends UiAutomatorTestCase {

    static final String TAG = "RichPushSampleUiTests";

    private static final String RICH_PUSH_URL = "https://go.urbanairship.com/api/airmail/send/";
    private static final String MASTER_SECRET = "";
    private static final String APP_KEY = "";
    private static final String RICH_PUSH_USER_ID = "";

    @Override
    public void setUp() throws Exception {
        openApp();
        navigateToAppHome();
    }

    String sendRichPushMessage() throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("{ \"push\": {\"android\": { \"alert\": \"Rich Push Alert\" } },");
        builder.append("\"users\": [\"" + RICH_PUSH_USER_ID + "\"],");
        builder.append("\"title\": \"Rich Push Title\",");
        builder.append("\"message\": \"Rich Push Message\",");
        builder.append("\"content-type\": \"text/html\"}");

        String json = builder.toString();

        URL url = new URL(RICH_PUSH_URL);
        HttpURLConnection conn =
                (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
        conn.setRequestProperty("Content-Type",
                "application/json");
        String basicAuthString =  "Basic "+Base64.encodeToString(String.format("%s:%s", APP_KEY, MASTER_SECRET).getBytes(), Base64.NO_WRAP);
        System.out.println(basicAuthString);
        conn.setRequestProperty("Authorization", basicAuthString);

        // Create the form content
        OutputStream out = conn.getOutputStream();
        Writer writer = new OutputStreamWriter(out, "UTF-8");
        writer.write(json);
        writer.close();
        out.close();

        if (conn.getResponseCode() != 200) {
            Log.e(TAG, "Sending rich push failed with: " + conn.getResponseCode() + " " + conn.getResponseMessage() + " json: " + json);
            throw new IOException(conn.getResponseMessage());
        }

        // Buffer the result into a string
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();

        conn.disconnect();
        return sb.toString();
    }

    void setPreferenceCheckBoxEnabled(String setting, boolean enabled) throws UiObjectNotFoundException {
        UiObject preference = new UiObject(new UiSelector().description(setting));
        UiObject preferenceCheckBox =  preference.getChild(new UiSelector().className(android.widget.CheckBox.class));


        if (preferenceCheckBox.isChecked() != enabled) {
            preferenceCheckBox.click();
        }
    }

    void goToPreferences() throws UiObjectNotFoundException {
        // Select the Preferences
        UiObject preferencesButton = new UiObject(new UiSelector().description("Preferences"));
        assertTrue("Unable to detect Preferences button.", preferencesButton.exists());
        preferencesButton.click();
    }

    void clearNotifications() throws UiObjectNotFoundException {

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


    void assertPreferenceViewDisabled(String setting) throws UiObjectNotFoundException {
        UiObject preferenceView = new UiObject(new UiSelector().description(setting));
        assertFalse(preferenceView.isEnabled());
    }

    void verifyTimePickerSetting(String setting) throws UiObjectNotFoundException {
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

    void verifyCheckBoxSetting(String setting) throws UiObjectNotFoundException {
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

    void navigateToAppHome() throws Exception {
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

    void openApp() throws UiObjectNotFoundException {
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
}
