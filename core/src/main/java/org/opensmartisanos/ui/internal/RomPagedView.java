package org.opensmartisanos.ui.internal;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Scroller;

import org.opensmartisanos.ui.R;

/**
 * Small public-SDK horizontal pager used in place of the ROM's private ViewPager.
 *
 * <p>Unlike the former ViewFlipper approximation this supports direct dragging, low-velocity
 * snapping, keyboard and accessibility paging, RTL layout and state restoration.</p>
 */
public final class RomPagedView extends ViewGroup {
    public interface OnPageChangeListener {
        void onPageSelected(int position);
    }

    private static final int INVALID_POINTER = -1;
    private static final int SETTLE_DURATION_MS = 220;

    private final Scroller scroller;
    private final int touchSlop;
    private final int minimumVelocity;
    private final int maximumVelocity;
    private VelocityTracker velocityTracker;
    private OnPageChangeListener pageChangeListener;
    private int currentPage;
    private int pendingRestoredPage = INVALID_POINTER;
    private int activePointerId = INVALID_POINTER;
    private float initialMotionX;
    private float initialMotionY;
    private float lastMotionX;
    private boolean dragging;

    public RomPagedView(Context context) {
        this(context, null);
    }

    public RomPagedView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RomPagedView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        scroller = new Scroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        touchSlop = configuration.getScaledPagingTouchSlop();
        minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(true);
    }

    public int getDisplayedChild() {
        return currentPage;
    }

    public void setDisplayedChild(int position) {
        setCurrentPage(position, false);
    }

    public void setCurrentPage(int position, boolean smoothScroll) {
        int target = clampPage(position);
        if (getChildCount() == 0) {
            pendingRestoredPage = Math.max(0, position);
            currentPage = 0;
            return;
        }
        boolean changed = target != currentPage;
        currentPage = target;
        pendingRestoredPage = INVALID_POINTER;
        int destination = scrollForPage(target);
        if (smoothScroll && getWidth() > 0) {
            scroller.startScroll(getScrollX(), 0, destination - getScrollX(), 0,
                    SETTLE_DURATION_MS);
            postInvalidateOnAnimation();
        } else {
            scroller.abortAnimation();
            scrollTo(destination, 0);
        }
        updatePageAccessibility();
        if (changed && pageChangeListener != null) pageChangeListener.onPageSelected(target);
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        pageChangeListener = listener;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // GridView claims the gesture as soon as an item enters pressed state. Each grid page is
        // fixed to at most three rows and cannot scroll vertically, so honouring that early claim
        // would prevent this pager from ever seeing the horizontal MOVE that selects a page.
        // Once paging has started, propagate the request normally to keep ancestors out of it.
        if (!disallowIntercept || dragging) {
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int availableWidth = Math.max(0, width - getPaddingLeft() - getPaddingRight());
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int availableHeight = Math.max(0, heightSize - getPaddingTop() - getPaddingBottom());
        int childHeightSpec = heightMode == MeasureSpec.UNSPECIFIED
                ? MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                : MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.AT_MOST);
        int firstChildHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.measure(MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY),
                    childHeightSpec);
            if (i == 0) firstChildHeight = child.getMeasuredHeight();
        }
        // R2's WrapContentViewPager deliberately derives its wrap height from page zero. Keep
        // measuring every child so later pages can be laid out immediately, but do not let a
        // taller later page change the popup's original geometry.
        int desiredHeight = firstChildHeight + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int pageWidth = getPageWidth();
        int childTop = getPaddingTop();
        for (int i = 0; i < getChildCount(); i++) {
            int physical = physicalPosition(i);
            int childLeft = getPaddingLeft() + physical * pageWidth;
            View child = getChildAt(i);
            child.layout(childLeft, childTop, childLeft + pageWidth,
                    childTop + child.getMeasuredHeight());
        }
        if (pendingRestoredPage != INVALID_POINTER) {
            currentPage = clampPage(pendingRestoredPage);
            pendingRestoredPage = INVALID_POINTER;
        } else {
            currentPage = clampPage(currentPage);
        }
        if (!dragging && scroller.isFinished()) scrollTo(scrollForPage(currentPage), 0);
        updatePageAccessibility();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            endDrag();
            return false;
        }
        if (action != MotionEvent.ACTION_DOWN && dragging) return true;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = event.getPointerId(0);
                initialMotionX = lastMotionX = event.getX();
                initialMotionY = event.getY();
                dragging = !scroller.isFinished();
                if (dragging) scroller.abortAnimation();
                ensureVelocityTracker().addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                int pointer = event.findPointerIndex(activePointerId);
                if (pointer < 0) break;
                float x = event.getX(pointer);
                float dx = x - lastMotionX;
                float totalX = x - initialMotionX;
                float totalY = event.getY(pointer) - initialMotionY;
                if (Math.abs(totalX) > touchSlop && Math.abs(totalX) > Math.abs(totalY)) {
                    dragging = true;
                    // Preserve the part of the first drag that exceeds touch slop. Basing this on
                    // the current pointer position would leave only `touchSlop` for onTouchEvent
                    // and make a slow, long drag snap back to the original page.
                    lastMotionX = initialMotionX + Math.copySign(touchSlop, totalX);
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                break;
            default:
                break;
        }
        return dragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getChildCount() <= 1) return super.onTouchEvent(event);
        ensureVelocityTracker().addMovement(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!scroller.isFinished()) scroller.abortAnimation();
                activePointerId = event.getPointerId(0);
                initialMotionX = lastMotionX = event.getX();
                initialMotionY = event.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                int pointer = event.findPointerIndex(activePointerId);
                if (pointer < 0) return false;
                float x = event.getX(pointer);
                if (!dragging && Math.abs(x - initialMotionX) > touchSlop) {
                    dragging = true;
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (dragging) {
                    float delta = lastMotionX - x;
                    lastMotionX = x;
                    int maximum = Math.max(0, (getChildCount() - 1) * getPageWidth());
                    scrollTo(Math.max(0, Math.min(maximum, Math.round(getScrollX() + delta))), 0);
                }
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(event);
                return true;
            case MotionEvent.ACTION_UP:
                boolean wasDragging = dragging;
                velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                float velocity = activePointerId == INVALID_POINTER ? 0f
                        : velocityTracker.getXVelocity(activePointerId);
                settleAfterDrag(velocity);
                endDrag();
                if (!wasDragging) performClick();
                return true;
            case MotionEvent.ACTION_CANCEL:
                setCurrentPage(currentPage, true);
                endDrag();
                return true;
            default:
                return true;
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void settleAfterDrag(float velocityX) {
        int pageWidth = getPageWidth();
        int currentPhysical = physicalPosition(currentPage);
        int targetPhysical = currentPhysical;
        int displacement = getScrollX() - currentPhysical * pageWidth;
        if (Math.abs(velocityX) >= minimumVelocity) {
            targetPhysical += velocityX < 0f ? 1 : -1;
        } else if (Math.abs(displacement) >= pageWidth / 4) {
            targetPhysical += displacement > 0 ? 1 : -1;
        }
        targetPhysical = Math.max(0, Math.min(getChildCount() - 1, targetPhysical));
        setCurrentPage(logicalPosition(targetPhysical), true);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidateOnAnimation();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) return movePhysical(-1);
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) return movePhysical(1);
        if (keyCode == KeyEvent.KEYCODE_PAGE_UP) return moveLogical(-1);
        if (keyCode == KeyEvent.KEYCODE_PAGE_DOWN) return moveLogical(1);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName("android.widget.HorizontalScrollView");
        boolean multiplePages = getChildCount() > 1;
        info.setScrollable(multiplePages);
        if (currentPage < getChildCount() - 1) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
        }
        if (currentPage > 0) {
            info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
        }
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) return moveLogical(1);
        if (action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) return moveLogical(-1);
        return super.performAccessibilityAction(action, arguments);
    }

    private boolean movePhysical(int delta) {
        int physical = physicalPosition(currentPage) + delta;
        if (physical < 0 || physical >= getChildCount()) return false;
        setCurrentPage(logicalPosition(physical), true);
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SCROLLED);
        return true;
    }

    private boolean moveLogical(int delta) {
        int target = currentPage + delta;
        if (target < 0 || target >= getChildCount()) return false;
        setCurrentPage(target, true);
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SCROLLED);
        return true;
    }

    private void updatePageAccessibility() {
        int count = getChildCount();
        setContentDescription(count == 0 ? null : getResources().getString(
                R.string.smartisan_rom_indicator_content_description, currentPage + 1, count));
        for (int i = 0; i < count; i++) {
            getChildAt(i).setImportantForAccessibility(i == currentPage
                    ? IMPORTANT_FOR_ACCESSIBILITY_AUTO
                    : IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        }
    }

    private int clampPage(int page) {
        return getChildCount() == 0 ? 0 : Math.max(0, Math.min(getChildCount() - 1, page));
    }

    private int physicalPosition(int logicalPosition) {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL
                ? getChildCount() - 1 - logicalPosition : logicalPosition;
    }

    private int logicalPosition(int physicalPosition) {
        return getLayoutDirection() == LAYOUT_DIRECTION_RTL
                ? getChildCount() - 1 - physicalPosition : physicalPosition;
    }

    private int scrollForPage(int page) {
        return Math.max(0, physicalPosition(clampPage(page)) * getPageWidth());
    }

    private int getPageWidth() {
        return Math.max(1, getWidth() - getPaddingLeft() - getPaddingRight());
    }

    private VelocityTracker ensureVelocityTracker() {
        if (velocityTracker == null) velocityTracker = VelocityTracker.obtain();
        return velocityTracker;
    }

    private void onSecondaryPointerUp(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        if (event.getPointerId(pointerIndex) != activePointerId) return;
        int newIndex = pointerIndex == 0 ? 1 : 0;
        if (newIndex < event.getPointerCount()) {
            activePointerId = event.getPointerId(newIndex);
            lastMotionX = event.getX(newIndex);
        } else {
            activePointerId = INVALID_POINTER;
        }
    }

    private void endDrag() {
        dragging = false;
        activePointerId = INVALID_POINTER;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        requestLayout();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.currentPage = currentPage;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        pendingRestoredPage = Math.max(0, savedState.currentPage);
        requestLayout();
    }

    private static final class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override public SavedState createFromParcel(Parcel source) {
                        return new SavedState(source);
                    }

                    @Override public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        int currentPage;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);
            currentPage = source.readInt();
        }

        @Override public void writeToParcel(Parcel destination, int flags) {
            super.writeToParcel(destination, flags);
            destination.writeInt(currentPage);
        }
    }
}
