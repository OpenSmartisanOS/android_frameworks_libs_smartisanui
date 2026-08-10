package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

/** Smartisan alert-dialog content block containing an app icon, title and summary. */
public class SmartisanDialogPatternAppInfoLayout extends LinearLayout {
    private final ImageView iconView;
    private final TextView titleView;
    private final TextView summaryView;

    public SmartisanDialogPatternAppInfoLayout(Context context) { this(context, null); }
    public SmartisanDialogPatternAppInfoLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public SmartisanDialogPatternAppInfoLayout(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.smartisan_rom_dialog_pattern_app_info,
                this, true);
        iconView = findViewById(R.id.smartisan_rom_app_info_icon);
        titleView = findViewById(R.id.smartisan_rom_app_info_title);
        summaryView = findViewById(R.id.smartisan_rom_app_info_summary);
        TypedArray values = context.obtainStyledAttributes(attrs,
                R.styleable.SmartisanDialogPatternAppInfoLayout, defStyleAttr, 0);
        setTitle(values.getString(
                R.styleable.SmartisanDialogPatternAppInfoLayout_smartisanDialogAppInfoTitle));
        setSummary(values.getString(
                R.styleable.SmartisanDialogPatternAppInfoLayout_smartisanDialogAppInfoSummary));
        Drawable icon = values.getDrawable(
                R.styleable.SmartisanDialogPatternAppInfoLayout_smartisanDialogAppInfoIcon);
        if (icon != null) setIcon(icon);
        values.recycle();
    }

    @SuppressWarnings("deprecation")
    public void setIcon(int resourceId) { setIcon(getResources().getDrawable(resourceId)); }
    public void setIcon(Drawable icon) { iconView.setImageDrawable(icon); }
    public void setTitle(int resourceId) { setTitle(getResources().getString(resourceId)); }
    public void setTitle(CharSequence title) { titleView.setText(title); }
    public void setSummary(int resourceId) { setSummary(getResources().getString(resourceId)); }
    public void setSummary(CharSequence summary) { summaryView.setText(summary); }
}
