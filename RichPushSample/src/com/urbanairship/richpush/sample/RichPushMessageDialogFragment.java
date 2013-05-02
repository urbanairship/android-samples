/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;

/**
 * Dialog Fragment that displays a rich push message
 *
 */
public class RichPushMessageDialogFragment extends DialogFragment {
    private static final String MESSAGE_ID_KEY = "com.urbanairship.richpush.sample.FIRST_MESSAGE_ID";

    /**
     * Creates a new instance of RichPushMessageDialogFragment
     * @param messageId The id of the message to display
     * @return RichPushMessageDialogFragment
     */
    public static RichPushMessageDialogFragment newInstance(String messageId) {
        RichPushMessageDialogFragment fragment = new RichPushMessageDialogFragment();

        Bundle args = new Bundle();
        args.putString(MESSAGE_ID_KEY, messageId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String messageId = getArguments().getString(MESSAGE_ID_KEY);
        RichPushMessage message = RichPushManager.shared().getRichPushUser().getInbox().getMessage(messageId);

        if (message == null) {
            return null;
        }

        View view = inflater.inflate(R.layout.message_dialog, container, true);

        RichPushMessageView messageView = (RichPushMessageView) view.findViewById(R.id.message_browser);
        messageView.loadRichPushMessage(message);
        message.markRead();

        getDialog().setTitle(R.string.rich_push_message_dialog_title);

        return view;
    }
}
