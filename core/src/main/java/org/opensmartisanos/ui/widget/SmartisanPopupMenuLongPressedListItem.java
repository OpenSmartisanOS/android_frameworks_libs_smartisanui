package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

/** Compact icon and title row used by grouped long-press menus. */
public class SmartisanPopupMenuLongPressedListItem extends RelativeLayout {
    private final ImageView iconView;
    private final TextView titleView;
    private MenuItem itemData;

    public SmartisanPopupMenuLongPressedListItem(Context context) { this(context, null); }
    public SmartisanPopupMenuLongPressedListItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public SmartisanPopupMenuLongPressedListItem(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(
                R.layout.smartisan_rom_popup_menu_long_pressed_list_item,
                (ViewGroup) this, true);
        iconView = findViewById(R.id.smartisan_rom_menu_icon);
        titleView = findViewById(R.id.smartisan_rom_menu_title);
    }

    public void initialize(MenuItem itemData, int position) {
        this.itemData = itemData;
        titleView.setText(itemData.getTitle());
        iconView.setImageDrawable(itemData.getIcon());
        setVisibility(itemData.isVisible() ? VISIBLE : GONE);
        setEnabled(itemData.isEnabled());
    }
    public MenuItem getItemData() { return itemData; }
}
