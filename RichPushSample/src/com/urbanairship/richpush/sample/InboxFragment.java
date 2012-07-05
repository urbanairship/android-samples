package com.urbanairship.richpush.sample;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.SparseArray;
import android.view.View;
import android.widget.ListView;

import com.urbanairship.UrbanAirshipProvider;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;

public abstract class InboxFragment extends ListFragment
		implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final String EMPTY_COLUMN_NAME = "";

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
        this.setRetainInstance(true);
		if (this.adapter == null) {
			this.adapter = new RichPushCursorAdapter(this.getActivity(), R.layout.inbox_message,
					this.createUIMapping());
		}
		this.setListAdapter(this.adapter);
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.setEmptyText(this.getString(R.string.no_messages));
		this.setListShown(false);
		this.getLoaderManager().initLoader(this.loaderId, null, this);
	}

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
		this.setSelection(position);
		RichPushMessage message = RichPushManager.shared().getInbox().getMessage(
				this.convertCursorIdToMessageId(id));
        this.listener.onMessageSelected(message);
    }

	// actions

	public void setViewBinder(RichPushCursorAdapter.ViewBinder binder) {
		this.adapter.setViewBinder(binder);
	}

	public abstract SparseArray<String> createUIMapping();

	// helpers

	private long convertMessageIdToCursorId(String messageId) {
		return Long.valueOf(messageId.replace(RichPushManager.PUSH_ID_SUFFIX, ""));
	}

	private String convertCursorIdToMessageId(long cursorId) {
		return String.valueOf(cursorId) + RichPushManager.PUSH_ID_SUFFIX;
	}

    private void setActivityAsListener(Activity activity) {
		try {
			this.listener = (OnMessageListener) activity;
		} catch (ClassCastException e) {
			throw new IllegalStateException("Activities using " + this.getClass().getName() + " must implement" +
					"the " + this.getClass().getName() + ".OnMessageListener interface.");
		}
    }

	@Override
	public CursorLoader onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(this.getActivity(), UrbanAirshipProvider.RICHPUSH_CONTENT_URI,
				null, null, null, RichPushCursorAdapter.NEWEST_FIRST_ORDER);
	}

	@Override
	public void onLoadFinished(Loader loader, Cursor cursor) {
		this.adapter.swapCursor(cursor);

		if (this.isResumed()) {
			this.setListShown(true);
		} else {
			this.setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader loader) {
		this.adapter.swapCursor(null);
	}

	// interfaces

    public interface OnMessageListener {
        void onMessageSelected(RichPushMessage message);
    }

}
