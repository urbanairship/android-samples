/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.util.UAStringUtil;

import java.util.HashSet;
import java.util.List;

/**
 * Activity that manages the inbox.
 * On a tablet it also manages the message view pager.
 */
public class InboxActivity extends SherlockFragmentActivity implements
InboxFragment.OnMessageListener,
ActionBar.OnNavigationListener,
ActionMode.Callback,
RichPushManager.Listener,
RichPushInbox.Listener,
SlidingPaneLayout.PanelSlideListener {

    static final String CHECKED_IDS_KEY = "com.urbanairship.richpush.sample.CHECKED_IDS";
    static final String MESSAGE_ID_KEY = "com.urbanairship.richpush.sample.FIRST_MESSAGE_ID";

    private ActionMode actionMode;
    private ArrayAdapter<String> navAdapter;

    private CustomViewPager messagePager;

    private InboxFragment inbox;
    private RichPushInbox richPushInbox;
    private ActionBar actionBar;

    private String pendingMessageId;
    private List<RichPushMessage> messages;
    private CustomSlidingPaneLayout slidingPaneLayout;

    private Button actionSelectionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.inbox);

        actionBar = getSupportActionBar();
        configureActionBar();

        this.richPushInbox = RichPushManager.shared().getRichPushUser().getInbox();

        // Set up the inbox fragment
        this.inbox = (InboxFragment) this.getSupportFragmentManager().findFragmentById(R.id.inbox);
        this.inbox.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        this.inbox.getListView().setBackgroundColor(Color.BLACK);

        // Set up the message view pager if it exists
        this.messagePager = (CustomViewPager) this.findViewById(R.id.message_pager);
        if (messagePager != null) {
            messagePager.setAdapter(new MessageFragmentAdapter(this.getSupportFragmentManager()));
            this.messagePager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
                @Override
                public void onPageSelected(int position) {
                    messages.get(position).markRead();
                    // Highlight the current item you are viewing in the inbox
                    inbox.getListView().setItemChecked(position, true);

                    // If we are in actionMode, update the menu items
                    if (actionMode != null) {
                        actionMode.invalidate();
                    }
                }
            });
        }

        slidingPaneLayout =  (CustomSlidingPaneLayout) findViewById(R.id.sliding_pane_layout);
        if (slidingPaneLayout != null) {
            slidingPaneLayout.setPanelSlideListener(this);
            slidingPaneLayout.openPane();

            slidingPaneLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // If sliding pane layout is slidable, set the actionbar to have an up action
                    actionBar.setDisplayHomeAsUpEnabled(slidingPaneLayout.isSlideable());
                    slidingPaneLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

        // First create, try to show any messages from the intent
        if (savedInstanceState == null) {
            this.setPendingMessageIdFromIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        this.setPendingMessageIdFromIntent(intent);
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

        // Listen for any rich push message changes
        RichPushManager.shared().addListener(this);
        RichPushManager.shared().getRichPushUser().getInbox().addListener(this);

        // Update the rich push messages to the latest
        updateRichPushMessages();

        // Show any pending message ids from the intent
        showPendingMessageId();

        startActionModeIfNecessary();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Remove listener for message changes
        RichPushManager.shared().removeListener(this);
        richPushInbox.removeListener(this);
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
        showMessage(message.getMessageId());

        // If we are in actionMode, update the menu items
        if (actionMode != null) {
            actionMode.invalidate();
        }
    }

    @Override
    public void onSelectionChanged() {
        startActionModeIfNecessary();

        // If we are in actionMode, update the menu items
        if (actionMode != null) {
            actionMode.invalidate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.inbox_menu, menu);
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
            inbox.setListShownNoAnimation(false);
            RichPushManager.shared().refreshMessages();
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
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.inbox_actions_menu, menu);

        View customView = LayoutInflater.from(this).inflate(R.layout.cab_selection_dropdown, null);
        actionSelectionButton = (Button) customView.findViewById(R.id.selection_button);

        final PopupMenu popupMenu = new PopupMenu(this, customView);
        popupMenu.getMenuInflater().inflate(R.menu.selection, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(android.view.MenuItem item) {
                if (item.getItemId() == R.id.menu_deselect_all) {
                    inbox.clearSelection();
                } else {
                    inbox.selectAll();
                }
                return true;
            }
        });

        actionSelectionButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                android.view.Menu menu = popupMenu.getMenu();
                menu.findItem(R.id.menu_deselect_all).setVisible(true);
                menu.findItem(R.id.menu_select_all).setVisible( inbox.getSelectedMessages().size() != messages.size());
                popupMenu.show();
            }

        });


        mode.setCustomView(customView);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        Logger.debug("onPrepareActionMode");

        boolean selectionContainsRead = false;
        boolean selectionContainsUnread = false;

        for (String id : inbox.getSelectedMessages()) {
            RichPushMessage message = richPushInbox.getMessage(id);
            if (message.isRead()) {
                selectionContainsRead = true;
            } else {
                selectionContainsUnread = true;
            }

            if (selectionContainsRead && selectionContainsUnread) {
                break;
            }
        }

        // Show them both
        menu.findItem(R.id.mark_read).setVisible(selectionContainsUnread);
        menu.findItem(R.id.mark_unread).setVisible(selectionContainsRead);

        String selectionText = this.getString(R.string.cab_selection, inbox.getSelectedMessages().size());
        actionSelectionButton.setText(selectionText);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Logger.debug("onActionItemClicked");
        switch (item.getItemId()) {
        case R.id.mark_read:
            richPushInbox.markMessagesRead(new HashSet<String>(inbox.getSelectedMessages()));
            break;
        case R.id.mark_unread:
            richPushInbox.markMessagesUnread(new HashSet<String>(inbox.getSelectedMessages()));
            break;
        case R.id.delete:
            richPushInbox.deleteMessages(new HashSet<String>(inbox.getSelectedMessages()));
            break;
        case R.id.abs__action_mode_close_button:
            break;
        default:
            return false;
        }

        actionMode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        Logger.debug("onDestroyActionMode");
        actionMode = null;
        inbox.clearSelection();
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
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        this.navAdapter = new ArrayAdapter<String>(this, R.layout.sherlock_spinner_dropdown_item,
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
     * Sets the pending message by looking for an id in the intent's extra
     * with key <code>RichPushApplication.MESSAGE_ID_RECEIVED_KEY</code>
     * 
     * @param intent Intent to look for a rich push message id
     */
    private void setPendingMessageIdFromIntent(Intent intent) {
        pendingMessageId = intent.getStringExtra(RichPushApplication.MESSAGE_ID_RECEIVED_KEY);

        if (!UAStringUtil.isEmpty(pendingMessageId)) {
            Logger.debug("Received message id " + pendingMessageId);
        }
    }

    /**
     * Tries to show a message if the pendingMessageId is set.
     * Clears the pendingMessageId after.
     */
    private void showPendingMessageId() {
        if (!UAStringUtil.isEmpty(pendingMessageId)) {
            showMessage(pendingMessageId);
            pendingMessageId = null;
        }
    }

    /**
     * Shows a message either in the message view pager, or by launching
     * a new MessageActivity
     * @param messageId the specified message id
     */
    private void showMessage(String messageId) {
        // Message is already deleted, skip
        if (richPushInbox.getMessage(messageId) == null) {
            return;
        }

        if (slidingPaneLayout != null && slidingPaneLayout.isOpen()) {
            slidingPaneLayout.closePane();
        }

        if (messagePager != null) {
            this.messagePager.setCurrentItem(RichPushMessageUtils.getMessagePosition(messageId, messages));
        } else {
            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra(MessageActivity.EXTRA_MESSAGE_ID_KEY, messageId);
            this.startActivity(intent);
        }
    }

    /**
     * Starts the action mode if there are any selected
     * messages in the inbox fragment
     */
    private void startActionModeIfNecessary() {
        List<String> checkedIds = inbox.getSelectedMessages();
        if (actionMode != null && checkedIds.isEmpty()) {
            actionMode.finish();
            return;
        } else if (actionMode == null && !checkedIds.isEmpty()) {
            actionMode = this.startActionMode(this);
        }
    }

    @Override
    public void onUpdateMessages(boolean success) {
        // Stop the progress spinner and display the list
        inbox.setListShownNoAnimation(true);

        // If the message update failed
        if (!success) {
            // Show an error dialog
            DialogFragment fragment = new InboxRefreshFailedDialog();
            fragment.show(getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    public void onUpdateUser(boolean success) {
        // no-op
    }

    @Override
    public void onRetrieveMessage(boolean success, String messageId) {
        // no-op
    }

    @Override
    public void onUpdateInbox() {
        updateRichPushMessages();
    }

    /**
     * Grabs the latest messages from the rich push inbox, and syncs them
     * with the inbox fragment and message view pager if available
     */
    private void updateRichPushMessages() {
        messages = RichPushManager.shared().getRichPushUser().getInbox().getMessages();
        this.inbox.setMessages(messages);
        if (messagePager != null) {
            ((MessageFragmentAdapter) messagePager.getAdapter()).setRichPushMessages(messages);
        }
    }

    /**
     * Alert dialog for when messages fail to refresh
     */
    public static class InboxRefreshFailedDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
            .setIcon(R.drawable.ua_launcher)
            .setTitle(R.string.inbox_refresh_failed_dialog_title)
            .setMessage(R.string.inbox_refresh_failed_dialog_message)
            .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            })
            .create();
        }
    }

    @Override
    public void onPanelClosed(View panel) {
        if (messagePager != null) {
            messagePager.enableTouchEvents(true);
        }
    }

    @Override
    public void onPanelOpened(View panel) {
        if (messagePager != null) {
            messagePager.enableTouchEvents(false);
        }
    }

    @Override
    public void onPanelSlide(View arg0, float arg1) {
        //do nothing
    }
}
