package org.opensmartisanos.ui.internal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

/** Direct port of Smartisan OS 8.5.3 DividerListView. */
public class RomDividerListView extends ListView {
    private final Rect bounds = new Rect();

    public RomDividerListView(Context context) { super(context); }
    public RomDividerListView(Context context, AttributeSet attrs) { super(context, attrs); }
    public RomDividerListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        Drawable divider = getDivider();
        ListAdapter adapter = getAdapter();
        if (divider == null || adapter == null) return;
        bounds.left = getPaddingLeft();
        bounds.right = getWidth() - getPaddingRight();
        int childCount = getChildCount();
        int firstPosition = getFirstVisiblePosition();
        for (int i = 0; i < childCount - 1; i++) {
            int position = firstPosition + i;
            if (position + 1 < adapter.getCount()
                    && (!adapter.isEnabled(position) || !adapter.isEnabled(position + 1))) {
                View child = getChildAt(i);
                bounds.top = child.getBottom();
                bounds.bottom = bounds.top + getDividerHeight();
                divider.setBounds(bounds);
                divider.draw(canvas);
            }
        }
    }
}
