package com.urbanairship.richpush.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.urbanairship.richpush.RichPushManager;

public class MessageFragmentAdapter extends FragmentStatePagerAdapter {

    public MessageFragmentAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return MessageFragment.newInstance(this.getMessageId(position));
    }

    @Override
    public int getCount() {
        return RichPushManager.shared().getRichPushUser().getInbox().getMessageCount();
    }

    public int getPosition(String messageId) {
        return RichPushManager.shared().getRichPushUser().getInbox().getMessagePosition(messageId);
    }

    public String getMessageId(int position) {
        return RichPushManager.shared().getRichPushUser().getInbox().getMessageIdAtPosition(position);
    }
}
