package com.urbanairship.richpush.sample;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

@SuppressWarnings("unused")
public class MessageActivity extends FragmentActivity {
	protected static final String TAG = "MessageActivity";

    public static final String EXTRA_URL_KEY = "com.urbanairship.richpush.sample.EXTRA_URL_KEY";

    MessageFragment message;
    String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.message);
        this.message = (MessageFragment) this.getSupportFragmentManager().findFragmentById(
                R.id.message_view);

        if (savedInstanceState == null) {
            this.url = this.getIntent().getStringExtra(EXTRA_URL_KEY);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.message.loadUrl(this.url);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(EXTRA_URL_KEY, this.url);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        this.url = savedInstanceState.getString(EXTRA_URL_KEY);
    }

}
