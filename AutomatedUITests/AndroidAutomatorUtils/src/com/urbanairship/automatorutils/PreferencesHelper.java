/*
 * Copyright 2013 Urban Airship
 */

package com.urbanairship.automatorutils;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;

/**
 * Helper class to test the preferences
 *
 */
public class PreferencesHelper {

    private static int KEYBOARD_WAIT_TIME = 3000;  // 3 seconds
    private static int UI_OBJECTS_WAIT_TIME = 2000;  // 2 seconds
    private static int SET_TEXT_WAIT_TIME = 3000;  // 3 seconds

    private UiSelector getPreferenceSummarySelector(String description) {
        return new UiSelector().description(description)
                .childSelector(new UiSelector()
                .className("android.widget.RelativeLayout")
                .childSelector(new UiSelector().index(1)));
    }

    private UiSelector getPreferenceTitleSelector(String description) {
        return new UiSelector().description(description)
                .childSelector(new UiSelector()
                .className("android.widget.RelativeLayout")
                .childSelector(new UiSelector().index(0)));
    }

    /**
     * Check specified preference view is enabled
     * @param setting The specified preference setting
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    public boolean isPreferenceViewEnabled(String setting) throws UiObjectNotFoundException, InterruptedException {
        UiObject preferenceView = new UiObject(new UiSelector().description(setting));
        scrollPreferenceIntoView(setting);
        return preferenceView.isEnabled();
    }

    /**
     * Set the specified preference setting
     * @param setting The specified preference to be set
     * @param enabled Boolean to enable or disable the specified setting
     * @throws Exception
     */
    public void setPreferenceCheckBoxEnabled(String setting, boolean enabled) throws Exception {
        // Scroll to the preference if its not visible in the list
        scrollPreferenceIntoView(setting);

        UiObject preference = new UiObject(new UiSelector().description(setting));
        AutomatorUtils.waitForUiObjectsToExist(UI_OBJECTS_WAIT_TIME, preference);
        UiObject preferenceCheckBox =  preference.getChild(new UiSelector().className(android.widget.CheckBox.class));
        AutomatorUtils.waitForUiObjectsToExist(UI_OBJECTS_WAIT_TIME, preferenceCheckBox);
        if (preferenceCheckBox.isChecked() != enabled) {
            preferenceCheckBox.click();
        }
    }

