/*
 * Copyright 2014 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.inbox;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.urbanairship.Logger;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.widget.RichPushMessageView;

/**
 * Fragment that displays a rich push message in a RichPushMessageView
 */
public class MessageFragment extends Fragment {

    private static final String MESSAGE_ID_KEY = "com.urbanairship.richpush.URL_KEY";
    private RichPushMessageView browser;

    /**
     * Creates a new MessageFragment
     * @param messageId The message's id to display
     * @return messageFragment new MessageFragment
     */
    public static MessageFragment newInstance(String messageId) {
        MessageFragment message = new MessageFragment();
        Bundle arguments = new Bundle();
        arguments.putString(MESSAGE_ID_KEY, messageId);
        message.setArguments(arguments);
        return message;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        browser = new RichPushMessageView(container.getContext());
        browser.setLayoutParams(container.getLayoutParams());
        return browser;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String messageId = getArguments().getString(MESSAGE_ID_KEY);
        RichPushMessage message = RichPushManager.shared().getRichPushUser().getInbox().getMessage(messageId);

        if (message != null) {
            browser.loadRichPushMessage(message);
        } else {
            Logger.info("Couldn't retrieve message for ID: " + messageId);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT >= 11) {
            browser.onPause();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 11) {
            browser.onResume();
        }
    }
}
