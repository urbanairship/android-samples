/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.inbox;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.urbanairship.Logger;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.richpush.sample.R;
import com.urbanairship.widget.RichPushMessageWebView;
import com.urbanairship.widget.RichPushWebViewClient;

/**
 * Fragment that displays a rich push message in a RichPushMessageView
 */
@SuppressLint("NewApi")
public class MessageFragment extends Fragment {

    private static final String MESSAGE_ID_KEY = "com.urbanairship.richpush.URL_KEY";
    private RichPushMessageWebView browser;
    private ProgressBar progressBar;
    private RichPushMessage message;

    /**
     * Creates a new MessageFragment
     * @param messageId The message's id to display
     * @return messageFragment new MessageFragment
     */
    public static MessageFragment newInstance(String messageId) {
        MessageFragment message = new MessageFragment();
        Bundle arguments = new Bundle();
        arguments.putString(MESSAGE_ID_KEY, messageId);
        message.setArguments(arguments);
        return message;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        String messageId = getArguments().getString(MESSAGE_ID_KEY);
        message = RichPushManager.shared().getRichPushUser().getInbox().getMessage(messageId);

        if (message == null) {
            Logger.info("Couldn't retrieve message for ID: " + messageId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_fragment, container, false);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        browser = (RichPushMessageWebView) view.findViewById(R.id.message_view);


        if (Build.VERSION.SDK_INT >= 12) {
            browser.setAlpha(0);
        } else {
            browser.setVisibility(View.INVISIBLE);
        }

        // Set a custom RichPushWebViewClient view client to listen for the page finish
        // Note: RichPushWebViewClient is required to load the proper auth and to
        // inject the Urban Airship Javascript interface.  When overriding any methods
        // make sure to call through to the super's implementation.
        browser.setWebViewClient(new RichPushWebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (Build.VERSION.SDK_INT >= 12) {
                    crossFade();
                } else {
                    browser.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (message != null) {
            Logger.info("Loading message: " + message.getMessageId());
            browser.loadRichPushMessage(message);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 11) {
            browser.onResume();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= 11) {
            browser.onPause();
        }
    }

    private void crossFade() {
        browser.animate()
                .alpha(1f)
                .setDuration(200)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        progressBar.animate()
                .alpha(0f)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

}
