package com.urbanairship.richpush.sample;

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
