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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.UrbanAirshipProvider;
import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.util.UAStringUtil;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InboxActivity extends SherlockFragmentActivity implements
InboxFragment.OnMessageListener,
ActionBar.OnNavigationListener,
ActionMode.Callback,
RichPushManager.Listener,
RichPushInbox.Listener {

    private static final SimpleDateFormat UA_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    static final String CHECKED_IDS_KEY = "com.urbanairship.richpush.sample.CHECKED_IDS";
    static final String MESSAGE_ID_KEY = "com.urbanairship.richpush.sample.FIRST_MESSAGE_ID";

    ActionMode actionMode;
    ArrayAdapter<String> navAdapter;
    private boolean isSingleWindow;

    MessageViewPager messagePager;
    InboxFragment inbox;
    Set<String> checkedIds = new HashSet<String>();
    String firstMessageIdSelected;

    private String pendingMessageId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.inbox);
        this.setPendingMessageIdFromIntent(getIntent());



        configureActionBar();

        this.inbox = (InboxFragment) this.getSupportFragmentManager().findFragmentById(R.id.inbox);
        this.inbox.setViewBinder(new MessageBinder());
        this.inbox.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        this.inbox.getListView().setBackgroundColor(Color.BLACK);

        this.messagePager = (MessageViewPager) this.findViewById(R.id.message_pager);
        this.messagePager.setOnPageChangeListener(new MessageViewPagerListener());
        this.isSingleWindow = messagePager.getVisibility() == View.GONE;

        View collapsableHandle = this.findViewById(R.id.handle);
        if (collapsableHandle != null) {
            configureCollaspableMessagePane(collapsableHandle);
        }

        if (savedInstanceState != null) {
            String messageId = savedInstanceState.getString(MESSAGE_ID_KEY);
            if (!UAStringUtil.isEmpty(messageId)) {
                this.firstMessageIdSelected = messageId;
                Collections.addAll(this.checkedIds, savedInstanceState.getStringArray(CHECKED_IDS_KEY));
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        this.setPendingMessageIdFromIntent(intent);
        this.showPendingMessageId();
    }

    @Override
    protected void onStart() {
        super.onStart();
        UAirship.shared().getAnalytics().activityStarted(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        RichPushManager.shared().addListener(this);
        RichPushManager.shared().getRichPushUser().getInbox().addListener(this);

        showPendingMessageId();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        Logger.debug("onSaveInstanceState");
        savedInstanceState.putString(MESSAGE_ID_KEY, this.firstMessageIdSelected);
        savedInstanceState.putStringArray(CHECKED_IDS_KEY, this.checkedIds
                .toArray(new String[this.checkedIds.size()]));
    }

    @Override
    protected void onPause() {
        super.onPause();
        RichPushManager.shared().removeListener(this);
        RichPushManager.shared().getRichPushUser().getInbox().removeListener(this);

        messagePager.clearViewPagerTouchListener();
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
        } else if (RichPushApplication.INBOX_ACTIVITY.equals(navName)) {
            if (getSupportFragmentManager().popBackStackImmediate()) {
                messagePager.setVisibility(View.GONE);
            }
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
        if (RichPushManager.shared().getRichPushUser().getInbox()
                .getMessage(this.firstMessageIdSelected).isRead()) {
            menu.findItem(R.id.mark_read_or_unread).setIcon(R.drawable.mark_unread)
            .setTitle(this.getString(R.string.mark_unread));
        } else {
            menu.findItem(R.id.mark_read_or_unread).setIcon(R.drawable.mark_read)
            .setTitle(this.getString(R.string.mark_read));
        }
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Logger.debug("onActionItemClicked");
        switch(item.getItemId()) {
        case R.id.mark_read_or_unread:
            if (this.getString(R.string.mark_read).equals(item.getTitle())) {
                RichPushManager.shared().getRichPushUser().getInbox()
                .markMessagesRead(this.checkedIds);
            } else {
                RichPushManager.shared().getRichPushUser().getInbox()
                .markMessagesUnread(this.checkedIds);
            }
            this.actionMode.finish();
            return true;
        case R.id.delete:
            RichPushManager.shared().getRichPushUser().getInbox()
            .deleteMessages(this.checkedIds);
            this.actionMode.finish();
            return true;
        case R.id.abs__action_mode_close_button:
            this.actionMode.finish();
            return true;
        default:
            return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        Logger.debug("onDestroyActionMode");
        this.checkedIds.clear();
        this.firstMessageIdSelected = null;
        this.actionMode = null;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            messagePager.setVisibility(View.GONE);
        } else {
            navigateToMain();
        }
    }

    // helpers

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);

        this.finish();
    }

    private void configureCollaspableMessagePane(final View collapseHandle) {
        int iconResource = this.inbox.isVisible() ? R.drawable.inbox_open : R.drawable.inbox_close;
        collapseHandle.setBackgroundResource(iconResource);
        collapseHandle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (InboxActivity.this.inbox.isVisible()) {
                    getSupportFragmentManager().beginTransaction().show(InboxActivity.this.inbox).commit();
                    collapseHandle.setBackgroundResource(R.drawable.inbox_close);
                } else {
                    getSupportFragmentManager().beginTransaction().hide(InboxActivity.this.inbox).commit();
                    collapseHandle.setBackgroundResource(R.drawable.inbox_open);
                }
            }
        });
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
        actionBar.setSelectedNavigationItem(this.navAdapter.getPosition(RichPushApplication.INBOX_ACTIVITY));
        this.startActionModeIfNecessary(this.firstMessageIdSelected);
    }

    private void setPendingMessageIdFromIntent(Intent intent) {
        pendingMessageId = intent.getStringExtra(RichPushApplication.MESSAGE_ID_RECEIVED_KEY);

        if(!UAStringUtil.isEmpty(pendingMessageId)) {
            Logger.debug("Received message id " + pendingMessageId);
        }
    }

    private void showPendingMessageId() {
        if(!UAStringUtil.isEmpty(pendingMessageId)) {
            showMessage(pendingMessageId);
            pendingMessageId = null;
        }
    }

    private void showMessage(String messageId) {
        this.messagePager.setCurrentMessage(messageId);
        if (isSingleWindow) {
            FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
            ft.hide(this.inbox);
            ft.addToBackStack("show_message");
            ft.commit();
            messagePager.setVisibility(View.VISIBLE);
        }
    }

    private void startActionModeIfNecessary(String messageId) {
        if (this.actionMode == null && !UAStringUtil.isEmpty(messageId)) {
            if (this.firstMessageIdSelected == null) this.firstMessageIdSelected = messageId;
            this.actionMode = this.startActionMode(this);
        }
    }

    // inner-classes

    class MessageBinder implements RichPushCursorAdapter.ViewBinder {
        @Override
        public void setViewValue(View view, RichPushMessage message, String columnName) {
            if (columnName.equals(UrbanAirshipProvider.RichPush.COLUMN_NAME_UNREAD)) {
                view.setBackgroundColor(message.isRead() ? Color.BLACK : Color.YELLOW);
            } else if (columnName.equals(UrbanAirshipProvider.RichPush.COLUMN_NAME_TITLE)) {
                ((TextView) view).setText(message.getTitle());
            } else if (columnName.equals(UrbanAirshipProvider.COLUMN_NAME_TIMESTAMP)) {
                ((TextView) view).setText(UA_DATE_FORMATTER.format(message.getSentDate()));
            } else {
                view.setOnClickListener(InboxActivity.this.checkBoxListener);
                view.setTag(message.getMessageId());
                if (InboxActivity.this.checkedIds.contains(message.getMessageId())) {
                    ((CheckBox)view).setChecked(true);
                } else {
                    ((CheckBox)view).setChecked(false);
                }
            }
            view.setFocusable(false);
            view.setFocusableInTouchMode(false);
        }
    }

    class MessageViewPagerListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            String messageId = InboxActivity.this.messagePager.getMessageId(position);
            RichPushManager.shared().getRichPushUser().getInbox().getMessage(messageId).markRead();
        }
    }

    OnClickListener checkBoxListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            String messageId = (String) view.getTag();
            if (((CheckBox)view).isChecked()) {
                InboxActivity.this.checkedIds.add(messageId);
            } else {
                InboxActivity.this.checkedIds.remove(messageId);
            }
            InboxActivity.this.startActionModeIfNecessary(messageId);
        }
    };

    //interface callbacks

    @Override
    public void onUpdateMessages(boolean success) {
        //stop the progress spinner and display the list
        inbox.setListShownNoAnimation(true);

        //if the message update failed
        if (!success) {
            //show an error dialog
            DialogFragment fragment = new InboxLoadFailedDialogFragment();
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
        this.inbox.refreshDisplay();
        this.messagePager.refreshDisplay();
    }

    public static class InboxLoadFailedDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
            .setIcon(R.drawable.icon)
            .setTitle("Unable to retrieve new messages")
            .setMessage("Please try again later")
            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            })
            .create();
        }
    }
}