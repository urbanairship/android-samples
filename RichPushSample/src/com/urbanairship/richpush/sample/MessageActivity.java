package com.urbanairship.richpush.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushManager;

@SuppressWarnings("unused")
public class MessageActivity extends SherlockFragmentActivity {
	protected static final String TAG = "MessageActivity";

    public static final String EXTRA_MESSAGE_ID_KEY = "com.urbanairship.richpush.sample.EXTRA_MESSAGE_ID_KEY";

    MessageViewPager messagePager;
    String currentMessageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.message);

        this.currentMessageId = savedInstanceState == null ? this.getIntent().getStringExtra(EXTRA_MESSAGE_ID_KEY) :
                savedInstanceState.getString(EXTRA_MESSAGE_ID_KEY);
        this.messagePager = (MessageViewPager) this.findViewById(R.id.message_pager);
        this.messagePager.setOnPageChangeListener(new MessageViewPagerListener());
    }

    @Override
    protected void onStart() {
        super.onStart();
        UAirship.shared().getAnalytics().activityStarted(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.messagePager.setCurrentMessage(this.currentMessageId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        UAirship.shared().getAnalytics().activityStopped(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(EXTRA_MESSAGE_ID_KEY, this.currentMessageId);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, InboxActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);
        this.finish();
    }

    // helpers

    class MessageViewPagerListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            MessageActivity.this.currentMessageId = MessageActivity.this.messagePager.getCurrentMessageId();
            RichPushManager.shared().getRichPushUser().getInbox()
                    .getMessage(MessageActivity.this.currentMessageId).markRead();
        }
    }

}
