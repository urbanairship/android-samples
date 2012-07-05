package com.urbanairship.richpush.sample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
public class MessageFragment extends Fragment {

    boolean pageLoaded = false;
	String url;
    WebView browser;

    public MessageFragment() {
    }

    public static MessageFragment newInstance() {
        return new MessageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (this.browser == null) {
			this.browser = (WebView) inflater.inflate(R.layout.message_fragment, container, false);
			this.createWebView();
		} else {
			((ViewGroup)this.browser.getParent()).removeView(this.browser);
			this.browser.setLayoutParams(container.getLayoutParams());
		}
        return this.browser;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
		this.loadUrl(this.url);
    }

    // public api

    public void show(FragmentManager manager, String tag, int layoutId) {
		manager.beginTransaction().add(layoutId, this, tag).commit();
    }

	public void dismiss() {
		this.getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
	}

    public void loadUrl(String url) {
        if (!pageLoaded || (url != null && !url.equals(this.url))) {
			this.url = url;
            this.browser.loadUrl(url);
            this.pageLoaded = true;
        }
    }

    // helpers

    private void createWebView() {
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
				MessageFragment.this.loadUrl(url);
                return true;
            }

        });

    }

}
