/* Ported from smartisanos.widget.ListContentItemCheck in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;

import org.opensmartisanos.ui.R;

public class SmartisanListContentItemCheck extends SmartisanListContentItem {
    private int selectedIconResource = R.drawable.smartisan_rom_selector_radio_choice;
    private ImageView selectedIcon;

    public SmartisanListContentItemCheck(Context context) { this(context, null); }
    public SmartisanListContentItemCheck(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanListContentItemCheck(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray values = context.obtainStyledAttributes(attrs,
                R.styleable.SmartisanListContentItemCheck, defStyleAttr, 0);
        setSelectedIconRes(values.getResourceId(
                R.styleable.SmartisanListContentItemCheck_selectedIcon,
                R.drawable.smartisan_rom_selector_radio_choice));
        values.recycle();
    }

    @Override protected int getDefaultRightLayout() {
        return R.layout.smartisan_rom_list_content_right_image_view;
    }
    @Override protected void initRightWidget() {
        selectedIcon = findViewById(R.id.smartisan_rom_imageview);
        selectedIcon.setImageResource(selectedIconResource);
        setChecked(false);
    }
    public void setSelectedIconRes(int resource) {
        selectedIconResource = resource;
        if (selectedIcon != null) selectedIcon.setImageResource(resource);
    }
    public void setChecked(boolean checked) {
        if (!customRightView) selectedIcon.setVisibility(checked ? VISIBLE : GONE);
    }
    public boolean isChecked() {
        return !customRightView && selectedIcon.getVisibility() == VISIBLE;
    }
    public ImageView getSelectedIcon() { return selectedIcon; }

    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setSelected(isChecked());
    }
}
