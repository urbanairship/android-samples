package com.urbanairship.richpush.sample;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.urbanairship.richpush.RichPushMessage;

public class InboxFragment extends ListFragment {

    private static final String SELECTED_MESSAGE_KEY = "com.urbanairship.richpush.SELECTED_MESSAGE";

    Bundle state;
    OnMessageListener listener;

    public static InboxFragment newInstance() {
        return new InboxFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.checkActivityForCorrectInterface(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.state = savedInstanceState;
        this.setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.restoreInstanceState(this.state);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.setListAdapter(new RichPushAdapter(this.getActivity(), R.layout.inbox_message));
        this.setEmptyText(this.getString(R.string.no_messages));
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        list.setItemChecked(position, true);
        this.listener.onMessageSelected((RichPushMessage)list.getItemAtPosition(position));
    }

    @Override
    public void onPause() {
        super.onPause();
        ((RichPushAdapter)this.getListAdapter()).save();
    }

    @Override
    public void onStop() {
        super.onStop();
        ((RichPushAdapter)this.getListAdapter()).cleanup();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(SELECTED_MESSAGE_KEY, this.getListView().getCheckedItemPosition());
    }

    // helpers

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            int position = savedInstanceState.getInt(SELECTED_MESSAGE_KEY, -1);
            if (position > -1) this.getListView().setItemChecked(position, true);
        }
    }

    private void checkActivityForCorrectInterface(Activity activity) {
        // Make sure we can talk to the activity that wants to attach to us
        if (!(activity instanceof OnMessageListener)) {
            throw new IllegalStateException("Activities using " + this.getClass().getName() + " must implement" +
                    "the " + this.getClass().getName() + ".OnMessageListener interface.");
        }
    }

    public void setOnMessageListener(OnMessageListener listener) {
        this.listener = listener;
    }

    // interfaces

    public interface OnMessageListener {
        void onMessageSelected(RichPushMessage message);
    }

}
