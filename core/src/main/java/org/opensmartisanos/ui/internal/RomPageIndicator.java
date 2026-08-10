package org.opensmartisanos.ui.internal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import org.opensmartisanos.ui.R;

/** Public-SDK drawing port of the ROM's circle page indicator. */
public final class RomPageIndicator extends View {
    private static final int MAX_PAGE_COUNT = 25;

    private final Paint pagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int pageCount;
    private int currentPage;
    private float radius;

    public RomPageIndicator(Context context) {
        this(context, null);
    }

    public RomPageIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RomPageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        pagePaint.setStyle(Paint.Style.FILL);
        pagePaint.setColor(context.getColor(R.color.smartisan_rom_grid_indicator_page));
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(context.getColor(R.color.smartisan_rom_grid_indicator_fill));
        radius = getResources().getDimension(R.dimen.smartisan_rom_grid_indicator_radius);
    }

    public void setState(int count, int selected) {
        setState(count, selected, true);
    }

    private void setState(int count, int selected, boolean announce) {
        pageCount = Math.max(0, Math.min(MAX_PAGE_COUNT, count));
        currentPage = pageCount == 0 ? 0 : Math.max(0, Math.min(pageCount - 1, selected));
        setContentDescription(pageCount == 0 ? null : getResources().getString(
                R.string.smartisan_rom_indicator_content_description,
                currentPage + 1, pageCount));
        AccessibilityManager manager = (AccessibilityManager) getContext().getSystemService(
                Context.ACCESSIBILITY_SERVICE);
        if (announce && manager != null && manager.isEnabled()) {
            sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        }
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = Math.round(getPaddingLeft() + getPaddingRight()
                + pageCount * radius * 2f + Math.max(0, pageCount - 1) * radius * 2f + 1f);
        int desiredHeight = Math.round(getPaddingTop() + getPaddingBottom() + radius * 4f + 1f);
        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pageCount <= 1) return;
        float step = radius * 4f;
        float totalWidth = radius * 2f + (pageCount - 1) * step;
        float startX = getPaddingLeft()
                + ((getWidth() - getPaddingLeft() - getPaddingRight()) - totalWidth) / 2f
                + radius;
        float centerY = getPaddingTop() + radius * 2f;
        for (int i = 0; i < pageCount; i++) {
            canvas.drawCircle(startX + i * step, centerY, radius,
                    i == currentPage ? fillPaint : pagePaint);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.pageCount = pageCount;
        state.currentPage = currentPage;
        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setState(savedState.pageCount, savedState.currentPage, false);
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

        int pageCount;
        int currentPage;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);
            pageCount = source.readInt();
            currentPage = source.readInt();
        }

        @Override public void writeToParcel(Parcel destination, int flags) {
            super.writeToParcel(destination, flags);
            destination.writeInt(pageCount);
            destination.writeInt(currentPage);
        }
    }
}
