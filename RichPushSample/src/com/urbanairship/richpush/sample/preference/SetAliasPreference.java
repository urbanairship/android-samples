/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * DialogPreference to set the quiet time start
 *
 */
public class SetAliasPreference extends DialogPreference implements UAPreference {

    private EditText editTextView;
    private String currentAlias;

    public SetAliasPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public PreferenceType getPreferenceType() {
        return PreferenceType.SET_ALIAS;
    }

    @Override
    public void setValue(Object value) {
        currentAlias = (String) value;
        notifyChanged();
    }

    @Override
    protected View onCreateDialogView() {
        editTextView = new EditText(getContext());
        editTextView.setText(currentAlias);

        return editTextView;
    }

    @Override
    public View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        view.setContentDescription(getPreferenceType().toString());
        return view;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String alias = editTextView.getText().toString();
            if (callChangeListener(alias)) {
                currentAlias = alias;
                notifyChanged();
            }
        }
    }

    @Override
    public String getSummary() {
        return currentAlias;
    }

    @Override
    protected boolean shouldPersist() {
        return false;
    }
}
