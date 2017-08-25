package forpdateam.ru.forpda.views;

/**
 * Created by radiationx on 25.08.17.
 */
/* -*- Mode: Java; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */


import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.VisibleForTesting;
import android.support.v4.os.TraceCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.webkit.WebView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;


public class NestedGeckoView extends WebView implements NestedScrollingChild {
    static final String TAG = "RecyclerView";

    static final boolean DEBUG = false;

    static final boolean VERBOSE_TRACING = false;

    private static final int[] NESTED_SCROLLING_ATTRS = {16843830};

    private static final int[] CLIP_TO_PADDING_ATTR = {android.R.attr.clipToPadding};


    static final boolean FORCE_INVALIDATE_DISPLAY_LIST = Build.VERSION.SDK_INT == 18
            || Build.VERSION.SDK_INT == 19 || Build.VERSION.SDK_INT == 20;

    static final boolean ALLOW_SIZE_IN_UNSPECIFIED_SPEC = Build.VERSION.SDK_INT >= 23;

    static final boolean POST_UPDATES_ON_ANIMATION = Build.VERSION.SDK_INT >= 16;


    private static final boolean ALLOW_THREAD_GAP_WORK = Build.VERSION.SDK_INT >= 21;

    private static final boolean FORCE_ABS_FOCUS_SEARCH_DIRECTION = Build.VERSION.SDK_INT <= 15;


    private static final boolean IGNORE_DETACHED_FOCUSED_CHILD = Build.VERSION.SDK_INT <= 15;

    static final boolean DISPATCH_TEMP_DETACH = false;
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    public static final int NO_POSITION = -1;
    public static final long NO_ID = -1;
    public static final int INVALID_TYPE = -1;


    public static final int TOUCH_SLOP_DEFAULT = 0;

    public static final int TOUCH_SLOP_PAGING = 1;

    static final int MAX_SCROLL_DURATION = 2000;


    static final String TRACE_SCROLL_TAG = "RV Scroll";


    private static final String TRACE_ON_LAYOUT_TAG = "RV OnLayout";


    private static final String TRACE_ON_DATA_SET_CHANGE_LAYOUT_TAG = "RV FullInvalidate";

    private static final String TRACE_HANDLE_ADAPTER_UPDATES_TAG = "RV PartialInvalidate";


    static final String TRACE_BIND_VIEW_TAG = "RV OnBindView";

    static final String TRACE_PREFETCH_TAG = "RV Prefetch";


    static final String TRACE_NESTED_PREFETCH_TAG = "RV Nested Prefetch";

    static final String TRACE_CREATE_VIEW_TAG = "RV CreateView";
    private static final Class<?>[] LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE =
            new Class[]{Context.class, AttributeSet.class, int.class, int.class};

    boolean mClipToPadding;


    final Rect mTempRect = new Rect();
    private final Rect mTempRect2 = new Rect();
    final RectF mTempRectF = new RectF();
    //RecyclerView.Adapter mAdapter;
    @VisibleForTesting
    //RecyclerView.LayoutManager mLayout;
            //RecyclerView.RecyclerListener mRecyclerListener;
            //final ArrayList<RecyclerView.ItemDecoration> mItemDecorations = new ArrayList<>();
            //private final ArrayList<RecyclerView.OnItemTouchListener> mOnItemTouchListeners = new ArrayList<>();
            //private RecyclerView.OnItemTouchListener mActiveOnItemTouchListener;
            boolean mIsAttached;
    boolean mHasFixedSize;
    @VisibleForTesting
    boolean mFirstLayoutComplete;

    // Counting lock to control whether we should ignore requestLayout calls from children or not.
    private int mEatRequestLayout = 0;

    boolean mLayoutRequestEaten;
    //boolean mLayoutFrozen;
    private boolean mIgnoreMotionEventTillDown;

    // binary OR of change events that were eaten during a layout or scroll.
    private int mEatenAccessibilityChangeFlags;
    boolean mAdapterUpdateDuringMeasure;


    /*
    * MY
    * */
    private int mDispatchScrollCounter = 0;

    private static final int INVALID_POINTER = -1;

    public static final int SCROLL_STATE_IDLE = 0;

