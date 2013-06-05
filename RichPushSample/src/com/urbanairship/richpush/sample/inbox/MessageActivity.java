/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.inbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.richpush.sample.R;
import com.urbanairship.richpush.sample.R.id;
import com.urbanairship.richpush.sample.R.layout;
import com.urbanairship.richpush.sample.widget.RichPushWidgetUtils;

import java.util.List;

/**
 * Manages the message view pager and display messages
 *
 */
public class MessageActivity extends SherlockFragmentActivity {

    public static final String EXTRA_MESSAGE_ID_KEY = "com.urbanairship.richpush.sample.EXTRA_MESSAGE_ID_KEY";

    private ViewPager messagePager;
    private List<RichPushMessage> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.message);

        String messageId = savedInstanceState == null ? this.getIntent().getStringExtra(EXTRA_MESSAGE_ID_KEY) :
            savedInstanceState.getString(EXTRA_MESSAGE_ID_KEY);

        // Get the list of rich push messages
        this.messages = RichPushManager.shared().getRichPushUser().getInbox().getMessages();

        // Sets up the MessageViewPager
        this.messagePager = (ViewPager) this.findViewById(R.id.message_pager);
        MessageFragmentAdapter  messageAdapter = new MessageFragmentAdapter(this.getSupportFragmentManager());
        this.messagePager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                messages.get(position).markRead();
            }
        });
        messageAdapter.setRichPushMessages(messages);
        this.messagePager.setAdapter(messageAdapter);

        // Get the first item to show
        int position = 0;
        RichPushMessage firstMessage = RichPushManager.shared().getRichPushUser().getInbox().getMessage(messageId);
        if (firstMessage != null) {
            position = messages.indexOf(firstMessage);
            if (position == -1) {
                position = 0;
            }
        }

        // Mark it as read
        messages.get(position).markRead();

        // Sets the current item to the position of the current message
        this.messagePager.setCurrentItem(position);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Activity instrumentation for analytic tracking
        UAirship.shared().getAnalytics().activityStarted(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Refresh any widgets
        RichPushWidgetUtils.refreshWidget(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Activity instrumentation for analytic tracking
        UAirship.shared().getAnalytics().activityStopped(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        String messageId = messages.get(messagePager.getCurrentItem()).getMessageId();
        savedInstanceState.putString(EXTRA_MESSAGE_ID_KEY, messageId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        Intent intent = new Intent(this, InboxActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);

        this.finish();
        return true;
    }

}
