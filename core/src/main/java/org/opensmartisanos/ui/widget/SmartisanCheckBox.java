/* Copyright (C) 2026 OpenSmartisanOS. Licensed under the Apache License, Version 2.0. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

import org.opensmartisanos.ui.R;

/**
 * OpenSmartisanOS platform CheckBox adapter using original Smartisan OS 8.5.3 state images.
 * This class is project glue, not a source port of a Smartisan OS widget.
 */
public class SmartisanCheckBox extends CheckBox {
    public SmartisanCheckBox(Context context) {
        this(context, null);
    }

    public SmartisanCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.smartisanCheckBoxStyle);
    }

    public SmartisanCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_SmartisanUi_CheckBox);
    }

    public SmartisanCheckBox(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