    /**
     * Toggle the checkbox for specified preference setting
     * @param setting The specified preference to select
     * @return <code>true</code> if checkbox is selected, otherwise <code>false</code>
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    public boolean getCheckBoxSetting(String setting) throws UiObjectNotFoundException, InterruptedException {
        scrollPreferenceIntoView(setting);

        UiObject settingCheckBox = new UiObject(new UiSelector().description(setting));

        settingCheckBox.click();
        return settingCheckBox.isChecked();
    }

    /**
     * Change the time preference value
     * @param setting The specified preference to change
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    public void changeTimePreferenceValue(String setting) throws UiObjectNotFoundException, InterruptedException {
        // Scroll to the preference if its not visible in the list
        scrollPreferenceIntoView(setting);

        UiObject timePicker = new UiObject(new UiSelector().description(setting));
        UiObject okButton = new UiObject(new UiSelector().className("android.widget.Button").text("OK"));

        timePicker.click();

        // Change the time
        for (int i = 0; i < 3; i++) {
            UiObject numberPicker = new UiObject(new UiSelector().className("android.widget.NumberPicker").index(i));
            UiObject button = numberPicker.getChild(new UiSelector().className("android.widget.Button"));
            button.click();
        }

        okButton.click();
    }

    /**
     * Get the preference summary
     * @param setting The specified preference
     * @return The string value of the preference
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    public String getPreferenceSummary(String setting) throws UiObjectNotFoundException, InterruptedException {
        String summaryString = "";
        // Scroll to the preference if its not visible in the list
        UiScrollable listView = new UiScrollable(new UiSelector().className("android.widget.ListView"));
        UiSelector summary = this.getPreferenceSummarySelector(setting);
        listView.scrollIntoView(summary);
        UiObject summaryText = new UiObject(summary);
        AutomatorUtils.waitForUiObjectsToExist(UI_OBJECTS_WAIT_TIME, summaryText);
        if (summaryText.exists()) {
            summaryString = summaryText.getText();
        }
        return summaryString;
    }

    /**
     * Set an alias
     * @param alias The string to set to
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    public void setAlias(String alias) throws UiObjectNotFoundException, InterruptedException {
        // Test set alias
        // Scroll to the preference if its not visible in the list
        scrollPreferenceIntoView("SET_ALIAS");

        UiObject setAlias = new UiObject(new UiSelector().description("SET_ALIAS"));
        UiObject aliasStringDisplayed = new UiObject(new UiSelector().text(alias));
        boolean aliasExist = false;
        if (aliasStringDisplayed.exists()) {
            aliasExist = true;
        }

        setAlias.click();
        UiObject aliasEditText = new UiObject(new UiSelector().text(alias));
        AutomatorUtils.waitForUiObjectsToExist(UI_OBJECTS_WAIT_TIME, aliasEditText);

        // Check if an alias already exist
        if (aliasExist) {
            aliasEditText.click();
            UiObject deleteAlias = new UiObject(new UiSelector().text("Delete"));
            if (deleteAlias.exists()) {
                // Alias exist, so clear it
                deleteAlias.click();
                UiObject okButton = new UiObject(new UiSelector().text("OK"));
                okButton.click();
                setAlias.click();
            }
        }

        UiObject setAliasText = new UiObject(new UiSelector().className("android.widget.EditText"));
        AutomatorUtils.waitForUiObjectsToExist(UI_OBJECTS_WAIT_TIME, setAliasText);
        setAliasText.click();

        // Wait for keyboard to pop up
        Thread.sleep(KEYBOARD_WAIT_TIME);

        // Set the alias
        AutomatorUtils.waitForUiObjectsToExist(SET_TEXT_WAIT_TIME, setAliasText);
        setAliasText.setText(alias);

        // save
        UiObject okButton = new UiObject(new UiSelector().text("OK"));
        okButton.click();
    }

    /**
     * Set a named user
     * @param namedUser The string to set to
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    public void setNamedUser(String namedUser) throws UiObjectNotFoundException, InterruptedException {
        // Test set named user
        // Scroll to the preference if its not visible in the list
        scrollPreferenceIntoView("SET_NAMED_USER");

        UiObject setNamedUser = new UiObject(new UiSelector().description("SET_NAMED_USER"));
        UiObject namedUserStringDisplayed = new UiObject(new UiSelector().text(namedUser));
        boolean namedUserExist = false;
        if (namedUserStringDisplayed.exists()) {
            namedUserExist = true;
        }

        setNamedUser.click();
        UiObject namedUserEditText = new UiObject(new UiSelector().text(namedUser));
        AutomatorUtils.waitForUiObjectsToExist(UI_OBJECTS_WAIT_TIME, namedUserEditText);

        // Check if a named user already exist
        if (namedUserExist) {
            namedUserEditText.click();
            UiObject deleteNamedUser = new UiObject(new UiSelector().text("Delete"));
            if (deleteNamedUser.exists()) {
                // Named user exist, so clear it
                deleteNamedUser.click();
                UiObject okButton = new UiObject(new UiSelector().text("OK"));
                okButton.click();
                setNamedUser.click();
            }
        }

        UiObject setNamedUserText = new UiObject(new UiSelector().className("android.widget.EditText"));
        AutomatorUtils.waitForUiObjectsToExist(UI_OBJECTS_WAIT_TIME, setNamedUserText);
        setNamedUserText.click();

        // Wait for keyboard to pop up
        Thread.sleep(KEYBOARD_WAIT_TIME);

        // Set the named user
        AutomatorUtils.waitForUiObjectsToExist(SET_TEXT_WAIT_TIME, setNamedUserText);
        setNamedUserText.setText(namedUser);

        // save
        UiObject okButton = new UiObject(new UiSelector().text("OK"));
        okButton.click();
    }

    /**
     * Add a tag
     * @param tags The string to add
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    public void addTags(String tags) throws UiObjectNotFoundException, InterruptedException {
        // Scroll to the preference if its not visible in the list
        scrollPreferenceIntoView("ADD_TAGS");

        UiObject okButton = new UiObject(new UiSelector().text("OK"));
        UiObject addTags = new UiObject(new UiSelector().description("ADD_TAGS"));
        addTags.click();

        // Check if a tag already exist
        UiObject tagsListView = new UiObject(new UiSelector().className("android.widget.ListView"));
        AutomatorUtils.waitForUiObjectsToExist(UI_OBJECTS_WAIT_TIME, tagsListView);

        if (tagsListView.exists()) {
            UiObject tagLinearLayout = tagsListView.getChild(new UiSelector().className("android.widget.LinearLayout"));
            UiObject tagDeleteButton = tagLinearLayout.getChild(new UiSelector().className("android.widget.ImageButton"));
            tagDeleteButton.click();
            okButton = new UiObject(new UiSelector().text("OK"));
            okButton.click();
            addTags.click();
        }

        // Add tag
        UiObject addTagsText = new UiObject(new UiSelector().className("android.widget.EditText"));

        // Add first tag
        addTagsText.click();

        // Wait for keyboard to pop up
        Thread.sleep(KEYBOARD_WAIT_TIME);

        addTagsText.setText(tags);
        UiObject addTagButton = new UiObject(new UiSelector().className("android.widget.ImageButton"));
        addTagButton.click();

        // Save first tag
        okButton = new UiObject(new UiSelector().text("OK"));
        okButton.click();
    }

    /**
     * Scrolls to the preference setting's title in the UI view
     * @param setting The specified preference setting
     * @throws UiObjectNotFoundException
     * @throws InterruptedException
     */
    private void scrollPreferenceIntoView(String setting) throws UiObjectNotFoundException, InterruptedException {
        UiScrollable listView = new UiScrollable(new UiSelector().className("android.widget.ListView"));
        AutomatorUtils.waitForUiObjectsToExist(UI_OBJECTS_WAIT_TIME, listView);
        listView.scrollIntoView(getPreferenceTitleSelector(setting));
    }
}
