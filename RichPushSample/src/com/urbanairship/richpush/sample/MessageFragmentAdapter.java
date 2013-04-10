/*
 * Copyright 2012 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.urbanairship.richpush.RichPushMessage;

import java.util.List;

public class MessageFragmentAdapter extends FragmentStatePagerAdapter {

    private List<RichPushMessage> messages;

    public MessageFragmentAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        if (messages == null || messages.size() < position) {
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

    //the default implementation of this method returns POSITION_UNCHANGED, which effectively
    //assumes that fragments will never change position or be destroyed
    @Override
    public int getItemPosition(Object item) {
        return POSITION_NONE;
    }

    // helpers

    public void setRichPushMessages(List<RichPushMessage> messages) {
        this.messages = messages;
    }

}
