package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

/** Smartisan alert-dialog single-choice row with an optional second line. */
public class SmartisanDialogPatternTwoLineSingleChoice extends RelativeLayout {
    private final ImageView selectedView;
    private final TextView titleView;
    private final TextView summaryView;

    public SmartisanDialogPatternTwoLineSingleChoice(Context context) { this(context, null); }
    public SmartisanDialogPatternTwoLineSingleChoice(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public SmartisanDialogPatternTwoLineSingleChoice(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(
                R.layout.smartisan_rom_dialog_pattern_two_line_single_choice, this, true);
        selectedView = findViewById(R.id.smartisan_rom_item_check);
        titleView = findViewById(R.id.smartisan_rom_item_title);
        summaryView = findViewById(R.id.smartisan_rom_item_summary);
    }

    public void setTitle(int resourceId) { setTitle(getResources().getString(resourceId)); }
    public void setTitle(CharSequence title) { titleView.setText(title); }
    public void setSummary(int resourceId) { setSummary(getResources().getString(resourceId)); }
    public void setSummary(CharSequence summary) {
        summaryView.setText(summary);
        summaryView.setVisibility(TextUtils.isEmpty(summary) ? View.GONE : View.VISIBLE);
    }
    public void setChecked(boolean checked) {
        selectedView.setVisibility(checked ? View.VISIBLE : View.INVISIBLE);
    }
}
