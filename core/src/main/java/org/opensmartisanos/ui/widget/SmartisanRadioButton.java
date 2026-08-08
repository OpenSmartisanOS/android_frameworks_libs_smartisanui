/* Copyright (C) 2026 OpenSmartisanOS. Licensed under the Apache License, Version 2.0. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

import org.opensmartisanos.ui.R;

/**
 * OpenSmartisanOS platform RadioButton adapter using an original Smartisan OS choice selector.
 * This class is project glue, not a source port of a Smartisan OS widget.
 */
public class SmartisanRadioButton extends RadioButton {
    public SmartisanRadioButton(Context context) {
        this(context, null);
    }

    public SmartisanRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.smartisanRadioButtonStyle);
    }

    public SmartisanRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Widget_SmartisanUi_RadioButton);
    }

    public SmartisanRadioButton(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
