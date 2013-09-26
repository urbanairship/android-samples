/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.inbox;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.urbanairship.richpush.RichPushMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * A list fragment that shows rich push messages.
 *
 */
public abstract class InboxFragment extends SherlockListFragment {
    public static final String EMPTY_COLUMN_NAME = "";
    public static final String ROW_LAYOUT_ID_KEY = "row_layout_id";
    public static final String EMPTY_LIST_STRING_KEY = "empty_list_string";

    private OnMessageListener listener;
    private RichPushMessageAdapter adapter;
    private List<String> selectedMessageIds = new ArrayList<String>();
    private List<RichPushMessage> messages;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.setActivityAsListener(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the RichPushMessageAdapter
        this.adapter = new RichPushMessageAdapter(getActivity(), getRowLayoutId());
        adapter.setViewBinder(createMessageBinder());
        this.setListAdapter(adapter);

        // Retain the instance so we keep list position and selection on activity re-creation
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setEmptyText(getString(getEmptyListStringId()));
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        this.listener.onMessageOpen(this.adapter.getItem(position));
    }

    /**
     * Sets the rich push messages to display
     * @param messages Current list of rich push messages
     */
    public void setMessages(List<RichPushMessage> messages) {
        this.messages = messages;
        adapter.setMessages(messages);
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
        listener.onSelectionChanged();
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
        listener.onSelectionChanged();
    }

    /**
     * @return The layout id to use in the RichPushMessageAdapter
     */
    public abstract int getRowLayoutId();

    /**
     * @return The string id of the message to display when no messages are available
     */
    public abstract int getEmptyListStringId();

    /**
     * Tries to set the activity as an OnMessageListener
     * @param activity The specified activity
     */
    private void setActivityAsListener(Activity activity) {
        try {
            this.listener = (OnMessageListener) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Activities using InboxFragment must implement " +
                    "the InboxFragment.OnMessageListener interface.");
        }
    }

    /**
     * Listens for message selection and selection changes
     *
     */
    public interface OnMessageListener {
        void onMessageOpen(RichPushMessage message);
        void onSelectionChanged();
    }


    /**
     * Sets a message is selected or not
     * @param messageId The id of the message
     * @param isChecked Boolean indicating if the message is selected or not
     */
    protected void onMessageSelected(String messageId, boolean isChecked) {
        if (isChecked && !selectedMessageIds.contains(messageId)) {
            selectedMessageIds.add(messageId);
        } else if (!isChecked && selectedMessageIds.contains(messageId)) {
            selectedMessageIds.remove(messageId);
        }

        listener.onSelectionChanged();
    }

    /**
     * Returns if a message is selected
     * @param messageId The id of the message
     * @return <code>true</code> If the message is selected, <code>false</code> otherwise.
     */
    protected boolean isMessageSelected(String messageId) {
        return selectedMessageIds.contains(messageId);
    }

    /**
     * @return RichPushMessageAdapter.ViewBinder to bind messages to a list view item
     * in the list adapter.
     */
    protected abstract RichPushMessageAdapter.ViewBinder createMessageBinder();




}
