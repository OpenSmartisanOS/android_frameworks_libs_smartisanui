package org.opensmartisanos.ui.internal;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

/** Package-internal public-SDK remap of the R2 FontFitTextView used by menu rows. */
public final class RomFontFitTextView extends TextView {
    private final Paint fitPaint = new Paint();
    private float minimumTextSize;

    public RomFontFitTextView(Context context) { this(context, null); }
    public RomFontFitTextView(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public RomFontFitTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        minimumTextSize = getResources().getDimension(
                R.dimen.smartisan_rom_menu_dialog_item_text_min_size);
    }

    public void setMinTextSize(float px) {
        minimumTextSize = px;
        if (px > getTextSize()) setTextSize(TypedValue.COMPLEX_UNIT_PX, px);
        refitText(getText().toString(), getWidth());
    }

    private void refitText(String text, int width) {
        if (width <= 0) return;
        int targetWidth = width - getPaddingLeft() - getPaddingRight();
        fitPaint.set(getPaint());
        if (fitPaint.measureText(text) <= targetWidth) return;
        float high = getTextSize();
        float low = minimumTextSize;
        if (high <= low) return;
        while (high - low > 0.5f) {
            float size = (high + low) / 2f;
            fitPaint.setTextSize(size);
            if (fitPaint.measureText(text) >= targetWidth) high = size;
            else low = size;
        }
        if (low > minimumTextSize + 0.5f) {
            float targetHeight = getMeasuredHeight();
            if (fontHeight() > targetHeight) {
                high = low;
                low = minimumTextSize;
                while (high - low > 0.5f) {
                    float size = (high + low) / 2f;
                    fitPaint.setTextSize(size);
                    if (fontHeight() >= targetHeight) high = size;
                    else low = size;
                }
            }
        }
        setTextSize(TypedValue.COMPLEX_UNIT_PX, low);
    }

    private float fontHeight() {
        Paint.FontMetrics metrics = fitPaint.getFontMetrics();
        return metrics.bottom - metrics.top;
    }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        refitText(getText().toString(), getMeasuredWidth());
    }

    @Override protected void onTextChanged(CharSequence text, int start, int before, int after) {
        super.onTextChanged(text, start, before, after);
        refitText(text.toString(), getWidth());
    }

    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width != oldWidth) refitText(getText().toString(), width);
    }
}
