/* Copyright (C) 2026 OpenSmartisanOS. Licensed under the Apache License, Version 2.0. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import org.opensmartisanos.ui.R;

/**
 * OpenSmartisanOS circular ProgressBar adapter using Smartisan OS artwork.
 * This class is project glue, not a source port of a Smartisan OS widget.
 */
public class SmartisanCircularProgressBar extends ProgressBar {
    public SmartisanCircularProgressBar(Context context) {
        this(context, null);
    }

    public SmartisanCircularProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.smartisanCircularProgressBarStyle);
    }

    public SmartisanCircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,
                R.style.Widget_SmartisanUi_ProgressBar_Circular_Medium);
    }

    public SmartisanCircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