    public static final int SCROLL_STATE_DRAGGING = 1;

    public static final int SCROLL_STATE_SETTLING = 2;

    static final long FOREVER_NS = Long.MAX_VALUE;

    // Touch/scrolling handling

    private int mScrollState = SCROLL_STATE_IDLE;
    private int mScrollPointerId = INVALID_POINTER;
    private VelocityTracker mVelocityTracker;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private int mLastTouchX;
    private int mLastTouchY;
    private int mTouchSlop;
    private RecyclerView.OnFlingListener mOnFlingListener;
    private final int mMinFlingVelocity;
    private final int mMaxFlingVelocity;
    // This value is used when handling generic motion events.
    private float mScrollFactor = Float.MIN_VALUE;
    private boolean mPreserveFocusAfterLayout = true;
    private NestedScrollingChildHelper mScrollingChildHelper;
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private final int[] mNestedOffsets = new int[2];


    private NestedScrollingChildHelper mChildHelper;

    public NestedGeckoView(Context context) {
        this(context, null);
    }

    public NestedGeckoView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    public NestedGeckoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        final ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        setWillNotDraw(getOverScrollMode() == View.OVER_SCROLL_NEVER);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        Log.d("SUKA", "onTouchEvent");
        /*if (mLayoutFrozen || mIgnoreMotionEventTillDown) {
            return false;
        }*/
  /*      if (dispatchOnItemTouch(e)) {
            cancelTouch();
            return true;
        }*/

        /*if (mLayout == null) {
            return false;
        }*/

        //final boolean canScrollHorizontally = mLayout.canScrollHorizontally();
        //final boolean canScrollVertically = mLayout.canScrollVertically();
        final boolean canScrollHorizontally = true;
        final boolean canScrollVertically = true;

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        boolean eventAddedToVelocityTracker = false;

