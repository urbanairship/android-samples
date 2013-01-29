/*
 * Copyright 2012 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.SparseArray;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;

public abstract class InboxFragment extends SherlockListFragment implements
LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EMPTY_COLUMN_NAME = "";
    public static final String ROW_LAYOUT_ID_KEY = "row_layout_id";
    public static final String EMPTY_LIST_STRING_KEY = "empty_list_string";

    final int loaderId = 0x1;

    OnMessageListener listener;
    RichPushCursorAdapter adapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.setActivityAsListener(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.adapter = new RichPushCursorAdapter(this.getActivity(), this.getRowLayoutId(),
                this.createUIMapping());
        this.setListAdapter(this.adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setEmptyText(this.getString(this.getEmptyListStringId()));
        this.getLoaderManager().initLoader(this.loaderId, null, this);
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        this.setSelection(position);
        this.listener.onMessageSelected(RichPushManager.shared().getRichPushUser().getInbox()
                .getMessageAtPosition(position));
    }

    // actions

    public void refreshDisplay() {
        this.adapter.notifyDataSetChanged();
    }

    public void setViewBinder(RichPushCursorAdapter.ViewBinder binder) {
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

    @Override
    public RichPushCursorLoader onCreateLoader(int i, Bundle bundle) {
        return new RichPushCursorLoader(this.getActivity());
    }

    @Override
    public void onLoadFinished(@SuppressWarnings("rawtypes") Loader loader, Cursor cursor) {
        this.adapter.changeCursor(cursor);
    }

    // interfaces

    public interface OnMessageListener {
        void onMessageSelected(RichPushMessage message);
    }
}
