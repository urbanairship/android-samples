package com.urbanairship.richpush.sample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.UrbanAirshipProvider;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.util.UAStringUtil;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
public class InboxActivity extends SherlockFragmentActivity implements
        InboxFragment.OnMessageListener,
        ActionBar.OnNavigationListener,
        ActionMode.Callback,
        MessageViewPager.ViewPagerTouchListener {

    private static final SimpleDateFormat UA_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    static final String CHECKED_IDS_KEY = "com.urbanairship.richpush.sample.CHECKED_IDS";
    static final String MESSAGE_ID_KEY = "com.urbanairship.richpush.sample.FIRST_MESSAGE_ID";

    ActionMode actionMode;
    ArrayAdapter<String> navAdapter;
    boolean panedView = false;
    boolean drawerView = false;
    Bundle state;
    MessageViewPager messagePager;
    ImageView handle;
    InboxFragment inbox;
    Set<String> checkedIds = new HashSet<String>();
    String firstMessageIdSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.inbox);
        this.state = savedInstanceState;
        this.discoverViewType();
    }

    @Override
    protected void onStart() {
        super.onStart();
        UAirship.shared().getAnalytics().activityStarted(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setState();
        this.configureActionBar();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(MESSAGE_ID_KEY, this.firstMessageIdSelected);
        savedInstanceState.putStringArray(CHECKED_IDS_KEY, this.checkedIds
                .toArray(new String[this.checkedIds.size()]));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (panedView) {
            this.messagePager.clearViewPagerTouchListener();
        }
    }

    @Override
    protected void onStop() {
        super.onStart();
        UAirship.shared().getAnalytics().activityStopped(this);
    }

    @Override
    public void onMessageSelected(RichPushMessage message) {
		message.markRead();
        this.showMessage(message.getMessageId());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                this.startActivity(intent);
                this.finish();
                break;
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        String navName = this.navAdapter.getItem(itemPosition);
        if (RichPushApplication.HOME_ACTIVITY.equals(navName)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            this.startActivity(intent);
            this.finish();
        } else if (RichPushApplication.INBOX_ACTIVITY.equals(navName)) {
            // do nothing, we're here
        }
        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.inbox_actions_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
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
                RichPushManager.shared().getRichPushUser().getInbox().deleteMessages(this.checkedIds);
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
        this.checkedIds.clear();
        this.firstMessageIdSelected = null;
        this.actionMode = null;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
        this.finish();
    }

    // helpers

    private void setState() {
        this.inbox = (InboxFragment) this.getSupportFragmentManager().findFragmentById(R.id.inbox);
        this.inbox.setViewBinder(new MessageBinder());

        if (panedView) {
            this.inbox.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            this.inbox.getListView().setBackgroundColor(Color.BLACK);
            this.messagePager = (MessageViewPager) this.findViewById(R.id.message_pager);
            this.messagePager.setOnPageChangeListener(new MessageViewPagerListener());

            if (this.state != null) {
                String messageId = this.state.getString(MESSAGE_ID_KEY);
                if (!UAStringUtil.isEmpty(messageId)) {
                    this.firstMessageIdSelected = messageId;
                    Collections.addAll(this.checkedIds, this.state.getStringArray(CHECKED_IDS_KEY));
                }
            }

            if (drawerView) {
                this.messagePager.setViewPagerTouchListener(this);
                this.handle = (ImageView) this.findViewById(R.id.handle);
                this.handle.setBackgroundResource(this.inbox.isVisible() ? R.drawable.inbox_open :
                    R.drawable.inbox_close);
                this.handle.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (InboxActivity.this.inbox.isVisible()) {
                            InboxActivity.this.closeInbox();
                        } else {
                            InboxActivity.this.openInbox();
                        }
                    }
                });
            }
        }

        String messageId = this.getIntent().getStringExtra(RichPushApplication.MESSAGE_ID_RECEIVED_KEY);
        Logger.debug("Received message id " + messageId);
        if (!UAStringUtil.isEmpty(messageId)) {
            this.showMessage(messageId);
        }
    }

    private void openInbox() {
        this.getSupportFragmentManager().beginTransaction().show(InboxActivity.this.inbox).commit();
        this.handle.setBackgroundResource(R.drawable.inbox_close);
    }

    private void closeInbox() {
        this.getSupportFragmentManager().beginTransaction().hide(InboxActivity.this.inbox).commit();
        this.handle.setBackgroundResource(R.drawable.inbox_open);
    }

    private void discoverViewType() {
        if (this.findViewById(R.id.message_pane) != null) {
            this.drawerView = this.panedView = true;
        } else if (this.findViewById(R.id.message_pager) != null) {
            this.panedView = true;
        }
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

    private void showMessage(String messageId) {
        if (this.panedView) {
            this.messagePager.setCurrentMessage(messageId);
            if (this.drawerView)
                this.closeInbox();
        } else {
            Intent intent = new Intent(this, MessageActivity.class);
            intent.putExtra(MessageActivity.EXTRA_MESSAGE_ID_KEY, messageId);
            this.startActivity(intent);
        }
    }

    private void startActionModeIfNecessary(String messageId) {
        if (this.actionMode == null && !UAStringUtil.isEmpty(messageId)) {
            if (this.firstMessageIdSelected == null) this.firstMessageIdSelected = messageId;
            this.actionMode = this.startActionMode(this);
        }
    }

    @Override
    public void onViewPagerTouch() {
        if (this.inbox.isVisible()) this.closeInbox();
    }

    // inner-classes

	class MessageBinder implements RichPushCursorAdapter.ViewBinder {
		@Override
		public void setViewValue(View view, RichPushMessage message, String columnName) {
			if (columnName.equals(UrbanAirshipProvider.RichPush.COLUMN_NAME_UNREAD)) {
				view.setBackgroundColor(message.isRead() ? Color.BLACK : Color.YELLOW);
			} else if (columnName.equals(UrbanAirshipProvider.RichPush.COLUMN_NAME_MESSAGE)) {
				((TextView) view).setText(message.getMessage());
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
            String messageId = InboxActivity.this.messagePager.getCurrentMessageId();
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

}