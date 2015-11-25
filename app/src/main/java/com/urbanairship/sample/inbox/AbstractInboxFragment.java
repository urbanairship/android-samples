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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.urbanairship.Cancelable;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * A list fragment that shows rich push messages. Activities that attach
 * this fragment must implement AbstractInboxFragment.OnMessageListener.
 */
public abstract class AbstractInboxFragment extends ListFragment
        implements RichPushInbox.Listener,
                   ViewBinderArrayAdapter.ViewBinder<RichPushMessage> {


    protected ViewBinderArrayAdapter<RichPushMessage> adapter;
    private final List<String> selectedMessageIds = new ArrayList<>();
    private List<RichPushMessage> messages;
    private RichPushInbox richPushInbox;
    private boolean isManualRefreshing = false;
    private Cancelable fetchMessagesOperation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the InboxMessageAdapter
        this.adapter = new ViewBinderArrayAdapter<>(getActivity(), getRowLayoutId(), this);
        this.setListAdapter(adapter);
        setRetainInstance(true);

        this.richPushInbox = UAirship.shared().getInbox();
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

        if (fetchMessagesOperation != null) {
            fetchMessagesOperation.cancel();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        RichPushMessage message = (RichPushMessage) adapter.getItem(position);

        // Use the com.urbanairship.VIEW_RICH_PUSH_MESSAGE that is also used by the open_mc_action
        Intent intent = new Intent(RichPushInbox.VIEW_MESSAGE_INTENT_ACTION)
                .setData(Uri.fromParts(RichPushInbox.MESSAGE_DATA_SCHEME, message.getMessageId(), null))
                .setPackage(getContext().getPackageName());

        this.startActivity(intent);
    }

    /**
     * @return The list of ids of the selected messages
     */
    public List<String> getSelectedMessages() {
        return selectedMessageIds;
    }

    /**
     * Clears the selected messages
     */
    public void clearSelection() {
        selectedMessageIds.clear();
        adapter.notifyDataSetChanged();
    }

    /**
     * Selects all the messages in the inbox
     */
    public void selectAll() {
        selectedMessageIds.clear();
        for (RichPushMessage message : messages) {
            selectedMessageIds.add(message.getMessageId());
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * @return The layout id to use in the InboxMessageAdapter
     */
    public abstract int getRowLayoutId();

    /**
     * @return The string id of the message to display when no messages are available
     */
    public abstract int getEmptyListStringId();

    @Override
    public void onInboxUpdated() {
        updateRichPushMessages();

    }

    protected void onManualRefreshFinished(boolean successful) {
        if (isManualRefreshing && !successful) {
            Toast.makeText(getActivity(), "Failed to update messages!", Toast.LENGTH_LONG).show();
        }
        isManualRefreshing = false;
    }

    /**
     * Grabs the latest messages from the rich push inbox, and syncs them
     * with the inbox fragment and message view pager if available
     */
    private void updateRichPushMessages() {
        this.messages = richPushInbox.getMessages();
        adapter.set(messages);
    }

    public void refreshMessages() {
        this.isManualRefreshing = true;
        if (fetchMessagesOperation != null) {
            fetchMessagesOperation.cancel();
        }

        fetchMessagesOperation = richPushInbox.fetchMessages(new RichPushInbox.FetchMessagesCallback() {
            @Override
            public void onFinished(boolean success) {
                onManualRefreshFinished(success);
            }
        });
    }

    /**
     * Listens for message opens
     */
    public interface Listener {
        void onMessageOpen(RichPushMessage message);
    }

    /**
     * Sets a message is selected or not
     *
     * @param messageId The id of the message
     * @param isChecked Boolean indicating if the message is selected or not
     */
    protected void onMessageSelected(String messageId, boolean isChecked) {
        if (isChecked && !selectedMessageIds.contains(messageId)) {
            selectedMessageIds.add(messageId);
        } else if (!isChecked && selectedMessageIds.contains(messageId)) {
            selectedMessageIds.remove(messageId);
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * Returns if a message is selected
     *
     * @param messageId The id of the message
     * @return <code>true</code> If the message is selected, <code>false</code> otherwise.
     */
    protected boolean isMessageSelected(String messageId) {
        return selectedMessageIds.contains(messageId);
    }

    /**
     * Gets the rich push inbox
     *
     * @return The rich push inbox
     */
    public RichPushInbox getRichPushInbox() {
        return richPushInbox;
    }

    /**
     * Gets the current rich push messages
     *
     * @return A list of rich push messages
     */
    public List<RichPushMessage> getMessages() {
        return messages;
    }
}
