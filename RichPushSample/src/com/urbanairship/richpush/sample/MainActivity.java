/*
 * Copyright 2012 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushUser;
import com.urbanairship.util.UAStringUtil;

@SuppressWarnings("unused")
public class MainActivity extends SherlockFragmentActivity implements
ActionBar.OnNavigationListener {
    protected static final String TAG = "MainActivity";

    static final String ALIAS_KEY = "com.urbanairship.richpush.sample.ALIAS";
    static final int aliasType = 1;

    ArrayAdapter<String> navAdapter;
    RichPushUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);
        this.user = RichPushManager.shared().getRichPushUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        UAirship.shared().getAnalytics().activityStarted(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.configureActionBar();
        this.displayMessageIfNecessary();
    }

    @Override
    protected void onStop() {
        super.onStart();
        UAirship.shared().getAnalytics().activityStopped(this);
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
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(intent);
        }
        return true;
    }

    // helpers

    private void displayMessageIfNecessary() {
        String messageId = this.getIntent().getStringExtra(RichPushApplication.MESSAGE_ID_RECEIVED_KEY);
        if (!UAStringUtil.isEmpty(messageId)) {
            MessageFragment message = MessageFragment.newInstance(messageId);
            message.show(this.getSupportFragmentManager(), R.id.floating_message_pane, "message");
            this.findViewById(R.id.floating_message_pane).setVisibility(View.VISIBLE);
        }
    }

    private void dismissMessageIfNecessary() {
        MessageFragment message = (MessageFragment) this.getSupportFragmentManager()
                .findFragmentByTag("message");
        if (message != null) {
            message.dismiss();
            this.findViewById(R.id.floating_message_pane).setVisibility(View.INVISIBLE);
        }
    }

    private void configureActionBar() {
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        this.navAdapter = new ArrayAdapter<String>(this, R.layout.sherlock_spinner_dropdown_item,
                RichPushApplication.navList);
        actionBar.setListNavigationCallbacks(this.navAdapter, this);
        actionBar.setSelectedNavigationItem(this.navAdapter.getPosition("Home"));
    }
}
