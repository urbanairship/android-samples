package com.urbanairship.richpush.sample;
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

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.urbanairship.analytics.Analytics;
import com.urbanairship.google.PlayServicesUtils;
import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.richpush.sample.inbox.InboxFragment;
import com.urbanairship.richpush.sample.inbox.MessageActivity;
import com.urbanairship.richpush.sample.preference.PushPreferencesActivity;
import com.urbanairship.util.UAStringUtil;


/**
 * The main application activity.
 */
public class MainActivity extends ActionBarActivity implements InboxFragment.Listener {

    private static final String TAG = "MainActivity";

    /**
     * Remember the position of the selected item for the navigation drawer.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Extra to launch a Message ID.
     */
    public static final String EXTRA_MESSAGE_ID = "com.urbanairship.richpush.sample.EXTRA_MESSAGE_ID";

    /**
     * Extra to select what item the fragment. Either {@link #HOME_ITEM} or {@link #INBOX_ITEM}.
     */
    public static final String EXTRA_NAVIGATE_ITEM = "com.urbanairship.richpush.sample.EXTRA_NAVIGATE_ITEM";

    /**
     * Home fragment position.
     */
    public static final int HOME_ITEM = 0;

    /**
     * Inbox fragment position.
     */
    public static final int INBOX_ITEM = 1;

    private int currentPosition = 0;
    private ListView navigationList;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configure the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        this.drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.navigationList = (ListView) findViewById(R.id.navigation_list);

        // Navigation Drawer
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, 0, 0);
        drawerLayout.setDrawerListener(drawerToggle);

        // Actual navigation
        navigationList.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.navigation_item,
                new String[] {
                        getString(R.string.home_title),
                        getString(R.string.inbox_title),
                }));

        navigationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                navigate(position);
            }
        });

        if (savedInstanceState != null) {
            navigate(savedInstanceState.getInt(STATE_SELECTED_POSITION));
        } else {
            navigate(HOME_ITEM);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, currentPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        drawerToggle.onConfigurationChanged(newConfig);
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
    protected void onResume() {
        super.onResume();

        // Handle any extras to set the position
        int position = getIntent().getIntExtra(EXTRA_NAVIGATE_ITEM, -1);
        if (position != -1) {
            getIntent().removeExtra(EXTRA_NAVIGATE_ITEM);
            navigate(position);
        }

        // Handle any pending messages to be shown
        String pendingMessageId = getIntent().getStringExtra(EXTRA_MESSAGE_ID);
        if (!UAStringUtil.isEmpty(pendingMessageId)) {
            getIntent().removeExtra(EXTRA_MESSAGE_ID);
            showRichPushMessage(pendingMessageId);
        }

        // Handle the "com.urbanairship.VIEW_RICH_PUSH_INBOX" intent action.
        if (RichPushInbox.VIEW_INBOX_INTENT_ACTION.equals(getIntent().getAction())) {
            navigate(INBOX_ITEM);
            // Clear the action so we don't handle it again
            getIntent().setAction(null);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Activity instrumentation for analytic tracking
        Analytics.activityStopped(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.preferences:
                this.startActivity(new Intent(this, PushPreferencesActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMessageOpen(RichPushMessage message) {
        showRichPushMessage(message.getMessageId());
    }

    /**
     * Navigates to an item in the navigation drawer.
     *
     * @param position The position to navigate to in the drawer.
     */
    private void navigate(int position) {
        Fragment fragment;
        switch (position) {
            case HOME_ITEM:
                setTitle(R.string.app_name);
                fragment = new HomeFragment();
                break;
            case INBOX_ITEM:
                setTitle(R.string.inbox_title);
                fragment = new InboxFragment();
                break;
            default:
                Log.e(TAG, "Invalid navigation drawer position");
                return;
        }

        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.container, fragment)
                                   .commit();

        currentPosition = position;
        navigationList.setItemChecked(position, true);
        drawerLayout.closeDrawers();
    }

    /**
     * Launches a {@link com.urbanairship.richpush.sample.inbox.MessageActivity} to show a
     * Rich Push message.
     *
     * @param messageId The ID of the Rich Push message to show.
     */
    private void showRichPushMessage(String messageId) {

        // Use the com.urbanairship.VIEW_RICH_PUSH_MESSAGE that is also used by the open_mc_action
        Intent intent = new Intent(this, MessageActivity.class)
                .setAction(RichPushInbox.VIEW_MESSAGE_INTENT_ACTION)
                .setData(Uri.fromParts(RichPushInbox.MESSAGE_DATA_SCHEME, messageId, null));

        this.startActivity(intent);
    }
}
