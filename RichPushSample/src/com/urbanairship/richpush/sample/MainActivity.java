/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushUser;
import com.urbanairship.util.UAStringUtil;

/**
 * An empty activity used for the home.
 *
 * If activity is started with an intent that has a message id under the key
 * <code>RichPushApplication.MESSAGE_ID_RECEIVED_KEY</code> it will display
 * the message in a dialog fragment.
 *
 */
public class MainActivity extends SherlockFragmentActivity implements
ActionBar.OnNavigationListener {
    protected static final String TAG = "MainActivity";

    static final String ALIAS_KEY = "com.urbanairship.richpush.sample.ALIAS";
    static final int aliasType = 1;

    ArrayAdapter<String> navAdapter;
    RichPushUser user;

    String pendingMessageId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);
        this.configureActionBar();

        this.user = RichPushManager.shared().getRichPushUser();

        // If we have a message id and its the first create, set the pending message id if available
        if (savedInstanceState == null) {
            pendingMessageId = getIntent().getStringExtra(RichPushApplication.MESSAGE_ID_RECEIVED_KEY);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        pendingMessageId = intent.getStringExtra(RichPushApplication.MESSAGE_ID_RECEIVED_KEY);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Activity instrumentation for analytic tracking
        UAirship.shared().getAnalytics().activityStarted(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Activity instrumentation for analytic tracking
        UAirship.shared().getAnalytics().activityStopped(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setNavigationToMainActivity();

        // Show a message dialog if the pending message id is not null
        if (!UAStringUtil.isEmpty(pendingMessageId)) {
            showRichPushMessage(pendingMessageId);
            pendingMessageId = null;

            // Dismiss any notifications if available
            InboxNotificationBuilder.dismissInboxNotification();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getSupportMenuInflater().inflate(R.menu.main_menu, menu);
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

        this.navAdapter = new ArrayAdapter<String>(this, R.layout.sherlock_spinner_dropdown_item,
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
