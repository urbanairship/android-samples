/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.util.SparseArray;

import com.urbanairship.UrbanAirshipProvider;

/**
 * Sample implementation of the InboxFragment
 *
 */
public class RichPushSampleInboxFragment extends InboxFragment {

    @Override
    public int getRowLayoutId() {
        return R.layout.inbox_message;
    }

    @Override
    public int getEmptyListStringId() {
        return R.string.no_messages;
    }

    @Override
    public SparseArray<String> createUIMapping() {
        SparseArray<String> mapping = new SparseArray<String>();
        mapping.put(R.id.unread_indicator, UrbanAirshipProvider.RichPush.COLUMN_NAME_UNREAD);
        mapping.put(R.id.title, UrbanAirshipProvider.RichPush.COLUMN_NAME_TITLE);
        mapping.put(R.id.date_sent, UrbanAirshipProvider.COLUMN_NAME_TIMESTAMP);
        mapping.put(R.id.message_checkbox, EMPTY_COLUMN_NAME);
        return mapping;
    }
}
