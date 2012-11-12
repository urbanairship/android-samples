/*
 * Copyright 2012 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushManager;

public class MessageFragmentAdapter extends FragmentStatePagerAdapter {

    public MessageFragmentAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return MessageFragment.newInstance(this.getInbox().getMessageIdAtPosition(position));
    }

    @Override
    public int getCount() {
        return this.getInbox().getCount();
    }

    public int getMessagePosition(String messageId) {
        return this.getInbox().getMessagePosition(messageId);
    }

    public String getMessageId(int position) {
        return this.getInbox().getMessageIdAtPosition(position);
    }

    // helpers

    private RichPushInbox getInbox() {
        return RichPushManager.shared().getRichPushUser().getInbox();
    }

}
