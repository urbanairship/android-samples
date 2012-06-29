package com.urbanairship.richpush.sample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.urbanairship.UAirship;

@SuppressLint("SetJavaScriptEnabled")
public class MessageFragment extends DialogFragment {

    boolean pageLoaded = false;
    WebView browser;

    public MessageFragment() {
        this.setStyle(STYLE_NO_FRAME, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    public static MessageFragment newInstance() {
        return new MessageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This has to go here or else super.onCreate will set it to true
        this.setShowsDialog(false);
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.message_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.browser == null) {
            this.createWebView();
        }
    }

    // public api

    public void show(FragmentManager manager, String tag, String url) {
        this.setShowsDialog(true);
        this.show(manager, tag);
        this.loadUrl(url);
    }

    public void show(FragmentTransaction transaction, String tag, String url) {
        this.setShowsDialog(true);
        this.show(transaction, tag);
        this.loadUrl(url);
    }

    public void loadUrl(String url) {
        if (!pageLoaded) {
            this.browser.loadUrl(url);
            this.pageLoaded = true;
        }
    }

    // helpers

    private void createWebView() {
        this.browser = (WebView) this.getView().findViewById(R.id.browser);
        this.browser.getSettings().setJavaScriptEnabled(true);
        this.browser.getSettings().setDomStorageEnabled(true);
        this.browser.getSettings().setAppCacheEnabled(true);
        this.browser.getSettings().setAllowFileAccess(true);
        this.browser.getSettings().setAppCacheMaxSize(1024*1024*8);
        this.browser.getSettings().setAppCachePath(UAirship.shared().getApplicationContext().getCacheDir().getAbsolutePath());
        this.browser.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        this.browser.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

        });

    }

}
