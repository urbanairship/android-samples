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
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.richpush.sample.R;
import com.urbanairship.richpush.sample.view.CustomViewPager;

import java.util.List;

/**
 * A fragment that displays messages in a view pager.
 */
public class MessagePagerFragment extends Fragment implements RichPushInbox.Listener {

    private final static String CURRENT_POSITION = "CURRENT_POSITION";

    private CustomViewPager messagePager;
    private List<RichPushMessage> messages;
    private RichPushInbox richPushInbox;
    private MessageFragmentAdapter adapter;
    private Listener listener;

    /**
     * Listener for the message pager fragment.  Hosting activities must implement
     * the listener or a IllegalStateException will be thrown.
     */
    public interface Listener {
        public void onMessageChanged(int position, RichPushMessage message);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.richPushInbox = RichPushManager.shared().getRichPushUser().getInbox();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_pager_fragment, container, false);
        this.messagePager = (CustomViewPager) view.findViewById(R.id.message_pager);
        this.adapter = new MessageFragmentAdapter(this.getFragmentManager());

        messagePager.setAdapter(adapter);
        updateRichPushMessages();

        this.messagePager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                messageChanged(position);
            }
        });

        // Restore the last position if available
        int position = savedInstanceState == null ? 0 : savedInstanceState.getInt(CURRENT_POSITION, 0);
        messagePager.setCurrentItem(position, false);

        messageChanged(position);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.listener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Activities using AbstractInboxFragment must implement " +
                    "the AbstractInboxFragment.OnMessageListener interface.");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_POSITION, messagePager.getCurrentItem());
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
        this.messages = RichPushManager.shared().getRichPushUser().getInbox().getMessages();
        adapter.setRichPushMessages(messages);
    }

    @Override
    public void onUpdateInbox() {
        updateRichPushMessages();
    }

    /**
     * Enables or disables view pager swiping to change messages.
     * @param enable <code>true</code> to enable paging, <code>false</code> to
     * disable paging.
     */
    public void enablePaging(boolean enable) {
        messagePager.enableTouchEvents(enable);
    }

    /**
     * Sets the current message to view
     * @param message The message to view
     */
    public void setCurrentMessage(RichPushMessage message) {
        int position = messages.indexOf(message);
        if (position != -1) {
            messagePager.setCurrentItem(position, false);
        }
    }

    /**
     * Called when the view pager changes messages.
     *
     * @param position The new message position.
     */
    private void messageChanged(int position) {
        RichPushMessage message = messages.get(position);
        if (message != null) {
            message.markRead();
            listener.onMessageChanged(position, message);
        }
    }
}
