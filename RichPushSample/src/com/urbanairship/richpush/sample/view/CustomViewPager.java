/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * A ViewPager that allows touch events to be
 * enabled and disabled
 * 
 */
public class CustomViewPager extends ViewPager {

    private boolean isTouchEnabled = true;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return isTouchEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return isTouchEnabled && super.onInterceptTouchEvent(event);
    }

    /**
     * Sets touch to be disabled or enabled
     * @param isTouchEnabled <code>true</code> to enable touch, <code>false</code> to disable
     */
    public void enableTouchEvents(boolean isTouchEnabled) {
        this.isTouchEnabled = isTouchEnabled;
    }
}