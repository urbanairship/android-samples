/*
 * Copyright 2012 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.urbanairship.richpush.RichPushMessage;

import java.util.List;

public abstract class InboxFragment extends SherlockListFragment {
    public static final String EMPTY_COLUMN_NAME = "";
    public static final String ROW_LAYOUT_ID_KEY = "row_layout_id";
    public static final String EMPTY_LIST_STRING_KEY = "empty_list_string";

    OnMessageListener listener;
    RichPushMessageAdapter adapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.setActivityAsListener(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.adapter = new RichPushMessageAdapter(getActivity(), getRowLayoutId(), createUIMapping());
        this.setListAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setEmptyText(getString(getEmptyListStringId()));
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        this.listener.onMessageSelected(this.adapter.getItem(position));
    }

    // actions

    public void setMessages(List<RichPushMessage> messages) {
        adapter.setMessages(messages);
    }

    public void setViewBinder(RichPushMessageAdapter.ViewBinder binder) {
        this.adapter.setViewBinder(binder);
    }

    public abstract SparseArray<String> createUIMapping();

    // helpers

    private int getRowLayoutId() {
        return this.getArguments() != null && this.getArguments().containsKey(ROW_LAYOUT_ID_KEY) ?
                this.getArguments().getInt(ROW_LAYOUT_ID_KEY) : R.layout.inbox_message;
    }

    private int getEmptyListStringId() {
        return this.getArguments() != null && this.getArguments().containsKey(EMPTY_LIST_STRING_KEY) ?
                this.getArguments().getInt(EMPTY_LIST_STRING_KEY) : R.string.no_messages;
    }

    private void setActivityAsListener(Activity activity) {
        try {
            this.listener = (OnMessageListener) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Activities using InboxFragment must implement " +
                    "the InboxFragment.OnMessageListener interface.");
        }
    }

    // interfaces
    public interface OnMessageListener {
        void onMessageSelected(RichPushMessage message);
    }
}
