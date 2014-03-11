/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.inbox;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.urbanairship.richpush.RichPushMessage;

import java.util.List;

/**
 * Pager adapter that manages the message fragments.  Activities that attach
 * this fragment must implement MessageFragmentAdapter.Listener.
 */
public class MessageFragmentAdapter extends FragmentStatePagerAdapter {

    private List<RichPushMessage> messages;

    public MessageFragmentAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        if (messages == null || position >= messages.size()) {
            return null;
        }
        String messageId = messages.get(position).getMessageId();
        return MessageFragment.newInstance(messageId);
    }

    @Override
    public int getCount() {
        if (messages == null) {
            return 0;
        }
        return messages.size();
    }

    /**
     * Set the list of rich push messages
     * @param messages The current list of rich push messages to display
     */
    public void setRichPushMessages(List<RichPushMessage> messages) {
        this.messages = messages;
        this.notifyDataSetChanged();
    }

    /**
     * Gets the RichPushMessage at the position in the adapter
     * @param position Position of the rich push message
     * @return The rich push message at the position in the adapter
     */
    public RichPushMessage getMessage(int position) {
        return messages.get(position);
    }
}
