/*
Copyright 2009-2015 Urban Airship Inc. All rights reserved.


Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.urbanairship.richpush.sample.inbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.urbanairship.analytics.Analytics;
import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.richpush.sample.R;
import com.urbanairship.richpush.sample.preference.PushPreferencesActivity;

/**
 * Manages the message view pager and display messages
 *
 */
public class MessageActivity extends AppCompatActivity implements MessagePagerFragment.Listener {

    private static final String TAG = "MessageActivity";
    private static final String FRAGMENT_TAG = "MessagePagerFragment";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG) == null) {
            String messageId = null;

            // Handle the "com.urbanairship.VIEW_RICH_PUSH_MESSAGE" intent action with the message
            // id encoded in the intent's data in the form of "message:<MESSAGE_ID>
            if (getIntent() != null && getIntent().getData() != null && RichPushInbox.VIEW_MESSAGE_INTENT_ACTION.equals(getIntent().getAction())) {
                messageId = getIntent().getData().getSchemeSpecificPart();
            }

            MessagePagerFragment pagerFragment = MessagePagerFragment.newInstance(messageId);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, pagerFragment, FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Activity instrumentation for analytic tracking
        Analytics.activityStarted(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Activity instrumentation for analytic tracking
        Analytics.activityStopped(this);
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
        if (message != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(message.getTitle());
        }
    }
}
