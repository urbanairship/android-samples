/*
 * Copyright 2014 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.urbanairship.push.PushManager;
import com.urbanairship.util.UAStringUtil;

/**
 * DialogPreference to set the alias
 *
 */
public class SetAliasPreference extends DialogPreference {

    private EditText editTextView;
    private String currentAlias;

    public SetAliasPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        currentAlias = PushManager.shared().getPreferences().getAlias();
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
        view.setContentDescription("SET_ALIAS");
        return view;
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String alias = editTextView.getText().toString();
            if (callChangeListener(alias)) {
                setAlias(alias);
                notifyChanged();
            }
        }
    }

    private void setAlias(String alias) {
        currentAlias = UAStringUtil.isEmpty(alias) ? null : alias;
        PushManager.shared().setAlias(currentAlias);
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
