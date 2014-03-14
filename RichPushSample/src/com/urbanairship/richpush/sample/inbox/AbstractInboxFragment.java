/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.inbox;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.richpush.sample.ViewBinderArrayAdapter;
import com.urbanairship.richpush.sample.widget.RichPushWidgetUtils;
import com.urbanairship.util.Toaster;

import java.util.ArrayList;
import java.util.List;

/**
 * A list fragment that shows rich push messages.  Activities that attach
 * this fragment must implement AbstractInboxFragment.OnMessageListener.
 */
public abstract class AbstractInboxFragment extends ListFragment
        implements RichPushManager.Listener,
        RichPushInbox.Listener,
        ViewBinderArrayAdapter.ViewBinder<RichPushMessage> {


    private Listener listener;
    protected ViewBinderArrayAdapter<RichPushMessage> adapter;
    private List<String> selectedMessageIds = new ArrayList<String>();
    private List<RichPushMessage> messages;
    private RichPushInbox richPushInbox;
    private boolean isManualRefreshing = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.listener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Activities using AbstractInboxFragment must implement " +
                    "the AbstractInboxFragment.Listener interface.");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set latest messages
        updateRichPushMessages();

        // Listen for any rich push message changes
        RichPushManager.shared().addListener(this);
        richPushInbox.addListener(this);

        // Refresh the widget inbox if we have one
        RichPushWidgetUtils.refreshWidget(this.getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();

        // Remove listeners for message changes
        RichPushManager.shared().removeListener(this);
        richPushInbox.removeListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the InboxMessageAdapter
        this.adapter = new ViewBinderArrayAdapter<RichPushMessage>(getActivity(), getRowLayoutId(), this);
        this.setListAdapter(adapter);
        setRetainInstance(true);

        this.richPushInbox = RichPushManager.shared().getRichPushUser().getInbox();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setEmptyText(getString(getEmptyListStringId()));
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        this.listener.onMessageOpen((RichPushMessage) adapter.getItem(position));
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
    public void onUpdateMessages(boolean success) {
        if (isManualRefreshing && !success) {
            Toaster.longerToast("Failed to update messages!");
        }
        isManualRefreshing = false;
        setListShown(true);
    }

    @Override
    public void onUpdateUser(boolean success) {
        // no-op
    }

    @Override
    public void onUpdateInbox() {
        updateRichPushMessages();
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
        this.setListShown(false);
        RichPushManager.shared().refreshMessages();
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
