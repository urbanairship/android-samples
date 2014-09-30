/*
Copyright 2009-2014 Urban Airship Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.urbanairship.richpush.sample.inbox;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.richpush.sample.R;
import com.urbanairship.widget.RichPushMessageWebView;

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
        RichPushMessage message = UAirship.shared().getRichPushManager().getRichPushInbox().getMessage(messageId);

        if (message == null) {
            return null;
        }


        View view = inflater.inflate(R.layout.message_dialog, container, true);

        RichPushMessageWebView messageView = (RichPushMessageWebView) view.findViewById(R.id.message_browser);
        messageView.loadRichPushMessage(message);
        message.markRead();

        getDialog().setTitle(R.string.rich_push_message_dialog_title);

        return view;
    }
}
