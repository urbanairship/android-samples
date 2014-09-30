/*
Copyright 2009-2014 Urban Airship Inc. All rights reserved.

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

package com.urbanairship.richpush.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.urbanairship.UAirship;
import com.urbanairship.analytics.Analytics;
import com.urbanairship.google.PlayServicesUtils;
import com.urbanairship.richpush.RichPushUser;
import com.urbanairship.richpush.sample.inbox.InboxActivity;
import com.urbanairship.richpush.sample.inbox.RichPushMessageDialogFragment;
import com.urbanairship.richpush.sample.preference.PushPreferencesActivity;
import com.urbanairship.util.UAStringUtil;


/**
 * An empty activity used for the home.
 *
 * If activity is started with an intent that has a message id under the key
 * <code>RichPushApplication.EXTRA_OPEN_MESSAGE_ID</code> it will display
 * the message in a dialog fragment.
 *
 */
public class MainActivity extends ActionBarActivity implements
ActionBar.OnNavigationListener {

    ArrayAdapter<String> navAdapter;
    RichPushUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main_activity);
        this.configureActionBar();

        this.user = UAirship.shared().getRichPushManager().getRichPushUser();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Handle any Google Play services errors
        if (PlayServicesUtils.isGooglePlayStoreAvailable()) {
            PlayServicesUtils.handleAnyPlayServicesError(this);
        }

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
    protected void onResume() {
        super.onResume();
        setNavigationToMainActivity();

        // Show a message dialog if the pending message id is not null
        String pendingMessageId = getIntent().getStringExtra(RichPushApplication.EXTRA_OPEN_MESSAGE_ID);
        if (!UAStringUtil.isEmpty(pendingMessageId)) {
            getIntent().removeExtra(RichPushApplication.EXTRA_OPEN_MESSAGE_ID);
            showRichPushMessage(pendingMessageId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.preferences:
            this.startActivity(new Intent(this, PushPreferencesActivity.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        String navName = this.navAdapter.getItem(itemPosition);
        if (RichPushApplication.HOME_ACTIVITY.equals(navName)) {
            // do nothing, we're here
        } else if (RichPushApplication.INBOX_ACTIVITY.equals(navName)) {
            Intent intent = new Intent(this, InboxActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            this.startActivity(intent);
        }

        return true;
    }

    /**
     * Displays the rich push message in a RichPushMessageDialogFragment
     * @param messageId The specified message id
     */
    private void showRichPushMessage(String messageId) {
        RichPushMessageDialogFragment message = RichPushMessageDialogFragment.newInstance(messageId);
        message.show(this.getSupportFragmentManager(), "message");
    }

    /**
     * Configures the action bar to have a navigation list of
     * 'Home' and 'Inbox'
     */
    private void configureActionBar() {
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        this.navAdapter = new ArrayAdapter<String>(this, android.support.v7.appcompat.R.layout.support_simple_spinner_dropdown_item,
                RichPushApplication.navList);
        actionBar.setListNavigationCallbacks(this.navAdapter, this);
    }

    /**
     * Sets the action bar navigation to show 'Home'
     */
    private void setNavigationToMainActivity() {
        int position = this.navAdapter.getPosition("Home");
        getSupportActionBar().setSelectedNavigationItem(position);
    }
}
