package com.urbanairship.richpush.sample;

import android.util.SparseArray;
import com.urbanairship.UrbanAirshipProvider;

public class RichPushSampleInboxFragment extends InboxFragment {

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

}
