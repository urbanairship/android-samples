/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.urbanairship.richpush.sample;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.*;
import android.view.accessibility.AccessibilityEvent;
import com.urbanairship.Logger;

public class AllDirectionSlidingDrawer extends ViewGroup {

    public static final int DIRECTION_TTB = 0;
    public static final int DIRECTION_RTL = 1;
    public static final int DIRECTION_BTT = 2;
    public static final int DIRECTION_LTR = 3;

    private static final int TAP_THRESHOLD = 6;
    private static final float MAXIMUM_TAP_VELOCITY = 100.0f;
    private static final float MAXIMUM_MINOR_VELOCITY = 150.0f;
    private static final float MAXIMUM_MAJOR_VELOCITY = 200.0f;
    private static final float MAXIMUM_ACCELERATION = 2000.0f;
    private static final int VELOCITY_UNITS = 1000;
    private static final int MSG_ANIMATE = 1000;
    private static final int ANIMATION_FRAME_DURATION = 1000 / 60;

    private static final int EXPANDED_FULL_OPEN = -10001;
    private static final int COLLAPSED_FULL_CLOSED = -10002;

    private final int mHandleId;
    private final int mContentId;

    private View mHandle;
    private View mContent;

    private final Rect mFrame = new Rect();
    private final Rect mInvalidate = new Rect();
    private boolean mTracking;
    private boolean mLocked;

    private VelocityTracker mVelocityTracker;

    private boolean mFlipped;
    private boolean mVertical;
    private boolean mExpanded;
    private int mBottomOffset;
    private int mTopOffset;
    private int mHandleHeight;
    private int mHandleWidth;

    private OnDrawerOpenListener mOnDrawerOpenListener;
    private OnDrawerCloseListener mOnDrawerCloseListener;
    private OnDrawerScrollListener mOnDrawerScrollListener;

    private final Handler mHandler = new SlidingHandler();
    private float mAnimatedAcceleration;
    private float mAnimatedVelocity;
    private float mAnimationPosition;
    private long mAnimationLastTime;
    private long mCurrentAnimationTime;
    private int mTouchDelta;
    private boolean mAnimating;
    private boolean mAllowSingleTap;
    private boolean mAnimateOnClick;

    private final int mTapThreshold;
    private final int mMaximumTapVelocity;
    private final int mMaximumMinorVelocity;
    private final int mMaximumMajorVelocity;
    private final int mMaximumAcceleration;
    private final int mVelocityUnits;

    /**
     * Callback invoked when the drawer is opened.
     */
    public static interface OnDrawerOpenListener {
        /**
         * Invoked when the drawer becomes fully open.
         */
        public void onDrawerOpened();
    }

    /**
     * Callback invoked when the drawer is closed.
     */
    public static interface OnDrawerCloseListener {
        /**
         * Invoked when the drawer becomes fully closed.
         */
        public void onDrawerClosed();
    }

    /**
     * Callback invoked when the drawer is scrolled.
     */
    public static interface OnDrawerScrollListener {
        /**
         * Invoked when the user starts dragging/flinging the drawer's handle.
         */
        public void onScrollStarted();

        /**
         * Invoked when the user stops dragging/flinging the drawer's handle.
         */
        public void onScrollEnded();
    }

