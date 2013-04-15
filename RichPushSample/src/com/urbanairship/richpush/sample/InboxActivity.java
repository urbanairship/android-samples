/*
 * Copyright 2012 Urban Airship and Contributors
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
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

public class InboxActivity extends SherlockFragmentActivity implements
InboxFragment.OnMessageListener,
ActionBar.OnNavigationListener,
ActionMode.Callback,
RichPushManager.Listener,
RichPushInbox.Listener {

    static final String CHECKED_IDS_KEY = "com.urbanairship.richpush.sample.CHECKED_IDS";
    static final String MESSAGE_ID_KEY = "com.urbanairship.richpush.sample.FIRST_MESSAGE_ID";

    private ActionMode actionMode;
    private ArrayAdapter<String> navAdapter;

    private MessageViewPager messagePager;
    private InboxFragment inbox;
    private RichPushInbox richPushInbox;

    private String pendingMessageId;
    private List<RichPushMessage> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.inbox);

        configureActionBar();

        this.richPushInbox = RichPushManager.shared().getRichPushUser().getInbox();
        this.inbox = (InboxFragment) this.getSupportFragmentManager().findFragmentById(R.id.inbox);
        this.inbox.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        this.inbox.getListView().setBackgroundColor(Color.BLACK);

        this.messagePager = (MessageViewPager) this.findViewById(R.id.message_pager);
        if (messagePager != null) {
            this.messagePager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
                @Override
                public void onPageSelected(int position) {
                    messages.get(position).markRead();

                    // highlight the current item you are viewing in the inbox
                    inbox.getListView().setItemChecked(position, true);
                }
            });
        }

        if (savedInstanceState == null) {
            this.setPendingMessageIdFromIntent(getIntent());
        }

        updateRichPushMessages();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        this.setPendingMessageIdFromIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        UAirship.shared().getAnalytics().activityStarted(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        setNavigationToInboxActivity();
        RichPushManager.shared().addListener(this);
        RichPushManager.shared().getRichPushUser().getInbox().addListener(this);

        showPendingMessageId();
        startActionModeIfNecessary();
    }

    @Override
    protected void onPause() {
        super.onPause();
        RichPushManager.shared().removeListener(this);
        richPushInbox.removeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        UAirship.shared().getAnalytics().activityStopped(this);
    }

    @Override
    public void onMessageSelected(RichPushMessage message) {
        message.markRead();
        showMessage(message.getMessageId());
    }

    @Override
    public void onSelectionChanged() {
        startActionModeIfNecessary();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.inbox_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case android.R.id.home:
            navigateToMain();
            break;
        case R.id.refresh:
            inbox.setListShownNoAnimation(false);
            RichPushManager.shared().refreshMessages();
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
        Logger.debug("onPrepareActionMode");
        mode.getMenuInflater().inflate(R.menu.inbox_actions_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        Logger.debug("onPrepareActionMode");
        String firstMessageId = inbox.getSelectedMessages().get(0);
        RichPushMessage firstMessage = richPushInbox.getMessage(firstMessageId);
        menu.findItem(R.id.mark_read).setVisible(!firstMessage.isRead());
        menu.findItem(R.id.mark_unread).setVisible(firstMessage.isRead());

        return true;
    }


    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Logger.debug("onActionItemClicked");
        switch(item.getItemId()) {
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
        inbox.clearSelection();
        actionMode = null;
    }

    @Override
    public void onBackPressed() {
        navigateToMain();
    }

    // helpers

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);

        this.finish();
    }

    private void configureActionBar() {
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        this.navAdapter = new ArrayAdapter<String>(this, R.layout.sherlock_spinner_dropdown_item,
                RichPushApplication.navList);
        actionBar.setListNavigationCallbacks(this.navAdapter, this);
    }

    private void setNavigationToInboxActivity() {
        int position = this.navAdapter.getPosition(RichPushApplication.INBOX_ACTIVITY);
        getSupportActionBar().setSelectedNavigationItem(position);
    }

    private void setPendingMessageIdFromIntent(Intent intent) {
        pendingMessageId = intent.getStringExtra(RichPushApplication.MESSAGE_ID_RECEIVED_KEY);

        if(!UAStringUtil.isEmpty(pendingMessageId)) {
            Logger.debug("Received message id " + pendingMessageId);
        }
    }

    private void showPendingMessageId() {
        if (!UAStringUtil.isEmpty(pendingMessageId)) {
            showMessage(pendingMessageId);
            pendingMessageId = null;
        }
    }

    private void showMessage(String messageId) {
        if (messagePager != null) {
            this.messagePager.setCurrentItem(RichPushMessageUtils.getMessagePosition(messageId, messages));
        } else {
            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra(MessageActivity.EXTRA_MESSAGE_ID_KEY, messageId);
            this.startActivity(intent);
        }
    }

    private void startActionModeIfNecessary() {
        List<String> checkedIds = inbox.getSelectedMessages();
        if (actionMode != null && checkedIds.isEmpty()) {
            actionMode.finish();
            return;
        } else if (actionMode == null && !checkedIds.isEmpty()) {
            actionMode = this.startActionMode(this);
        }
    }

    //interface callbacks

    @Override
    public void onUpdateMessages(boolean success) {
        //stop the progress spinner and display the list
        inbox.setListShownNoAnimation(true);

        //if the message update failed
        if (!success) {
            //show an error dialog
            DialogFragment fragment = new InboxRefreshFailedDialog();
            fragment.show(getSupportFragmentManager(), "dialog");
        }
    }

    //no-op
    @Override
    public void onUpdateUser(boolean success) {
    }

    //no-op
    @Override
    public void onRetrieveMessage(boolean success, String messageId) {
    }

    @Override
    public void onUpdateInbox() {
        updateRichPushMessages();
    }

    private void updateRichPushMessages() {
        messages = RichPushManager.shared().getRichPushUser().getInbox().getMessages();
        this.inbox.setMessages(messages);
        if (messagePager != null) {
            this.messagePager.setMessages(messages);
        }
    }

    public static class InboxRefreshFailedDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
            .setIcon(R.drawable.icon)
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


}