/*
 * Copyright 2012 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;

import java.util.List;


public class MessageActivity extends SherlockFragmentActivity {

    public static final String EXTRA_MESSAGE_ID_KEY = "com.urbanairship.richpush.sample.EXTRA_MESSAGE_ID_KEY";

    private MessageViewPager messagePager;
    private List<RichPushMessage> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.message);

        String messageId = savedInstanceState == null ? this.getIntent().getStringExtra(EXTRA_MESSAGE_ID_KEY) :
            savedInstanceState.getString(EXTRA_MESSAGE_ID_KEY);

        this.messages = RichPushManager.shared().getRichPushUser().getInbox().getMessages();

        this.messagePager = (MessageViewPager) this.findViewById(R.id.message_pager);
        this.messagePager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                messages.get(position).markRead();
            }
        });
        this.messagePager.setMessages(messages);
        this.messagePager.setCurrentItem(RichPushMessageUtils.getMessagePosition(messageId, messages));
    }

    @Override
    protected void onStart() {
        super.onStart();
        UAirship.shared().getAnalytics().activityStarted(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        UAirship.shared().getAnalytics().activityStopped(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        String messageId = messages.get(messagePager.getCurrentItem()).getMessageId();
        savedInstanceState.putString(EXTRA_MESSAGE_ID_KEY, messageId);
    }


}
