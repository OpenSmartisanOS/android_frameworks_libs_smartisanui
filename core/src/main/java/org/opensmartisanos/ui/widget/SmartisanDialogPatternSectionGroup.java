package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

/** Smartisan alert-dialog section containing an optional title, subtitle and message. */
public class SmartisanDialogPatternSectionGroup extends LinearLayout {
    private final TextView primaryTitleView;
    private final TextView subtitleView;
    private final TextView messageView;

    public SmartisanDialogPatternSectionGroup(Context context) { this(context, null); }
    public SmartisanDialogPatternSectionGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public SmartisanDialogPatternSectionGroup(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.smartisan_rom_dialog_pattern_section_group,
                this, true);
        primaryTitleView = findViewById(R.id.smartisan_rom_section_primary_title);
        subtitleView = findViewById(R.id.smartisan_rom_section_subtitle);
        messageView = findViewById(R.id.smartisan_rom_section_message);
        TypedArray values = context.obtainStyledAttributes(attrs,
                R.styleable.SmartisanDialogPatternSectionGroup, defStyleAttr, 0);
        setPrimaryTitle(values.getString(
                R.styleable.SmartisanDialogPatternSectionGroup_smartisanDialogSectionTitle));
        setSubtitle(values.getString(
                R.styleable.SmartisanDialogPatternSectionGroup_smartisanDialogSectionSubtitle));
        setMessage(values.getString(
                R.styleable.SmartisanDialogPatternSectionGroup_smartisanDialogSectionMessage));
        values.recycle();
    }

    public void setPrimaryTitle(int resourceId) { setPrimaryTitle(getResources().getString(resourceId)); }
    public void setPrimaryTitle(CharSequence value) { setText(primaryTitleView, value); }
    public void setSubtitle(int resourceId) { setSubtitle(getResources().getString(resourceId)); }
    public void setSubtitle(CharSequence value) { setText(subtitleView, value); }
    public void setMessage(int resourceId) { setMessage(getResources().getString(resourceId)); }
    public void setMessage(CharSequence value) { setText(messageView, value); }

    private static void setText(TextView view, CharSequence value) {
        view.setText(value);
        view.setVisibility(TextUtils.isEmpty(value) ? View.GONE : View.VISIBLE);
    }
}
