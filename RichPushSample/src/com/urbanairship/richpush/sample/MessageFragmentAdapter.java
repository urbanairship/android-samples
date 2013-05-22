/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.urbanairship.richpush.RichPushMessage;

import java.util.List;

/**
 * Pager adapter that manages the message fragments
 *
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
        MessageFragment fragment = MessageFragment.newInstance(messageId);
        return fragment;
    }

    @Override
    public int getCount() {
        if (messages == null) {
            return 0;
        }
        return messages.size();
    }

    @Override
    public int getItemPosition(Object item) {

        // The default implementation of this method returns POSITION_UNCHANGED, which effectively
        // assumes that fragments will never change position or be destroyed
        return POSITION_NONE;
    }

    /**
     * Set the list of rich push messages
     * @param messages The current list of rich push messages to display
     */
    public void setRichPushMessages(List<RichPushMessage> messages) {
        this.messages = messages;
        this.notifyDataSetChanged();
    }
}