        final MotionEvent vtev = MotionEvent.obtain(e);
        final int action = MotionEventCompat.getActionMasked(e);
        final int actionIndex = MotionEventCompat.getActionIndex(e);

        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsets[0] = mNestedOffsets[1] = 0;
        }
        vtev.offsetLocation(mNestedOffsets[0], mNestedOffsets[1]);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mScrollPointerId = e.getPointerId(0);
                mInitialTouchX = mLastTouchX = (int) (e.getX() + 0.5f);
                mInitialTouchY = mLastTouchY = (int) (e.getY() + 0.5f);

                int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
                if (canScrollHorizontally) {
                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_HORIZONTAL;
                }
                if (canScrollVertically) {
                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
                }
                startNestedScroll(nestedScrollAxis);
                super.onTouchEvent(e);
            }
            break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                mScrollPointerId = e.getPointerId(actionIndex);
                mInitialTouchX = mLastTouchX = (int) (e.getX(actionIndex) + 0.5f);
                mInitialTouchY = mLastTouchY = (int) (e.getY(actionIndex) + 0.5f);
                super.onTouchEvent(e);
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                final int index = e.findPointerIndex(mScrollPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id " +
                            mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                final int x = (int) (e.getX(index) + 0.5f);
                final int y = (int) (e.getY(index) + 0.5f);
                int dx = mLastTouchX - x;
                int dy = mLastTouchY - y;

                if (dispatchNestedPreScroll(dx, dy, mScrollConsumed, mScrollOffset)) {
                    dx -= mScrollConsumed[0];
                    dy -= mScrollConsumed[1];
                    vtev.offsetLocation(mScrollOffset[0], mScrollOffset[1]);
                    // Updated the nested offsets
                    mNestedOffsets[0] += mScrollOffset[0];
                    mNestedOffsets[1] += mScrollOffset[1];
                }

                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    boolean startScroll = false;
                    if (canScrollHorizontally && Math.abs(dx) > mTouchSlop) {
                        if (dx > 0) {
                            dx -= mTouchSlop;
                        } else {
                            dx += mTouchSlop;
                        }
                        startScroll = true;
                    }
                    if (canScrollVertically && Math.abs(dy) > mTouchSlop) {
                        if (dy > 0) {
                            dy -= mTouchSlop;
                        } else {
                            dy += mTouchSlop;
                        }
                        startScroll = true;
                    }
                    if (startScroll) {
                        setScrollState(SCROLL_STATE_DRAGGING);
                    }
                }

                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    mLastTouchX = x - mScrollOffset[0];
                    mLastTouchY = y - mScrollOffset[1];
                    if (scrollByInternal(
                            canScrollHorizontally ? dx : 0,
                            canScrollVertically ? dy : 0,
                            vtev)) {
                        //getParent().requestDisallowInterceptTouchEvent(true);
                    }else {


                    }
                    super.onTouchEvent(e);
                    /*if (mGapWorker != null && (dx != 0 || dy != 0)) {
                        mGapWorker.postFromTraversal(this, dx, dy);
                    }*/
                }
            }
            break;

            case MotionEventCompat.ACTION_POINTER_UP: {
                onPointerUp(e);
                super.onTouchEvent(e);
            }
            break;

            case MotionEvent.ACTION_UP: {
                mVelocityTracker.addMovement(vtev);
                eventAddedToVelocityTracker = true;
                mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                final float xvel = canScrollHorizontally ?
                        -VelocityTrackerCompat.getXVelocity(mVelocityTracker, mScrollPointerId) : 0;
                final float yvel = canScrollVertically ?
                        -VelocityTrackerCompat.getYVelocity(mVelocityTracker, mScrollPointerId) : 0;
                if (!((xvel != 0 || yvel != 0) && fling((int) xvel, (int) yvel))) {
                    setScrollState(SCROLL_STATE_IDLE);
                }
                resetTouch();
                super.onTouchEvent(e);
            }
            break;

            case MotionEvent.ACTION_CANCEL: {
                cancelTouch();
                super.onTouchEvent(e);
            }
            break;
        }

        if (!eventAddedToVelocityTracker) {
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();

        return true;
    }


    boolean scrollByInternal(int x, int y, MotionEvent ev) {
        Log.e("SUKA", "scrollByInternal");
        int unconsumedX = 0, unconsumedY = 0;
        int consumedX = 0, consumedY = 0;

        //if (mAdapter != null) {
        TraceCompat.beginSection(TRACE_SCROLL_TAG);
        if (x != 0) {
            //consumedX = mLayout.scrollHorizontallyBy(x, mRecycler, mState);
            consumedX = 0;
            unconsumedX = x - consumedX;
        }
        if (y != 0) {
            //consumedY = mLayout.scrollVerticallyBy(y, mRecycler, mState);
            consumedY = 0;
            unconsumedY = y - consumedY;
        }
        TraceCompat.endSection();
        //}

        if (dispatchNestedScroll(consumedX, consumedY, unconsumedX, unconsumedY, mScrollOffset)) {
            // Update the last touch co-ords, taking any scroll offset into account
            mLastTouchX -= mScrollOffset[0];
            mLastTouchY -= mScrollOffset[1];
            if (ev != null) {
                ev.offsetLocation(mScrollOffset[0], mScrollOffset[1]);
            }
            mNestedOffsets[0] += mScrollOffset[0];
            mNestedOffsets[1] += mScrollOffset[1];
        } else if (getOverScrollMode() != View.OVER_SCROLL_NEVER) {
            if (ev != null) {
                //pullGlows(ev.getX(), unconsumedX, ev.getY(), unconsumedY);
            }
            //considerReleasingGlowsOnScroll(x, y);
        }
        if (consumedX != 0 || consumedY != 0) {
            dispatchOnScrolled(consumedX, consumedY);
        }
        if (!awakenScrollBars()) {
            invalidate();
        }
        return consumedX != 0 || consumedY != 0;
    }

    public boolean fling(int velocityX, int velocityY) {
        /*if (mLayout == null) {
            Log.e(TAG, "Cannot fling without a LayoutManager set. " +
                    "Call setLayoutManager with a non-null argument.");
            return false;
        }*/
        /*if (mLayoutFrozen) {
            return false;
        }*/

        //final boolean canScrollHorizontal = mLayout.canScrollHorizontally();
        //final boolean canScrollVertical = mLayout.canScrollVertically();
        final boolean canScrollHorizontal = true;
        final boolean canScrollVertical = true;

        if (!canScrollHorizontal || Math.abs(velocityX) < mMinFlingVelocity) {
            velocityX = 0;
        }
        if (!canScrollVertical || Math.abs(velocityY) < mMinFlingVelocity) {
            velocityY = 0;
        }
        if (velocityX == 0 && velocityY == 0) {
            // If we don't have any velocity, return false
            return false;
        }

        if (!dispatchNestedPreFling(velocityX, velocityY)) {
            final boolean canScroll = canScrollHorizontal || canScrollVertical;
            dispatchNestedFling(velocityX, velocityY, canScroll);

            if (mOnFlingListener != null && mOnFlingListener.onFling(velocityX, velocityY)) {
                return true;
            }

            if (canScroll) {
                velocityX = Math.max(-mMaxFlingVelocity, Math.min(velocityX, mMaxFlingVelocity));
                velocityY = Math.max(-mMaxFlingVelocity, Math.min(velocityY, mMaxFlingVelocity));
                //todo mViewFlinger.fling(velocityX, velocityY);
                return true;
            }
        }
        return false;
    }

    private void resetTouch() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
        }
        stopNestedScroll();
        //releaseGlows();
    }

    private void cancelTouch() {
        resetTouch();
        setScrollState(SCROLL_STATE_IDLE);
    }

    private void onPointerUp(MotionEvent e) {
        final int actionIndex = MotionEventCompat.getActionIndex(e);
        if (e.getPointerId(actionIndex) == mScrollPointerId) {
            // Pick a new pointer to pick up the slack.
            final int newIndex = actionIndex == 0 ? 1 : 0;
            mScrollPointerId = e.getPointerId(newIndex);
            mInitialTouchX = mLastTouchX = (int) (e.getX(newIndex) + 0.5f);
            mInitialTouchY = mLastTouchY = (int) (e.getY(newIndex) + 0.5f);
        }
    }

    void setScrollState(int state) {
        if (state == mScrollState) {
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "setting scroll state to " + state + " from " + mScrollState,
                    new Exception());
        }
        mScrollState = state;
        if (state != SCROLL_STATE_SETTLING) {
            stopScrollersInternal();
        }
        dispatchOnScrollStateChanged(state);
    }

    private void stopScrollersInternal() {
        /*mViewFlinger.stop();
        if (mLayout != null) {
            mLayout.stopSmoothScroller();
        }*/
    }

    void dispatchOnScrollStateChanged(int state) {
        // Let the LayoutManager go first; this allows it to bring any properties into
        // a consistent state before the RecyclerView subclass responds.
        /*if (mLayout != null) {
            mLayout.onScrollStateChanged(state);
        }*/

        // Let the RecyclerView subclass handle this event next; any LayoutManager property
        // changes will be reflected by this time.
        onScrollStateChanged(state);

        // Listeners go last. All other internal state is consistent by this point.
        /*if (mScrollListener != null) {
            mScrollListener.onScrollStateChanged(this, state);
        }
        if (mScrollListeners != null) {
            for (int i = mScrollListeners.size() - 1; i >= 0; i--) {
                mScrollListeners.get(i).onScrollStateChanged(this, state);
            }
        }*/
    }

    public void onScrollStateChanged(int state) {
        // Do nothing
    }


    void dispatchOnScrolled(int hresult, int vresult) {
        // Pass the current scrollX/scrollY values; no actual change in these properties occurred
        // but some general-purpose code may choose to respond to changes this way.
        final int scrollX = getScrollX();
        final int scrollY = getScrollY();
        onScrollChanged(scrollX, scrollY, scrollX, scrollY);

        // Pass the real deltas to onScrolled, the RecyclerView-specific method.
        //onScrolled(hresult, vresult);

        // Invoke listeners last. Subclassed view methods always handle the event first.
        // All internal state is consistent by the time listeners are invoked.
        /*if (mScrollListener != null) {
            mScrollListener.onScrolled(this, hresult, vresult);
        }
        if (mScrollListeners != null) {
            for (int i = mScrollListeners.size() - 1; i >= 0; i--) {
                mScrollListeners.get(i).onScrolled(this, hresult, vresult);
            }
        }*/
    }


    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
}