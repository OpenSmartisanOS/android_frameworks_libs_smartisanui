/* Ported from smartisanos.widget.SettingItemCheck in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SmartisanSettingItemCheck extends SmartisanListContentItemCheck {
    public SmartisanSettingItemCheck(Context context) { this(context, null); }
    public SmartisanSettingItemCheck(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanSettingItemCheck(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ImageView getIcon() { return getIconView(); }
    public void setIconVisibility(int visibility) {
        if (visibility != VISIBLE && visibility != INVISIBLE && visibility != GONE) {
            throw new IllegalArgumentException("illegal icon visibility");
        }
        if (getIconView() != null) getIconView().setVisibility(visibility);
    }
    public void setSummaryMarqueeEnable(boolean enabled) {
        if (summary == null) return;
        summary.setEllipsize(enabled ? TextUtils.TruncateAt.MARQUEE : TextUtils.TruncateAt.END);
        summary.setFocusable(enabled);
        summary.setFocusableInTouchMode(enabled);
    }
}
