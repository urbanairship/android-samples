/*
 * Copyright 2012 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.urbanairship.UrbanAirshipProvider;
import com.urbanairship.richpush.RichPushMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public abstract class InboxFragment extends SherlockListFragment {
    private static final SimpleDateFormat UA_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static final String EMPTY_COLUMN_NAME = "";
    public static final String ROW_LAYOUT_ID_KEY = "row_layout_id";
    public static final String EMPTY_LIST_STRING_KEY = "empty_list_string";

    private OnMessageListener listener;
    private RichPushMessageAdapter adapter;
    private List<String> checkedIds = new ArrayList<String>();


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.setActivityAsListener(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.adapter = new RichPushMessageAdapter(getActivity(), getRowLayoutId(), createUIMapping());
        adapter.setViewBinder(new MessageBinder());
        setRetainInstance(true);
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

    public List<String> getSelectedMessages() {
        return checkedIds;
    }

    public void clearSelection() {
        checkedIds.clear();
        adapter.notifyDataSetChanged();
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
        void onSelectionChanged();
    }

    // inner class
    class MessageBinder implements RichPushMessageAdapter.ViewBinder {
        @Override
        public void setViewValue(View view, RichPushMessage message, String columnName) {
            if (columnName.equals(UrbanAirshipProvider.RichPush.COLUMN_NAME_UNREAD)) {
                view.setBackgroundColor(message.isRead() ? Color.BLACK : Color.YELLOW);
            } else if (columnName.equals(UrbanAirshipProvider.RichPush.COLUMN_NAME_TITLE)) {
                ((TextView) view).setText(message.getTitle());
            } else if (columnName.equals(UrbanAirshipProvider.COLUMN_NAME_TIMESTAMP)) {
                ((TextView) view).setText(UA_DATE_FORMATTER.format(message.getSentDate()));
            } else {
                view.setTag(message.getMessageId());
                if (checkedIds.contains(message.getMessageId())) {
                    ((CheckBox)view).setChecked(true);
                } else {
                    ((CheckBox)view).setChecked(false);
                }

                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String messageId = (String) view.getTag();
                        if (((CheckBox)view).isChecked()) {
                            checkedIds.add(messageId);
                        } else {
                            checkedIds.remove(messageId);
                        }
                        listener.onSelectionChanged();
                    }
                });
            }
            view.setFocusable(false);
            view.setFocusableInTouchMode(false);
        }
    }
}
