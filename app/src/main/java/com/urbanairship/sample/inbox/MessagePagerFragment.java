/*
Copyright 2009-2015 Urban Airship Inc. All rights reserved.


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

package com.urbanairship.sample.inbox;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.sample.R;

import java.util.List;

/**
 * A fragment that displays messages in a view pager.
 */
public class MessagePagerFragment extends Fragment implements RichPushInbox.Listener {

    private final static String MESSAGE_ID = "CURRENT_MESSAGE_ID";

    private ViewPager messagePager;
    private List<RichPushMessage> messages;
    private RichPushInbox richPushInbox;
    private MessageFragmentAdapter adapter;
    private Listener listener;
    private String currentMessageId;

    /**
     * Creates a new instance of MessagePagerFragment.
     * @param messageId The initial message ID to view.
     * @return MessagePagerFragment instance.
     */
    public static MessagePagerFragment newInstance(String messageId) {
        Bundle args = new Bundle();
        args.putString(MESSAGE_ID, messageId);

        MessagePagerFragment fragment = new MessagePagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Listener for the message pager fragment.  Hosting activities must implement
     * the listener or a IllegalStateException will be thrown.
     */
    public interface Listener {
        void onMessageChanged(RichPushMessage message);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.richPushInbox = UAirship.shared().getRichPushManager().getRichPushInbox();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message_pager, container, false);
        this.messagePager = (ViewPager) view.findViewById(R.id.message_pager);
        this.adapter = new MessageFragmentAdapter(this.getChildFragmentManager());

        messagePager.setAdapter(adapter);

        this.messagePager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                RichPushMessage message = messages.get(position);
                if (message != null) {
                    setCurrentMessage(message.getMessageId());
                }
            }
        });

        if (savedInstanceState == null) {
            currentMessageId = getArguments().getString(MESSAGE_ID);
        } else {
            currentMessageId = savedInstanceState.getString(MESSAGE_ID);
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Activities using MessagePagerFragment must implement " +
                    "the MessagePagerFragment.Listener interface.");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(MESSAGE_ID, currentMessageId);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set latest messages
        updateRichPushMessages();

        // Listen for any rich push message changes
        richPushInbox.addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Remove listeners for message changes
        richPushInbox.removeListener(this);
    }
    /**
     * Grabs the latest messages from the rich push inbox, and syncs them
     * with the inbox fragment and message view pager if available
     */
    private void updateRichPushMessages() {
        this.messages = UAirship.shared().getRichPushManager().getRichPushInbox().getMessages();
        adapter.setRichPushMessages(messages);

        // Restore the position in the message list if the message still exists
        setCurrentMessage(currentMessageId);
    }

    @Override
    public void onUpdateInbox() {
        updateRichPushMessages();
    }

    /**
     * Sets the current message to view
     * @param messageId The message's ID to view
     */
    private void setCurrentMessage(String messageId) {
        RichPushMessage message = richPushInbox.getMessage(messageId);

        if (message == null) {
            return;
        }

        message.markRead();
        currentMessageId = message.getMessageId();
        listener.onMessageChanged(message);

        if (messagePager.getCurrentItem() != messages.indexOf(message)) {
            messagePager.setCurrentItem(messages.indexOf(message), false);
        }
    }
}
