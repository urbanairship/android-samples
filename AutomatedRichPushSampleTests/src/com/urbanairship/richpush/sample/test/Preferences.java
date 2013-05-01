package com.urbanairship.richpush.sample.test;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;

public class Preferences {
    private UiSelector getPreferenceSummarySelector(String description) {
        return new UiSelector().description(description)
                .childSelector(new UiSelector()
                .className("android.widget.RelativeLayout")
                .childSelector(new UiSelector().index(1)));
    }

    /**
     * Check specified preference view is enabled
     * @param setting The specified preference setting
     * @throws UiObjectNotFoundException
     */
    public boolean isPreferenceViewEnabled(String setting) throws UiObjectNotFoundException {
        UiObject preferenceView = new UiObject(new UiSelector().description(setting));
        UiScrollable listView = new UiScrollable(new UiSelector().className("android.widget.ListView"));
        listView.scrollDescriptionIntoView(setting);
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
        UiScrollable listView = new UiScrollable(new UiSelector().className("android.widget.ListView"));
        listView.scrollDescriptionIntoView(setting);

        UiObject preference = new UiObject(new UiSelector().description(setting));
        UiObject preferenceCheckBox =  preference.getChild(new UiSelector().className(android.widget.CheckBox.class));

        if (preferenceCheckBox.isChecked() != enabled) {
            preferenceCheckBox.click();
        }
    }


    public boolean getCheckBoxSetting(String setting) throws UiObjectNotFoundException {
        UiScrollable listView = new UiScrollable(new UiSelector().className("android.widget.ListView"));
        listView.scrollDescriptionIntoView(setting);

        UiObject settingCheckBox = new UiObject(new UiSelector().description(setting));

        settingCheckBox.click();
        return settingCheckBox.isChecked();
    }


    public void changeTimePreferenceValue(String setting) throws UiObjectNotFoundException {
        // Scroll to the preference if its not visible in the list
        UiScrollable listView = new UiScrollable(new UiSelector().className("android.widget.ListView"));
        listView.scrollDescriptionIntoView(setting);

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

    public String getPreferenceSummary(String setting) throws UiObjectNotFoundException {
        // Scroll to the preference if its not visible in the list
        UiScrollable listView = new UiScrollable(new UiSelector().className("android.widget.ListView"));
        UiSelector summary = this.getPreferenceSummarySelector(setting);
        listView.scrollIntoView(summary);

        return new UiObject(summary).getText();
    }

    public void setAlias(String alias) throws UiObjectNotFoundException {
        // Test set alias
        // Scroll to the preference if its not visible in the list
        UiScrollable listView = new UiScrollable(new UiSelector().className("android.widget.ListView"));
        listView.scrollDescriptionIntoView("SET_ALIAS");

        UiObject setAlias = new UiObject(new UiSelector().description("SET_ALIAS"));
        UiObject aliasStringDisplayed = new UiObject(new UiSelector().text(alias));
        boolean aliasExist = false;
        if (aliasStringDisplayed.exists()) {
            aliasExist = true;
        }

        setAlias.click();

        // Check if an alias already exist
        if (aliasExist) {
            UiObject aliasEditText = new UiObject(new UiSelector().text(alias));
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
        setAliasText.click();

        // Set the alias
        setAliasText.setText(alias);

        // save
        UiObject okButton = new UiObject(new UiSelector().text("OK"));
        okButton.click();
    }

    public void setTags(String tags) throws UiObjectNotFoundException {
        // Scroll to the preference if its not visible in the list
        UiScrollable listView = new UiScrollable(new UiSelector().className("android.widget.ListView"));
        listView.scrollDescriptionIntoView("SET_TAGS");
        UiObject okButton = new UiObject(new UiSelector().text("OK"));
        UiObject setTags = new UiObject(new UiSelector().description("SET_TAGS"));
        setTags.click();

        // Check if a tag already exist
        UiObject tagsListView = new UiObject(new UiSelector().className("android.widget.ListView"));
        if (tagsListView.exists()) {
            UiObject tagLinearLayout = tagsListView.getChild(new UiSelector().className("android.widget.LinearLayout"));
            UiObject tagDeleteButton = tagLinearLayout.getChild(new UiSelector().className("android.widget.ImageButton"));
            tagDeleteButton.click();
            okButton = new UiObject(new UiSelector().text("OK"));
            okButton.click();
            setTags.click();
        }

        // Set tag
        UiObject setTagsText = new UiObject(new UiSelector().className("android.widget.EditText"));

        // Add first tag
        setTagsText.click();
        setTagsText.setText(tags);
        UiObject addTagButton = new UiObject(new UiSelector().className("android.widget.ImageButton"));
        addTagButton.click();

        // Save first tag
        okButton = new UiObject(new UiSelector().text("OK"));
        okButton.click();
    }
}

