package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

/** Popup row with an independently clickable remove affordance. */
public class SmartisanPopupMenuRemovableListItem extends RelativeLayout {
    private final ImageView menuIcon;
    private final TextView menuTitle;
    private final FrameLayout closeIcon;

    public SmartisanPopupMenuRemovableListItem(Context context) { this(context, null); }
    public SmartisanPopupMenuRemovableListItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public SmartisanPopupMenuRemovableListItem(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(
                R.layout.smartisan_rom_popup_menu_removable_list_item, this, true);
        menuIcon = view.findViewById(R.id.smartisan_rom_menu_icon);
        menuTitle = view.findViewById(R.id.smartisan_rom_menu_title);
        closeIcon = view.findViewById(R.id.smartisan_rom_menu_closed);
    }

    public void setMenuIcon(int resourceId) { setMenuIcon(getContext().getDrawable(resourceId)); }
    public void setMenuIcon(Drawable icon) {
        menuIcon.setImageDrawable(icon);
        menuIcon.setVisibility(icon == null ? GONE : VISIBLE);
        adjustMenuTitleMargin(icon != null);
    }
    public void adjustMenuTitleMargin(boolean hasMenuIcon) {
        LayoutParams params = (LayoutParams) menuTitle.getLayoutParams();
        params.setMarginStart(hasMenuIcon ? 0 : getResources().getDimensionPixelSize(
                R.dimen.smartisan_rom_popup_list_title_left_margin));
        menuTitle.setLayoutParams(params);
    }
    public ImageView getIconView() { return menuIcon; }
    public void setMenuTitle(int resourceId) { setMenuTitle(getResources().getText(resourceId)); }
    public void setMenuTitle(CharSequence title) { menuTitle.setText(title); }
    public void setOnCloseIconClickListener(View.OnClickListener listener) { closeIcon.setOnClickListener(listener); }
    public View getCloseIconView() { return closeIcon; }
    public void setCloseIconVisibility(int visibility) { closeIcon.setVisibility(visibility); }
    @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(SmartisanPopupMenuStandardListItem.class.getName());
        info.setCheckable(true);
    }
}
