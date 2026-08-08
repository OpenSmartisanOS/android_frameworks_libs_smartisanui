/* Copyright (C) 2026 OpenSmartisanOS. Licensed under the Apache License, Version 2.0. */
package org.opensmartisanos.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.opensmartisanos.ui.R;

/**
 * OpenSmartisanOS bottom-dialog adapter backed only by platform views.
 *
 * <p>The sheet is always presented from the bottom and is intentionally non-draggable, matching
 * the fixed bottom panels used by Smartisan Settings. Callers can resize the sheet without
 * depending on Material's behavior object or private resource ids.
 *
 * <p>This class is a project compatibility control, not a source port of a Smartisan OS widget.
 */
public class SmartisanBottomSheetDialog extends Dialog {
    private final FrameLayout sheet;

    public SmartisanBottomSheetDialog(Context context) {
        this(context, resolveDialogTheme(context));
    }

    public SmartisanBottomSheetDialog(Context context, int themeResId) {
        super(context, themeResId == 0 ? resolveDialogTheme(context) : themeResId);
        sheet = new FrameLayout(getContext());
        sheet.setId(View.generateViewId());
        sheet.setBackgroundResource(R.drawable.smartisan_rom_bottom_sheet_background);
        sheet.setClipToPadding(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (window == null) return;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.gravity = Gravity.BOTTOM;
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(attributes);
        window.setDimAmount(0.32f);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        setCanceledOnTouchOutside(true);
    }

    @Override
    public void setContentView(int layoutResID) {
        View content = getLayoutInflater().inflate(layoutResID, sheet, false);
        setContentView(content);
    }

    @Override
    public void setContentView(View view) {
        setContentView(view, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        sheet.removeAllViews();
        sheet.addView(view, params);
        super.setContentView(sheet, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public FrameLayout getSheetView() {
        return sheet;
    }

    public void setSheetHeight(int height) {
        ViewGroup.LayoutParams params = sheet.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        } else {
            params.height = height;
        }
        sheet.setLayoutParams(params);
    }

    private static int resolveDialogTheme(Context context) {
        TypedValue value = new TypedValue();
        if (context.getTheme().resolveAttribute(R.attr.smartisanBottomSheetDialogTheme,
                value, true) && value.resourceId != 0) {
            return value.resourceId;
        }
        return R.style.ThemeOverlay_SmartisanUi_BottomSheetDialog;
    }
}
