/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.inbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.richpush.sample.R;
import com.urbanairship.richpush.sample.preference.PushPreferencesActivity;
import com.urbanairship.richpush.sample.widget.RichPushWidgetUtils;

/**
 * Manages the message view pager and display messages
 *
 */
public class MessageActivity extends ActionBarActivity implements MessagePagerFragment.Listener {

    public static final String EXTRA_MESSAGE_ID_KEY = "com.urbanairship.richpush.sample.EXTRA_MESSAGE_ID_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.message_activity);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeButtonEnabled(true);

        MessagePagerFragment pagerFragment = (MessagePagerFragment) getSupportFragmentManager().findFragmentById(R.id.pager);

        if (savedInstanceState == null) {
            String messageId = getIntent().getStringExtra(EXTRA_MESSAGE_ID_KEY);
            RichPushMessage message = RichPushInbox.shared().getMessage(messageId);
            pagerFragment.setCurrentMessage(message);
        }
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
    public boolean onCreateOptionsMenu(final Menu menu) {
        this.getMenuInflater().inflate(R.menu.message_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.preferences:
                this.startActivity(new Intent(this, PushPreferencesActivity.class));
                return true;
        }
        return false;
    }

    @Override
    public void onMessageChanged(int position, RichPushMessage message) {
        if (message != null) {
            setTitle(message.getTitle());
        }
    }
}
