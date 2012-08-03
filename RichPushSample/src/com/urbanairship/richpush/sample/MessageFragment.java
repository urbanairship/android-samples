package com.urbanairship.richpush.sample;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
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
import com.urbanairship.richpush.RichPushMessageJavaScript;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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

    @SuppressWarnings("unchecked")
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

        Class<? extends RichPushMessageJavaScript> jsInterfaceClass = RichPushManager.getJsInterface();
        if (jsInterfaceClass != null) {
            try {
                Constructor<RichPushMessageJavaScript> constructor =
                        (Constructor<RichPushMessageJavaScript>)jsInterfaceClass.getConstructor(View.class,
                                String.class);
                RichPushMessageJavaScript jsInterface = constructor.newInstance(this.browser,
                        this.getArguments().getString(MESSAGE_ID_KEY));
                this.browser.addJavascriptInterface(jsInterface, RichPushManager.getJsIdentifier());
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

}
