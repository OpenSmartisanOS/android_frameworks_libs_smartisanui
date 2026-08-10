package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

/** Standard icon/title/subtitle/check popup row. */
public class SmartisanPopupMenuStandardListItem extends RelativeLayout {
    private final ImageView menuIcon;
    private final ImageView selectedIcon;
    private final TextView menuTitle;
    private final TextView menuSubtitle;

    public SmartisanPopupMenuStandardListItem(Context context) { this(context, null); }
    public SmartisanPopupMenuStandardListItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public SmartisanPopupMenuStandardListItem(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        android.view.View view = LayoutInflater.from(context).inflate(
                R.layout.smartisan_rom_popup_menu_standard_list_item, this, true);
        menuIcon = view.findViewById(R.id.smartisan_rom_menu_icon);
        selectedIcon = view.findViewById(R.id.smartisan_rom_menu_selected);
        menuTitle = view.findViewById(R.id.smartisan_rom_menu_title);
        menuSubtitle = view.findViewById(R.id.smartisan_rom_subtitle);
    }

    public void setMenuIcon(int resourceId) { setMenuIcon(getContext().getDrawable(resourceId)); }
    public void setMenuIcon(Drawable icon) {
        menuIcon.setImageDrawable(icon);
        menuIcon.setVisibility(icon == null ? GONE : VISIBLE);
        adjustMenuTitleMargin(icon != null);
    }
    public void adjustMenuTitleMargin(boolean hasMenuIcon) {
        LinearLayout.LayoutParams title = (LinearLayout.LayoutParams) menuTitle.getLayoutParams();
        LinearLayout.LayoutParams subtitle = (LinearLayout.LayoutParams) menuSubtitle.getLayoutParams();
        int margin = hasMenuIcon ? 0 : getResources().getDimensionPixelSize(
                R.dimen.smartisan_rom_popup_list_title_left_margin);
        title.setMarginStart(margin);
        subtitle.setMarginStart(margin);
        menuTitle.setLayoutParams(title);
        menuSubtitle.setLayoutParams(subtitle);
    }
    public ImageView getIconView() { return menuIcon; }
    public void setMenuTitle(int resourceId) { setMenuTitle(getResources().getText(resourceId)); }
    public void setMenuTitle(CharSequence title) { menuTitle.setText(title); adjustTitleTextSize(); }
    public void setMenuSubtitle(CharSequence subtitle) {
        menuSubtitle.setText(subtitle);
        menuSubtitle.setVisibility(VISIBLE);
        adjustTitleTextSize();
    }
    public void adjustTitleTextSize() { menuTitle.setTextSize(menuSubtitle.getVisibility() == VISIBLE ? 14 : 15); }
    public void setChecked(boolean checked) { selectedIcon.setVisibility(checked ? VISIBLE : GONE); }
    public boolean isChecked() { return selectedIcon.getVisibility() == VISIBLE; }
    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(getClass().getName());
        info.setCheckable(true);
        info.setChecked(isChecked());
    }
}
