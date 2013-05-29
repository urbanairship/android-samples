/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.richpush.RichPushMessageJavaScript;
import com.urbanairship.richpush.RichPushUser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A web view that displays a rich push message
 *
 */
public class RichPushMessageView extends WebView {


    public RichPushMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        configureWebView();
    }

    public RichPushMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        configureWebView();
    }

    public RichPushMessageView(Context context) {
        super(context);
        configureWebView();
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
    public void loadRichPushMessage(RichPushMessage message) {
        if (message == null) {
            Logger.warn("Unable to load null message into RichPushMessageView");
            return;
        }

        if (Build.VERSION.SDK_INT >= 11) {
            removeJavascriptInterface(RichPushManager.getJsIdentifier());
        }

        RichPushMessageJavaScript jsInterface = createRichPushMessageJavaScript(message.getMessageId());
        if (jsInterface != null) {
            addJavascriptInterface(jsInterface, RichPushManager.getJsIdentifier());
        }

        loadUrl(message.getMessageBodyUrl());
    }

    /**
     * Configures the web view to display a rich push message
     */
    protected void configureWebView() {
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


    /**
     * Creates a RichPushMessageJavaScript to be added the webview
     * 
     * @param messageId Message id of the message to display
     * @return A new RichPushMessageJavaScript
     */
    private RichPushMessageJavaScript createRichPushMessageJavaScript(String messageId) {
        Class<? extends RichPushMessageJavaScript> jsInterfaceClass = RichPushManager.getJsInterface();
        if (jsInterfaceClass == null) {
            return null;
        }

        try {
            Constructor<? extends RichPushMessageJavaScript> constructor = jsInterfaceClass.getConstructor(View.class, String.class);
            return constructor.newInstance(this, messageId);
        } catch (NoSuchMethodException e) {
            Logger.error("Failed to add the js interface, the rich push javascript interface implementation does not define a constructor: " + e.getMessage());
        } catch (InvocationTargetException e) {
            Logger.error("Failed to add the js interface, the rich push javascript interface implementation constructor threw an exception", e);
        } catch (java.lang.InstantiationException e) {
            Logger.error("Failed to add the js interface, the rich push javascript interface implementation cannot be instantiated", e);
        } catch (IllegalAccessException e) {
            Logger.error("Failed to add the js interface, the rich push javascript interface implementation's constructor is not accesible: " + e.getMessage());
        }

        return null;
    }
}
