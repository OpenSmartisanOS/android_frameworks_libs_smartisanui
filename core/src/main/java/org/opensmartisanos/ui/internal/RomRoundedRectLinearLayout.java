package org.opensmartisanos.ui.internal;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import org.opensmartisanos.ui.R;

/** Direct port of Smartisan OS 8.5.3 RoundedRectLinearLayout. */
public class RomRoundedRectLinearLayout extends LinearLayout {
    private Path clip;
    private float radius;

    public RomRoundedRectLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RomRoundedRectLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs == null) return;
        TypedArray array = getContext().obtainStyledAttributes(attrs,
                new int[] {R.attr.cornerRadius}, 0, 0);
        radius = array.getDimensionPixelSize(0, 0);
        array.recycle();
    }

    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (radius > 0f) {
            clip = new Path();
            clip.addRoundRect(new RectF(0f, 0f, width, height), radius, radius, Path.Direction.CW);
        }
    }

    @Override protected void dispatchDraw(Canvas canvas) {
        int saveCount = canvas.save();
        if (radius > 0f && clip != null) canvas.clipPath(clip);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(saveCount);
    }
}
