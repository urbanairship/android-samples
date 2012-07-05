package com.urbanairship.richpush.sample;

import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.util.SparseArray;
import com.urbanairship.UrbanAirshipProvider;

public class RichPushSampleInboxFragment extends InboxFragment{

	public static RichPushSampleInboxFragment newInstance() {
		return new RichPushSampleInboxFragment();
	}

	@Override
	public SparseArray<String> createUIMapping() {
		SparseArray<String> mapping = new SparseArray<String>();
		mapping.put(R.id.unread_indicator, UrbanAirshipProvider.RichPush.COLUMN_NAME_UNREAD);
		mapping.put(R.id.title, UrbanAirshipProvider.RichPush.COLUMN_NAME_TITLE);
		mapping.put(R.id.message, UrbanAirshipProvider.RichPush.COLUMN_NAME_MESSAGE);
		mapping.put(R.id.date_sent, UrbanAirshipProvider.COLUMN_NAME_TIMESTAMP);
		mapping.put(R.id.message_checkbox, EMPTY_COLUMN_NAME);
		return mapping;
	}

	@Override
	public CursorLoader onCreateLoader(int id, Bundle bundle) {
		return new CursorLoader(this.getActivity(), UrbanAirshipProvider.RICHPUSH_CONTENT_URI,
				null, UrbanAirshipProvider.RichPush.COLUMN_NAME_DELETED + " != ?",
				new String[] {"1"}, RichPushCursorAdapter.NEWEST_FIRST_ORDER);
	}
}