    /**
     * Creates a new AllDirectionSlidingDrawer from a specified set of attributes defined in XML.
     *
     * @param context The application's environment.
     * @param attrs The attributes defined in XML.
     */
    public AllDirectionSlidingDrawer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Creates a new AllDirectionSlidingDrawer a specified set of attributes defined in XML.
     *
     * @param context The application's environment.
     * @param attrs The attributes defined in XML.
     * @param defStyle The style to apply to this widget.
     */
    public AllDirectionSlidingDrawer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AllDirectionSlidingDrawer, defStyle, 0);

        int orientation = a.getInt(R.styleable.AllDirectionSlidingDrawer_direction, DIRECTION_LTR);
        mFlipped = (orientation == DIRECTION_TTB || orientation == DIRECTION_LTR);
        mVertical = (orientation == DIRECTION_BTT || orientation == DIRECTION_TTB);
        mBottomOffset = (int) a.getDimension(R.styleable.AllDirectionSlidingDrawer_bottomOffset, 0.0f);
        mTopOffset = (int) a.getDimension(R.styleable.AllDirectionSlidingDrawer_topOffset, 0.0f);
        mAllowSingleTap = a.getBoolean(R.styleable.AllDirectionSlidingDrawer_allowSingleTap, true);
        mAnimateOnClick = a.getBoolean(R.styleable.AllDirectionSlidingDrawer_animateOnClick, true);

        Logger.debug("mBottomOffset: " + mBottomOffset);
        Logger.debug("mTopOffset: " + mTopOffset);

        int handleId = a.getResourceId(R.styleable.AllDirectionSlidingDrawer_handle, 0);
        if (handleId == 0) {
            throw new IllegalArgumentException("The handle attribute is required and must refer "
                    + "to a valid child.");
        }

        int contentId = a.getResourceId(R.styleable.AllDirectionSlidingDrawer_content, 0);
        if (contentId == 0) {
            throw new IllegalArgumentException("The content attribute is required and must refer "
                    + "to a valid child.");
        }

        if (handleId == contentId) {
            throw new IllegalArgumentException("The content and handle attributes must refer "
                    + "to different children.");
        }

        mHandleId = handleId;
        mContentId = contentId;

        final float density = getResources().getDisplayMetrics().density;
        int flipInt = mFlipped ? -1 : 1;
        mTapThreshold = (int) (TAP_THRESHOLD * density + 0.5f);
        mMaximumTapVelocity = (int) (MAXIMUM_TAP_VELOCITY * density + 0.5f);
        mMaximumMinorVelocity = flipInt * (int) (MAXIMUM_MINOR_VELOCITY * density + 0.5f);
        mMaximumMajorVelocity = flipInt * (int) (MAXIMUM_MAJOR_VELOCITY * density + 0.5f);
        mMaximumAcceleration = flipInt * (int) (MAXIMUM_ACCELERATION * density + 0.5f);
        mVelocityUnits = (int) (VELOCITY_UNITS * density + 0.5f);

        a.recycle();

        setAlwaysDrawnWithCacheEnabled(false);
    }

    @Override
    protected void onFinishInflate() {
        mHandle = findViewById(mHandleId);
        if (mHandle == null) {
            throw new IllegalArgumentException("The handle attribute is must refer to an"
                    + " existing child.");
        }
        mHandle.setOnClickListener(new DrawerToggler());

        mContent = findViewById(mContentId);
        if (mContent == null) {
            throw new IllegalArgumentException("The content attribute is must refer to an"
                    + " existing child.");
        }
        mContent.setVisibility(View.GONE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("AllDirectionSlidingDrawer cannot have UNSPECIFIED dimensions");
        }

        final View handle = mHandle;
        measureChild(handle, widthMeasureSpec, heightMeasureSpec);

        if (mVertical) {
            int height = heightSpecSize - handle.getMeasuredHeight() - mTopOffset;
            mContent.measure(MeasureSpec.makeMeasureSpec(widthSpecSize, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        } else {
            int width = widthSpecSize - handle.getMeasuredWidth() - mTopOffset;
            mContent.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(heightSpecSize, MeasureSpec.EXACTLY));
        }

        Logger.debug("onMeasure widthSpecSize " + widthSpecSize);
        Logger.debug("onMeasure heightSpecSize " + widthSpecSize);
        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        final long drawingTime = getDrawingTime();
        final View handle = mHandle;
        final boolean isVertical = mVertical;

        drawChild(canvas, handle, drawingTime);

        if (mTracking || mAnimating) {
            final Bitmap cache = mContent.getDrawingCache();
            if (cache != null) {
                if (isVertical) {
                    int floatTop = mFlipped ? handle.getTop() - (this.getBottom() - this.getTop()) + mHandleHeight :
                            handle.getBottom();
                    canvas.drawBitmap(cache, 0, floatTop, null);
                } else {
                    int floatLeft = mFlipped ? handle.getLeft() - cache.getWidth() : handle.getRight();
                    canvas.drawBitmap(cache, floatLeft, 0, null);
                }
            } else {
                canvas.save();
                // mFlipped == true and isVertical == true, dx = 0
                // mFlipped == false and isVertical == true, dx = 0
                // mFlipped == true and isVertical == false, dy = 0
                // mFlipped == false and isVertical == false, dy = 0
                int dx = 0;
                int dy = 0;
                if (!isVertical) {
                    dx = mFlipped ? handle.getLeft() - mTopOffset - mContent.getMeasuredWidth() :
                            handle.getLeft() - mTopOffset;
                } else {
                    dy = mFlipped ? handle.getTop() - mTopOffset - mContent.getMeasuredHeight() :
                            handle.getTop() - mTopOffset;
                }
                Logger.debug("dx in dispatchDraw is " + dx);
                Logger.debug("dy in dispatchDraw is " + dy);
                canvas.translate(dx, dy);
                drawChild(canvas, mContent, drawingTime);
                canvas.restore();
            }
        } else if (mExpanded) {
            drawChild(canvas, mContent, drawingTime);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mTracking) {
            return;
        }

        final int width = r - l;
        final int height = b - t;

        Logger.debug("width in onLayout is " + width);
        Logger.debug("height in onLayout is " + height);

        final View handle = mHandle;

        int childWidth = handle.getMeasuredWidth();
        int childHeight = handle.getMeasuredHeight();

        Logger.debug("childWidth in onLayout is " + childWidth);
        Logger.debug("childHeight in onLayout is " + childHeight);

        int childLeft;
        int childTop;

        final View content = mContent;
        if ( mVertical ) {
            childLeft = ( width - childWidth ) / 2;
            Logger.debug("childLeft in onLayout is " + childLeft);
            // if mFlipped == false and mExpanded == true, childTop = mTopOffset
            // if mFlipped == false and mExpanded == false, childTop = height - childHeight + mBottomOffset
            // if mFlipped == true and mExpanded == true, childTop = height - childHeight - mBottomOffset
            // if mFlipped == true and mExpanded == false, childTop = mTopOffset
            if (mFlipped) {
                childTop = mExpanded ? height - mBottomOffset - childHeight : mTopOffset;
                Logger.debug("childTop in onLayout with mFlipped is " + childTop);
                content.layout( 0, mTopOffset, content.getMeasuredWidth(), mTopOffset + content.getMeasuredHeight() );
            } else {
                childTop = mExpanded ? mTopOffset : height - childHeight + mBottomOffset;
                Logger.debug("childTop in onLayout without mFlipped is " + childTop);
                content.layout( 0, mTopOffset + childHeight, content.getMeasuredWidth(), mTopOffset + childHeight + content.getMeasuredHeight() );
            }
        } else {
            childTop = ( height - childHeight ) / 2;
            Logger.debug("childTop in onLayout is " + childTop);
            // if mFlipped == false and mExpanded == true, childLeft = mTopOffset
            // if mFlipped == false and mExpanded == false, childLeft = width - childWidth + mBottomOffset
            // if mFlipped == true and mExpanded == true, childLeft = width - childWidth - mBottomOffset
            // if mFlipped == true and mExpanded == false, childLeft = mTopOffset
            if (mFlipped) {
                childLeft = mExpanded ? width - mBottomOffset - childWidth : mTopOffset;
                Logger.debug("childLeft in onLayout with mFlipped is " + childLeft);
                content.layout( mTopOffset, 0, mTopOffset + content.getMeasuredWidth(), content.getMeasuredHeight() );
            } else {
                childLeft = mExpanded ? mTopOffset : width - childWidth + mBottomOffset;
                Logger.debug("childLeft in onLayout without mFlipped is " + childLeft);
                content.layout( mTopOffset + childWidth, 0, mTopOffset + childWidth + content.getMeasuredWidth(), content.getMeasuredHeight() );
            }
        }

        handle.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        mHandleHeight = handle.getHeight();
        mHandleWidth = handle.getWidth();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mLocked) {
            return false;
        }

        final int action = event.getAction();

        float x = event.getX();
        float y = event.getY();

        final Rect frame = mFrame;
        final View handle = mHandle;

        handle.getHitRect(frame);
        if (!mTracking && !frame.contains((int) x, (int) y)) {
            return false;
        }

        if (action == MotionEvent.ACTION_DOWN) {
            mTracking = true;

            handle.setPressed(true);
            // Must be called before prepareTracking()
            prepareContent();

            // Must be called after prepareContent()
            if (mOnDrawerScrollListener != null) {
                mOnDrawerScrollListener.onScrollStarted();
            }

            if (mVertical) {
                final int top = mHandle.getTop();
                mTouchDelta = (int) y - top;
                prepareTracking(top);
            } else {
                final int left = mHandle.getLeft();
                mTouchDelta = (int) x - left;
                prepareTracking(left);
            }
            mVelocityTracker.addMovement(event);
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLocked) {
            return true;
        }

        if (mTracking) {
            mVelocityTracker.addMovement(event);
            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    moveHandle((int) (mVertical ? event.getY() : event.getX()) - mTouchDelta);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(mVelocityUnits);

                    float yVelocity = velocityTracker.getYVelocity();
                    float xVelocity = velocityTracker.getXVelocity();
                    boolean negative;

                    final boolean vertical = mVertical;
                    if (vertical) {
                        negative = yVelocity < 0;
                        if (xVelocity < 0) {
                            xVelocity = -xVelocity;
                        }
                        if ((!mFlipped && xVelocity > mMaximumMinorVelocity) ||
                                (mFlipped && xVelocity < mMaximumMinorVelocity)) {
                            xVelocity = mMaximumMinorVelocity;
                        }
                        Logger.debug("xVelocity in onTouchEvent is " + xVelocity);
                    } else {
                        negative = xVelocity < 0;
                        if (yVelocity < 0) {
                            yVelocity = -yVelocity;
                        }
                        if ((!mFlipped && yVelocity > mMaximumMinorVelocity) ||
                                (mFlipped && yVelocity < mMaximumMinorVelocity)) {
                            yVelocity = mMaximumMinorVelocity;
                        }
                        Logger.debug("yVelocity in onTouchEvent is " + yVelocity);
                    }

                    float velocity = (float) Math.hypot(xVelocity, yVelocity);
                    if (negative) {
                        velocity = -velocity;
                    }

                    final int top = mHandle.getTop();
                    final int left = mHandle.getLeft();
                    final int bottom = mHandle.getBottom();
                    final int right = mHandle.getRight();

                    if (Math.abs( velocity ) < mMaximumTapVelocity) {
                        boolean c1;
                        boolean c2;
                        boolean c3;
                        boolean c4;

                        if (mFlipped) {
                            c1 = ( mExpanded && (getBottom() - bottom) < mTapThreshold + mBottomOffset );
                            c2 = ( !mExpanded && top < mTopOffset + mHandleHeight - mTapThreshold );
                            c3 = ( mExpanded && (getRight() - right) < mTapThreshold + mBottomOffset );
                            c4 = ( !mExpanded && left > mTopOffset + mHandleWidth + mTapThreshold );
                        } else {
                            c1 = ( mExpanded && top < mTapThreshold + mTopOffset );
                            c2 = ( !mExpanded && top > mBottomOffset + getBottom() - getTop() - mHandleHeight - mTapThreshold );
                            c3 = ( mExpanded && left < mTapThreshold + mTopOffset );
                            c4 = ( !mExpanded && left > mBottomOffset + getRight() - getLeft() - mHandleWidth - mTapThreshold );
                        }

                        if ( vertical ? c1 || c2 : c3 || c4 ) {

                            if ( mAllowSingleTap ) {
                                playSoundEffect( SoundEffectConstants.CLICK );

                                if ( mExpanded ) {
                                    animateClose( vertical ? top : left);
                                } else {
                                    animateOpen( vertical ? top : left);
                                }
                            } else {
                                performFling( vertical ? top : left, velocity, false );
                            }
                        } else {
                            performFling( vertical ? top : left, velocity, false );
                        }
                    } else {
                        performFling( vertical ? top : left, velocity, false );
                    }
                }
                break;

            }
        }

        return mTracking || mAnimating || super.onTouchEvent(event);
    }

    private void animateClose(int position) {
        prepareTracking(position);
        performFling(position, mMaximumAcceleration, true);
    }

    private void animateOpen(int position) {
        prepareTracking(position);
        performFling(position, -mMaximumAcceleration, true);
    }

    private void performFling(int position, float velocity, boolean always) {
        mAnimationPosition = position;
        mAnimatedVelocity = velocity;

        boolean c1 = mFlipped ? velocity < mMaximumMajorVelocity : velocity > mMaximumMajorVelocity;
        boolean c2;
        boolean c3 = mFlipped ? velocity < -mMaximumMajorVelocity : velocity > -mMaximumMajorVelocity;

        if (mExpanded) {
            int bottom = mVertical ? getBottom() : getRight();
            int handleHeight = mVertical ? mHandleHeight : mHandleWidth;

            c2 = mFlipped ? (bottom - (position + handleHeight)) + mBottomOffset > handleHeight :
                    position > mTopOffset + handleHeight;

            if (always || (c1 || (c2 && c3))) {
                // We are expanded, but they didn't move sufficiently to cause
                // us to retract.  Animate back to the expanded position.
                mAnimatedAcceleration = mMaximumAcceleration;
                if ((mFlipped && velocity > 0) || (!mFlipped && velocity < 0)) {
                    mAnimatedVelocity = 0;
                }
            } else {
                // We are expanded and are now going to animate away.
                mAnimatedAcceleration = -mMaximumAcceleration;
                if ((mFlipped && velocity < 0) || (!mFlipped && velocity > 0)) {
                    mAnimatedVelocity = 0;
                }
            }
        } else {
            c2 = mFlipped ? (position < (mVertical ? getHeight() : getWidth())/2) :
                    (position > (mVertical ? getHeight() : getWidth())/2);

            if (!always && (c1 || (c2 && c3))) {
                // We are collapsed, and they moved enough to allow us to expand.
                mAnimatedAcceleration = mMaximumAcceleration;
                if ((!mFlipped && velocity < 0) || (mFlipped && velocity > 0)) {
                    mAnimatedVelocity = 0;
                }
            } else {
                // We are collapsed, but they didn't move sufficiently to cause
                // us to retract.  Animate back to the collapsed position.
                mAnimatedAcceleration = -mMaximumAcceleration;
                if ((!mFlipped && velocity > 0) || (mFlipped && velocity < 0)) {
                    mAnimatedVelocity = 0;
                }
            }
        }

        long now = SystemClock.uptimeMillis();
        mAnimationLastTime = now;
        mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
        mAnimating = true;
        mHandler.removeMessages(MSG_ANIMATE);
        mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
        stopTracking();
    }

    private void prepareTracking(int position) {
        mTracking = true;
        mVelocityTracker = VelocityTracker.obtain();
        boolean opening = !mExpanded;
        if (opening) {
            mAnimatedAcceleration = mMaximumAcceleration;
            mAnimatedVelocity = mMaximumMajorVelocity;
            if (mFlipped) {
                mAnimationPosition = mTopOffset;
            } else {
                mAnimationPosition = mBottomOffset +
                        (mVertical ? getHeight() - mHandleHeight : getWidth() - mHandleWidth);
            }
            moveHandle((int) mAnimationPosition);
            mAnimating = true;
            mHandler.removeMessages(MSG_ANIMATE);
            long now = SystemClock.uptimeMillis();
            mAnimationLastTime = now;
            mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
            mAnimating = true;
        } else {
            if (mAnimating) {
                mAnimating = false;
                mHandler.removeMessages(MSG_ANIMATE);
            }
            moveHandle(position);
        }
    }

    private void moveHandle(int position) {
        final View handle = mHandle;

        if (mVertical) {
            if (position == EXPANDED_FULL_OPEN) {
                if (mFlipped) {
                    handle.offsetTopAndBottom(mBottomOffset + getBottom() - getTop() - mHandleHeight);
                } else {
                    handle.offsetTopAndBottom(mTopOffset - handle.getTop());
                }
                invalidate();
            } else if (position == COLLAPSED_FULL_CLOSED) {
                if (mFlipped) {
                    handle.offsetTopAndBottom(mTopOffset - handle.getTop());
                } else {
                    handle.offsetTopAndBottom(mBottomOffset + getBottom() - getTop() - mHandleHeight - handle.getTop());
                }
                invalidate();
            } else {
                final int top = handle.getTop();
                int deltaY = position - top;
                if (position < mTopOffset) {
                    deltaY = mTopOffset - top;
                } else if (deltaY > mBottomOffset + getBottom() - getTop() - mHandleHeight - top) {
                    deltaY = mBottomOffset + getBottom() - getTop() - mHandleHeight - top;
                }
                handle.offsetTopAndBottom(deltaY);

                final Rect frame = mFrame;
                final Rect region = mInvalidate;

                handle.getHitRect(frame);
                region.set(frame);

                region.union(frame.left, frame.top - deltaY, frame.right, frame.bottom - deltaY);
                region.union(0, frame.bottom - deltaY, getWidth(),
                        frame.bottom - deltaY + mContent.getHeight());

                invalidate(region);
            }
        } else {
            if (position == EXPANDED_FULL_OPEN) {
                if (mFlipped) {
                    handle.offsetLeftAndRight(mBottomOffset + getRight() - getLeft() - mHandleWidth);
                } else {
                    handle.offsetLeftAndRight(mTopOffset - handle.getLeft());
                }
                invalidate();
            } else if (position == COLLAPSED_FULL_CLOSED) {
                if (mFlipped) {
                    handle.offsetLeftAndRight(mTopOffset - handle.getLeft());
                } else {
                    handle.offsetLeftAndRight(mBottomOffset + getRight() - getLeft() -
                            mHandleWidth - handle.getLeft());
                }
                invalidate();
            } else {
                final int left = handle.getLeft();
                int deltaX = position - left;
                if (position < mTopOffset) {
                    deltaX = mTopOffset - left;
                } else if (deltaX > mBottomOffset + getRight() - getLeft() - mHandleWidth - left) {
                    deltaX = mBottomOffset + getRight() - getLeft() - mHandleWidth - left;
                }
                handle.offsetLeftAndRight(deltaX);

                final Rect frame = mFrame;
                final Rect region = mInvalidate;

                handle.getHitRect(frame);
                region.set(frame);

                region.union(frame.left - deltaX, frame.top, frame.right - deltaX, frame.bottom);
                region.union(frame.right - deltaX, 0,
                        frame.right - deltaX + mContent.getWidth(), getHeight());

                invalidate(region);
            }
        }
    }

    private void prepareContent() {
        if (mAnimating) {
            return;
        }

        // Something changed in the content, we need to honor the layout request
        // before creating the cached bitmap
        final View content = mContent;
        if (content.isLayoutRequested()) {
            if (mVertical) {
                final int childHeight = mHandleHeight;
                int height = getBottom() - getTop() - childHeight - mTopOffset;
                content.measure(MeasureSpec.makeMeasureSpec(getRight() - getLeft() , MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                if (mFlipped) {
                    content.layout(0, mTopOffset, content.getMeasuredWidth(), mTopOffset + content.getMeasuredHeight());
                } else {
                    content.layout(0, mTopOffset + childHeight, content.getMeasuredWidth(),
                            mTopOffset + childHeight + content.getMeasuredHeight());
                }
            } else {
                final int childWidth = mHandle.getWidth();
                int width = getRight() - getLeft() - childWidth - mTopOffset;
                content.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(getBottom() - getTop(), MeasureSpec.EXACTLY));
                if (mFlipped) {
                    content.layout(mTopOffset, 0, mTopOffset + content.getMeasuredWidth(), content.getMeasuredHeight());
                } else {
                    content.layout(childWidth + mTopOffset, 0,
                            mTopOffset + childWidth + content.getMeasuredWidth(),
                            content.getMeasuredHeight());
                }
            }
        }
        // Try only once... we should really loop but it's not a big deal
        // if the draw was cancelled, it will only be temporary anyway
        content.getViewTreeObserver().dispatchOnPreDraw();
        if (!content.isHardwareAccelerated()) content.buildDrawingCache();

        content.setVisibility(View.GONE);
    }

    private void stopTracking() {
        mHandle.setPressed(false);
        mTracking = false;

        if (mOnDrawerScrollListener != null) {
            mOnDrawerScrollListener.onScrollEnded();
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void doAnimation() {
        if (mAnimating) {
            incrementAnimation();
            if (mFlipped) {
                if ( mAnimationPosition < mTopOffset ) {
                    mAnimating = false;
                    closeDrawer();
                } else if ( mAnimationPosition >= mTopOffset + ( mVertical ? getHeight() : getWidth() ) - 1 ) {
                    mAnimating = false;
                    openDrawer();
                } else {
                    moveHandle( (int)mAnimationPosition );
                    mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
                    mHandler.sendMessageAtTime( mHandler.obtainMessage( MSG_ANIMATE ), mCurrentAnimationTime );
                }
            } else {
                if (mAnimationPosition >= mBottomOffset + (mVertical ? getHeight() : getWidth()) - 1) {
                    mAnimating = false;
                    closeDrawer();
                } else if (mAnimationPosition < mTopOffset) {
                    mAnimating = false;
                    openDrawer();
                } else {
                    moveHandle((int) mAnimationPosition);
                    mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
                    mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE),
                            mCurrentAnimationTime);
                }
            }
        }
    }

    private void incrementAnimation() {
        long now = SystemClock.uptimeMillis();
        float t = (now - mAnimationLastTime) / 1000.0f;                   // ms -> s
        final float position = mAnimationPosition;
        final float v = mAnimatedVelocity;                                // px/s
        final float a = mAnimatedAcceleration;                            // px/s/s
        mAnimationPosition = position + (v * t) + (0.5f * a * t * t);     // px
        mAnimatedVelocity = v + (a * t);                                  // px/s
        mAnimationLastTime = now;                                         // ms
    }

    /**
     * Toggles the drawer open and close. Takes effect immediately.
     *
     * @see #open()
     * @see #close()
     * @see #animateClose()
     * @see #animateOpen()
     * @see #animateToggle()
     */
    public void toggle() {
        if (!mExpanded) {
            openDrawer();
        } else {
            closeDrawer();
        }
        invalidate();
        requestLayout();
    }

    /**
     * Toggles the drawer open and close with an animation.
     *
     * @see #open()
     * @see #close()
     * @see #animateClose()
     * @see #animateOpen()
     * @see #toggle()
     */
    public void animateToggle() {
        if (!mExpanded) {
            animateOpen();
        } else {
            animateClose();
        }
    }

    /**
     * Opens the drawer immediately.
     *
     * @see #toggle()
     * @see #close()
     * @see #animateOpen()
     */
    public void open() {
        openDrawer();
        invalidate();
        requestLayout();

        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    /**
     * Closes the drawer immediately.
     *
     * @see #toggle()
     * @see #open()
     * @see #animateClose()
     */
    public void close() {
        closeDrawer();
        invalidate();
        requestLayout();
    }

    /**
     * Closes the drawer with an animation.
     *
     * @see #close()
     * @see #open()
     * @see #animateOpen()
     * @see #animateToggle()
     * @see #toggle()
     */
    public void animateClose() {
        prepareContent();
        final OnDrawerScrollListener scrollListener = mOnDrawerScrollListener;
        if (scrollListener != null) {
            scrollListener.onScrollStarted();
        }
        animateClose(mVertical ? mHandle.getTop() : mHandle.getLeft());

        if (scrollListener != null) {
            scrollListener.onScrollEnded();
        }
    }

    /**
     * Opens the drawer with an animation.
     *
     * @see #close()
     * @see #open()
     * @see #animateClose()
     * @see #animateToggle()
     * @see #toggle()
     */
    public void animateOpen() {
        prepareContent();
        final OnDrawerScrollListener scrollListener = mOnDrawerScrollListener;
        if (scrollListener != null) {
            scrollListener.onScrollStarted();
        }
        animateOpen(mVertical ? mHandle.getTop() : mHandle.getLeft());

        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);

        if (scrollListener != null) {
            scrollListener.onScrollEnded();
        }
    }

    private void closeDrawer() {
        moveHandle(COLLAPSED_FULL_CLOSED);
        mContent.setVisibility(View.GONE);
        mContent.destroyDrawingCache();

        if (!mExpanded) {
            return;
        }

        mExpanded = false;
        if (mOnDrawerCloseListener != null) {
            mOnDrawerCloseListener.onDrawerClosed();
        }
    }

    private void openDrawer() {
        moveHandle(EXPANDED_FULL_OPEN);
        mContent.setVisibility(View.VISIBLE);

        if (mExpanded) {
            return;
        }

        mExpanded = true;

        if (mOnDrawerOpenListener != null) {
            mOnDrawerOpenListener.onDrawerOpened();
        }
    }

    /**
     * Sets the listener that receives a notification when the drawer becomes open.
     *
     * @param onDrawerOpenListener The listener to be notified when the drawer is opened.
     */
    public void setOnDrawerOpenListener(OnDrawerOpenListener onDrawerOpenListener) {
        mOnDrawerOpenListener = onDrawerOpenListener;
    }

    /**
     * Sets the listener that receives a notification when the drawer becomes close.
     *
     * @param onDrawerCloseListener The listener to be notified when the drawer is closed.
     */
    public void setOnDrawerCloseListener(OnDrawerCloseListener onDrawerCloseListener) {
        mOnDrawerCloseListener = onDrawerCloseListener;
    }

    /**
     * Sets the listener that receives a notification when the drawer starts or ends
     * a scroll. A fling is considered as a scroll. A fling will also trigger a
     * drawer opened or drawer closed event.
     *
     * @param onDrawerScrollListener The listener to be notified when scrolling
     *        starts or stops.
     */
    public void setOnDrawerScrollListener(OnDrawerScrollListener onDrawerScrollListener) {
        mOnDrawerScrollListener = onDrawerScrollListener;
    }

    /**
     * Returns the handle of the drawer.
     *
     * @return The View reprenseting the handle of the drawer, identified by
     *         the "handle" id in XML.
     */
    public View getHandle() {
        return mHandle;
    }

    /**
     * Returns the content of the drawer.
     *
     * @return The View reprenseting the content of the drawer, identified by
     *         the "content" id in XML.
     */
    public View getContent() {
        return mContent;
    }

    /**
     * Unlocks the AllDirectionSlidingDrawer so that touch events are processed.
     *
     * @see #lock()
     */
    public void unlock() {
        mLocked = false;
    }

    /**
     * Locks the AllDirectionSlidingDrawer so that touch events are ignores.
     *
     * @see #unlock()
     */
    public void lock() {
        mLocked = true;
    }

    /**
     * Indicates whether the drawer is currently fully opened.
     *
     * @return True if the drawer is opened, false otherwise.
     */
    public boolean isOpened() {
        return mExpanded;
    }

    /**
     * Indicates whether the drawer is scrolling or flinging.
     *
     * @return True if the drawer is scroller or flinging, false otherwise.
     */
    public boolean isMoving() {
        return mTracking || mAnimating;
    }

    private class DrawerToggler implements OnClickListener {
        public void onClick(View v) {
            if (mLocked) {
                return;
            }
            // mAllowSingleTap isn't relevant here; you're *always*
            // allowed to open/close the drawer by clicking with the
            // trackball.

            if (mAnimateOnClick) {
                animateToggle();
            } else {
                toggle();
            }
        }
    }

    private class SlidingHandler extends Handler {
        public void handleMessage(Message m) {
            switch (m.what) {
                case MSG_ANIMATE:
                    doAnimation();
                    break;
            }
        }
    }
}

