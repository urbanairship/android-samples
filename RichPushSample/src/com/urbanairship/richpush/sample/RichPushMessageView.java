/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.richpush.RichPushMessageJavaScript;
import com.urbanairship.richpush.RichPushUser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Displays the rich push message
 *
 */
public class RichPushMessageView extends WebView {

    boolean isJSInterfaceAdded = false;

    public RichPushMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        configureBrowser();
    }

    public RichPushMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        configureBrowser();
    }

    public RichPushMessageView(Context context) {
        super(context);
        configureBrowser();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Android JB bug where it logs errors incorrectly in a view pager
        // http://stackoverflow.com/questions/12090899/android-webview-jellybean-should-not-happen-no-rect-based-test-nodes-found
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            int y = this.getScrollY();
            int x = this.getScrollX();
            this.onScrollChanged(x, y, x, y);
        }

        return super.onTouchEvent(event);
    }

    /**
     * Loads the web view with the rich push message
     * @param message
     */
    @SuppressWarnings("unchecked")
    public void loadRichPushMessage(RichPushMessage message) {
        if (isJSInterfaceAdded) {
            removeJavascriptInterface(RichPushManager.getJsIdentifier());
            isJSInterfaceAdded = false;
        }

        // Try to load the javascript interface
        Class<? extends RichPushMessageJavaScript> jsInterfaceClass = RichPushManager.getJsInterface();
        if (jsInterfaceClass != null) {
            try {
                Constructor<RichPushMessageJavaScript> constructor =
                        (Constructor<RichPushMessageJavaScript>)jsInterfaceClass.getConstructor(View.class,
                                String.class);
                RichPushMessageJavaScript jsInterface = constructor.newInstance(this, message.getMessageId());
                addJavascriptInterface(jsInterface, RichPushManager.getJsIdentifier());
                isJSInterfaceAdded = true;
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

        loadUrl(message.getMessageBodyUrl());
    }

    /**
     * Configures the web view to display a rich push message
     */
    private void configureBrowser() {
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAppCacheMaxSize(1024*1024*8);
        settings.setAppCachePath(UAirship.shared().getApplicationContext().getCacheDir().getAbsolutePath());
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        setWebChromeClient(new WebChromeClient());

        setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                RichPushUser user = RichPushManager.shared().getRichPushUser();
                handler.proceed(user.getId(), user.getPassword());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

        });
    }
}
