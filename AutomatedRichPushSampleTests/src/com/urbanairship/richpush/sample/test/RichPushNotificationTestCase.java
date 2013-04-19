package com.urbanairship.richpush.sample.test;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiSelector;

public class RichPushNotificationTestCase extends RichPushSampleBaseTestCase {

    public void testRichPushNotification() throws Exception {

        // Make sure push is enabled
        goToPreferences();
        enablePreference("PUSH_ENABLE");
        this.getUiDevice().pressBack();

        clearNotifications();

        sendRichPushMessage();

        // wait for notification to arrive
        this.getUiDevice().waitForWindowUpdate(null, 5000);

        // open notification area
        this.getUiDevice().swipe(50, 2, 50, this.getUiDevice().getDisplayHeight(), 5);

        // check for notification application name
        UiObject appName = new UiObject(new UiSelector().text("Rich Push Sample"));
        assertTrue(appName.exists());

        // check for notification alert
        UiObject alert = new UiObject(new UiSelector().text("Rich Push Alert"));
        assertTrue(alert.exists());

        // click on notification alert
        alert.click();

        // wait for views to load
        this.getUiDevice().waitForWindowUpdate(null, 1000);

        // check for notification being displayed (web view)
        UiObject webview = new UiObject(new UiSelector().className("android.webkit.WebView"));
        assertTrue(webview.exists());
    }
}
