package com.urbanairship.richpush.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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
    static final String EMAIL_KEY = "com.urbanairship.richpush.sample.EMAIL";
    static final int aliasType = 1;
    static final int emailType = 2;

    ArrayAdapter<String> navAdapter;
    EditText aliasInput;
    EditText emailInput;
    RichPushUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);

        this.user = RichPushManager.shared().getRichPushUser();
        this.aliasInput = (EditText)this.findViewById(R.id.alias_input);
        this.emailInput = (EditText)this.findViewById(R.id.email_input);

        if (savedInstanceState == null) {
            this.aliasInput.setText(this.user.getAlias());
            this.emailInput.setText(this.user.getEmailAddress());
        } else {
            this.aliasInput.setText(savedInstanceState.getString(ALIAS_KEY));
            this.emailInput.setText(savedInstanceState.getString(EMAIL_KEY));
        }
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
    protected void onPause() {
        super.onPause();
        this.updateUser();
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(ALIAS_KEY, this.aliasInput.getText().toString());
        savedInstanceState.putString(EMAIL_KEY, this.emailInput.getText().toString());
    }

    @Override
    protected void onStop() {
        super.onStart();
        UAirship.shared().getAnalytics().activityStopped(this);
    }

    @Override
    public void onBackPressed() {
        this.finish();
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
            this.startActivity(new Intent(this, InboxActivity.class));
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

    private void updateUser() {
        this.user.setAlias(this.aliasInput.getText().toString());
        this.user.setEmailAddress(this.emailInput.getText().toString());
        RichPushManager.shared().updateUser();
    }

}
