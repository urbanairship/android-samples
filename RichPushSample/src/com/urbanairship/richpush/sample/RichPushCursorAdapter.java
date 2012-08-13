package com.urbanairship.richpush.sample;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.urbanairship.Logger;
import com.urbanairship.UrbanAirshipProvider;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;

public class RichPushCursorAdapter extends CursorAdapter {

	public static String NEWEST_FIRST_ORDER = UrbanAirshipProvider.COLUMN_NAME_KEY + " DESC";

	int layout;
	int messageIdCol = -1;
	LayoutInflater inflater;
	SparseArray<String> mapping;
	ViewBinder binder;

	public RichPushCursorAdapter(Context context, int layout, SparseArray<String> mapping) {
		super(context, null, true);
		this.layout = layout;
		this.mapping = mapping;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		return this.inflater.inflate(this.layout, viewGroup, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
        String messageId = cursor.getString(this.getMessageIdColumnId(cursor));
        Logger.debug("Getting message " + messageId);
        RichPushMessage message = RichPushManager.shared().getRichPushUser().getInbox()
                .getMessage(messageId);
		int count = this.mapping.size();
		for (int i = 0; i < count; i++) {
			int key = this.mapping.keyAt(i);
			View toView = view.findViewById(key);
			if (toView != null) {
				this.binder.setViewValue(toView, message, this.mapping.get(key));
			}
		}
	}

	// actions

	public void setViewBinder(ViewBinder binder) {
		this.binder = binder;
	}

	// helpers

	private int getMessageIdColumnId(Cursor c) {
		if (this.messageIdCol == -1) {
			this.messageIdCol = c.getColumnIndex(UrbanAirshipProvider.RichPush.COLUMN_NAME_MESSAGE_ID);
		}
		return this.messageIdCol;
	}

    // interfaces

	public static interface ViewBinder {
		void setViewValue(View view, RichPushMessage message, String columnName);
	}

}