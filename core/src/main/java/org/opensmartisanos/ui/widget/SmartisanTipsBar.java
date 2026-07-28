/* Ported from smartisanos.widget.TipsView in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

public class SmartisanTipsBar extends TextView {
    public SmartisanTipsBar(Context context) { this(context, null); }
    public SmartisanTipsBar(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanTipsBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTextSize(13.5f); setTextColor(0x99000000); setPadding(dp(12), dp(8), dp(12), dp(8));
    }
    private int dp(float value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setGravity(getLineCount() > 1 ? Gravity.START : Gravity.CENTER);
    }
}
