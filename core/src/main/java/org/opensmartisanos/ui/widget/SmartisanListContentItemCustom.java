/* Ported from smartisanos.widget.ListContentItemCustom in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

public class SmartisanListContentItemCustom extends SmartisanListContentItem {
    public SmartisanListContentItemCustom(Context context) { this(context, null); }
    public SmartisanListContentItemCustom(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanListContentItemCustom(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override protected int getDefaultRightLayout() { return 0; }
    @Override protected void initRightWidget() { }
}
