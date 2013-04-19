package com.urbanairship.richpush.sample.test;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;

public class RichPushSettingsTestCase extends RichPushSampleBaseTestCase {
    public void testPushEnable() throws UiObjectNotFoundException {
        goToPreferences();

        boolean isEnabled;
        UiObject pushEnabledCheckbox = getPreferenceCheckbox("PUSH_ENABLE");

        pushEnabledCheckbox.click();
        isEnabled = pushEnabledCheckbox.isChecked();
        this.getUiDevice().pressBack();
        goToPreferences();

        assertEquals(isEnabled, pushEnabledCheckbox.isChecked());

        pushEnabledCheckbox.click();
        isEnabled = pushEnabledCheckbox.isChecked();
        this.getUiDevice().pressBack();
        goToPreferences();

        assertEquals(isEnabled, pushEnabledCheckbox.isChecked());
    }
}
