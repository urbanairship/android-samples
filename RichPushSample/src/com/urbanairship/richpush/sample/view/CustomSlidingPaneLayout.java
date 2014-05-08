/*
Copyright 2009-2014 Urban Airship Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.urbanairship.richpush.sample.view;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

/**
 * Sliding pane layout that only allows
 * sliding if the slide gesture originates from a gutter
 * 
 */
public class CustomSlidingPaneLayout extends SlidingPaneLayout {

    private static int MEDIUM_DENSITY_SCREEN_DPI = 160;
    public static int DEFAULT_GUTTER_SIZE_DP = 32;

    float gutter;
    boolean ignoreEvents = false;

    public CustomSlidingPaneLayout(Context context) {
        this(context, null);
    }

    public CustomSlidingPaneLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSlidingPaneLayout(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        gutter = DEFAULT_GUTTER_SIZE_DP * (metrics.densityDpi / (float) MEDIUM_DENSITY_SCREEN_DPI);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);

        switch(action) {
        case MotionEvent.ACTION_DOWN:
            if (ev.getX() > gutter && !isOpen()) {
                ignoreEvents = true;
                return false;
            }
            break;

        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            if (ignoreEvents) {
                ignoreEvents = false;
                return false;
            }
            break;
        }

        return !ignoreEvents && super.onInterceptTouchEvent(ev);
    }
}
