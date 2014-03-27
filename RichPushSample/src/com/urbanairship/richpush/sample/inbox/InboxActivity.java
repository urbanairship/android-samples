/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.inbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;

import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.richpush.sample.MainActivity;
import com.urbanairship.richpush.sample.R;
import com.urbanairship.richpush.sample.RichPushApplication;
import com.urbanairship.richpush.sample.preference.PushPreferencesActivity;
import com.urbanairship.richpush.sample.view.CustomSlidingPaneLayout;
import com.urbanairship.util.UAStringUtil;

/**
 * Activity that manages the inbox.  On a phone the layout will only contain
 * an InboxFragment.  On a tablet the layout will contain a sliding pager that
 * shows an InboxFragment and a MessagePagerFragment.
 */
public class InboxActivity extends ActionBarActivity implements
        InboxFragment.Listener,
        MessagePagerFragment.Listener,
        ActionBar.OnNavigationListener,
        SlidingPaneLayout.PanelSlideListener {


    private ArrayAdapter<String> navAdapter;
    private MessagePagerFragment messagePager;
    private InboxFragment inbox;
    private CustomSlidingPaneLayout slidingPaneLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.inbox_activity);
        configureActionBar();

        this.inbox = (InboxFragment) this.getSupportFragmentManager().findFragmentById(R.id.inbox);

        // Optional message pager.  If not available messages will be opened in a separate activity
        this.messagePager = (MessagePagerFragment) this.getSupportFragmentManager().findFragmentById(R.id.pager);

        slidingPaneLayout = (CustomSlidingPaneLayout) findViewById(R.id.sliding_pane_layout);
        if (slidingPaneLayout != null) {
            slidingPaneLayout.setPanelSlideListener(this);
            slidingPaneLayout.openPane();

            slidingPaneLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // If sliding pane layout is slidable, set the actionbar to have an up action
                    getSupportActionBar().setDisplayHomeAsUpEnabled(slidingPaneLayout.isSlideable());
                    slidingPaneLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Activity instrumentation for analytic tracking
        UAirship.shared().getAnalytics().activityStarted(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set the navigation to show Inbox
        setNavigationToInboxActivity();

        // Show any pending message ids from the intent
        showPendingMessageId();
    }


    @Override
    protected void onStop() {
        super.onStop();

        // Activity instrumentation for analytic tracking
        UAirship.shared().getAnalytics().activityStopped(this);
    }

    @Override
    public void onMessageOpen(RichPushMessage message) {
        message.markRead();
        showMessage(message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inbox_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (slidingPaneLayout != null) {
                    if (slidingPaneLayout.isOpen()) {
                        slidingPaneLayout.closePane();
                    } else {
                        slidingPaneLayout.openPane();
                    }
                }
                break;
            case R.id.refresh:
                inbox.refreshMessages();
                break;
            case R.id.preferences:
                this.startActivity(new Intent(this, PushPreferencesActivity.class));
                break;

        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        String navName = this.navAdapter.getItem(itemPosition);
        if (RichPushApplication.HOME_ACTIVITY.equals(navName)) {
            navigateToMain();
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        navigateToMain();
    }

    /**
     * Navigates to the main activity and finishes the current one
     */
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);

        this.finish();
    }

    /**
     * Configures the action bar to have a navigation list of
     * 'Home' and 'Inbox'
     */
    private void configureActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        this.navAdapter = new ArrayAdapter<String>(this, android.support.v7.appcompat.R.layout.support_simple_spinner_dropdown_item,
                RichPushApplication.navList);
        actionBar.setListNavigationCallbacks(this.navAdapter, this);
    }

    /**
     * Sets the action bar navigation to show 'Inbox'
     */
    private void setNavigationToInboxActivity() {
        int position = this.navAdapter.getPosition(RichPushApplication.INBOX_ACTIVITY);
        getSupportActionBar().setSelectedNavigationItem(position);
    }

    /**
     * Tries to show a message if the pendingMessageId is set.
     * Clears the pendingMessageId after.
     */
    private void showPendingMessageId() {
        String pendingMessageId = getIntent().
                getStringExtra(RichPushApplication.MESSAGE_ID_RECEIVED_KEY);

        if (!UAStringUtil.isEmpty(pendingMessageId)) {
            getIntent().removeExtra(RichPushApplication.MESSAGE_ID_RECEIVED_KEY);

            RichPushInbox richPushInbox = RichPushManager.shared().getRichPushUser().getInbox();
            RichPushMessage message = richPushInbox.getMessage(pendingMessageId);
            showMessage(message);
        }
    }

    /**
     * Shows a message either in the message view pager, or by launching
     * a new MessageActivity
     *
     * @param message The message to show
     */
    private void showMessage(RichPushMessage message) {

        // Message is already deleted, skip
        if (message == null) {
            return;
        }

        if (slidingPaneLayout != null && slidingPaneLayout.isSlideable() && slidingPaneLayout.isOpen()) {
            slidingPaneLayout.closePane();
        }

        message.markRead();

        if (messagePager != null) {
            this.messagePager.setCurrentMessage(message);
        } else {
            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra(MessageActivity.EXTRA_MESSAGE_ID_KEY, message.getMessageId());
            this.startActivity(intent);
        }
    }


    @Override
    public void onPanelClosed(View panel) {
        if (messagePager != null) {
            messagePager.enablePaging(true);
        }
    }

    @Override
    public void onPanelOpened(View panel) {
        if (messagePager != null) {
            messagePager.enablePaging(false);
        }
    }

    @Override
    public void onPanelSlide(View arg0, float arg1) {
        //do nothing
    }

    // Required for MessagePagerFragment
    @Override
    public void onMessageChanged(int position, RichPushMessage message) {
        // do nothing
    }
}
