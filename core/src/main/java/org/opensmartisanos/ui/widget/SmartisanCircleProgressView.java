/* Ported from smartisanos.widget.CircleProgressView in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import org.opensmartisanos.ui.R;

/** Determinate circular progress using the original Smartisan circle and mask artwork. */
public class SmartisanCircleProgressView extends View {
    private final Drawable circle;
    private final Bitmap mask;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect rect = new Rect();
    private final int preferredSize;
    private float sweep;

    public SmartisanCircleProgressView(Context context) {
        this(context, null);
    }

    public SmartisanCircleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        circle = getResources().getDrawable(R.drawable.smartisan_rom_circle,
                context.getTheme());
        mask = ((BitmapDrawable) getResources().getDrawable(
                R.drawable.smartisan_rom_circle_mask, context.getTheme())).getBitmap();
        preferredSize = getResources().getDimensionPixelSize(
                R.dimen.smartisan_rom_circle_progress_view_size);
        paint.setColor(getResources().getColor(
                R.color.smartisan_rom_circle_progress_view_arc_color, context.getTheme()));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = resolveSize(preferredSize, widthMeasureSpec);
        int height = resolveSize(preferredSize, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int side = Math.min(getWidth(), getHeight());
        int left = (getWidth() - side) / 2;
        int top = (getHeight() - side) / 2;
        rect.set(left, top, left + side, top + side);

        int layer = canvas.saveLayer(left, top, left + side, top + side, null);
        canvas.drawArc(left, top, left + side, top + side, -90, sweep, true, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(mask, null, rect, paint);
        paint.setXfermode(null);
        canvas.restoreToCount(layer);

        circle.setBounds(rect);
        circle.draw(canvas);
    }

    public int getCircleWidth() {
        return preferredSize;
    }

    public int getCircleHeight() {
        return preferredSize;
    }

    public float getSweepAngle() {
        return sweep;
    }

    public void setSweepAngle(float value) {
        sweep = Math.max(0f, Math.min(360f, value));
        invalidate();
    }

    public void reset() {
        setSweepAngle(0);
    }
}
