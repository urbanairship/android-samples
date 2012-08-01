package com.urbanairship.richpush.sample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.actionbarsherlock.app.SherlockFragment;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;

import java.io.DataInputStream;
import java.io.IOException;

@SuppressLint("SetJavaScriptEnabled")
public class MessageFragment extends SherlockFragment {

    private static final String MESSAGE_ID_KEY = "com.urbanairship.richpush.URL_KEY";

    WebView browser;

    public static MessageFragment newInstance(String messageId) {
        MessageFragment message = new MessageFragment();
        Bundle arguments = new Bundle();
        arguments.putString(MESSAGE_ID_KEY, messageId);
        message.setArguments(arguments);
        return message;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.browser = new WebView(container.getContext());
        this.browser.setLayoutParams(container.getLayoutParams());
        return this.browser;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.initializeBrowser();
        this.loadUrl();
    }

    // public api

    public void show(FragmentManager manager, int layoutId, String tag) {
		manager.beginTransaction().add(layoutId, this, tag).commit();
    }

	public void dismiss() {
		this.getSherlockActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
	}

    public void loadUrl() {
        RichPushMessage message = RichPushManager.shared().getRichPushUser().getInbox()
                .getMessage(this.getArguments().getString(MESSAGE_ID_KEY));
        // TODO Get the url from the message
        //this.browser.loadUrl(url);
        try {
            this.browser.loadData(this.buildHtml(message), "text/html", "UTF-8");
        } catch (IOException e) {
            Logger.debug(e.getMessage());
        }
    }

    // helpers

    private String buildHtml(RichPushMessage message) throws IOException {
        AssetManager assetManager = UAirship.shared().getApplicationContext().getResources().getAssets();
        DataInputStream dis = new DataInputStream(assetManager.open("message.html"));
        byte[] buffer = new byte[dis.available()];
        dis.readFully(buffer);
        String html = new String(buffer);
        return html.replace("{{ title }}", message.getTitle())
                .replace("{{ message }}", message.getMessage())
                .replace("{{ id }}", message.getMessageId())
                .replace("{{ sent_date }}", message.getSentDate().toString());
    }

    private void initializeBrowser() {
        this.browser.getSettings().setJavaScriptEnabled(true);
        this.browser.getSettings().setDomStorageEnabled(true);
        this.browser.getSettings().setAppCacheEnabled(true);
        this.browser.getSettings().setAllowFileAccess(true);
        this.browser.getSettings().setAppCacheMaxSize(1024*1024*8);
        this.browser.getSettings().setAppCachePath(UAirship.shared().getApplicationContext().getCacheDir().getAbsolutePath());
        this.browser.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        this.browser.setWebChromeClient(new WebChromeClient());

        this.browser.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                MessageFragment.this.browser.loadUrl(url);
                return true;
            }

        });

        this.browser.addJavascriptInterface(new RichPushMessageJavaScriptInterface() {

            @Override
            public int getViewHeight() {
                return MessageFragment.this.getView().getHeight();
            }

            @Override
            public int getViewWidth() {
                return MessageFragment.this.getView().getWidth();
            }

            @Override
            public String getMessageId() {
                return MessageFragment.this.getArguments().getString(MESSAGE_ID_KEY);
            }

            @Override
            public String getUserId() {
                return RichPushManager.shared().getRichPushUser().getId();
            }

            @Override
            public String getDeviceModel() {
                return Build.MODEL;
            }

            @Override
            public String getDeviceOrientation() {
                int orientation = MessageFragment.this.getResources().getConfiguration().orientation;
                switch (orientation) {
                    case Configuration.ORIENTATION_LANDSCAPE:
                        return "landscape";
                    case Configuration.ORIENTATION_PORTRAIT:
                        return "portrait";
                    default:
                        return "undefined";
                }
            }

            @Override
            public void navigateTo(String activityName) {
                Class intentClass = null;
                if ("home".equals(activityName)) {
                    intentClass = MainActivity.class;
                } else if ("inbox".equals(activityName)) {
                    intentClass = InboxActivity.class;
                } else if ("preferences".equals(activityName)) {
                    intentClass = PushPreferencesActivity.class;
                }
                if (intentClass != null) {
                    Intent intent = new Intent(MessageFragment.this.getSherlockActivity(), intentClass);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    MessageFragment.this.getSherlockActivity().startActivity(intent);
                    MessageFragment.this.getSherlockActivity().finish();
                }
            }
        }, "urbanairship");

    }

}
