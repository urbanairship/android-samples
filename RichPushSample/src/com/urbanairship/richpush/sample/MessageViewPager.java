/*
 * Copyright 2012 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.urbanairship.richpush.RichPushMessage;

import java.util.List;

public class MessageViewPager extends ViewPager {

    MessageFragmentAdapter adapter;
    ViewPagerTouchListener listener;

    public MessageViewPager(Context context) {
        this(context, null, ((FragmentActivity)context).getSupportFragmentManager());
    }

    public MessageViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, ((FragmentActivity) context).getSupportFragmentManager());
    }

    public MessageViewPager(Context context, AttributeSet attrs, FragmentManager manager) {
        super(context, attrs);
        this.adapter = new MessageFragmentAdapter(manager);
        this.setAdapter(this.adapter);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.listener != null) this.listener.onViewPagerTouch();
        return super.onInterceptTouchEvent(event);
    }

    public void setViewPagerTouchListener(ViewPagerTouchListener listener) {
        this.listener = listener;
    }

    public void clearViewPagerTouchListener() {
        this.listener = null;
    }


    public void setMessages(List<RichPushMessage> messages) {
        this.adapter.setRichPushMessages(messages);
        this.adapter.notifyDataSetChanged();
    }

    // interfaces

    public static interface ViewPagerTouchListener {
        public void onViewPagerTouch();
    }

}
