package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

public class SmartisanSimpleEditor extends SmartisanEditor {
    public SmartisanSimpleEditor(Context context) { this(context, null); }
    public SmartisanSimpleEditor(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanSimpleEditor(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
    @Override protected int getDefaultLeftLayout() { return 0; }
    @Override protected int getDefaultRightLayout() { return 0; }
    @Override protected void initLeftWidget() { }
    @Override protected void initRightWidget() { }
}
