package org.opensmartisanos.ui.widget;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import org.opensmartisanos.ui.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/** Public-SDK grouped Menu adapter used by long-press popup menus. */
public class SmartisanGroupMenuAdapter extends BaseAdapter
        implements AdapterView.OnItemClickListener, SmartisanListPopupMenu.GroupedAdapter {
    /** Exact-item callback for callers that cannot guarantee unique platform Menu item IDs. */
    public interface OnMenuItemActionListener {
        void onMenuItemAction(MenuItem item);
    }

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_DIVIDER = 1;
    private static final String DIVIDER_MENU = "Divider";
    private final Menu menu;
    private final MenuItem dividerMenuItem;
    private final List<MenuItem> menuItems;
    private OnMenuItemActionListener menuItemActionListener;

    public SmartisanGroupMenuAdapter(Menu menu) {
        if (menu == null) throw new IllegalArgumentException("menu == null");
        this.menu = menu;
        ArrayList<MenuItem> visible = new ArrayList<>();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.isVisible()) visible.add(item);
        }
        menuItems = new LinkedList<>(visible);
        // R2 creates one sentinel MenuItem in the supplied menu and reuses it for every divider.
        dividerMenuItem = menu.add(DIVIDER_MENU);
        Collections.sort(menuItems, (first, second) -> Integer.compare(
                first.getGroupId(), second.getGroupId()));
        for (int tail = menuItems.size() - 1, head = tail - 1; head >= 0;
                head--, tail--) {
            if (menuItems.get(head).getGroupId() != menuItems.get(tail).getGroupId()) {
                menuItems.add(tail, dividerMenuItem);
            }
        }
    }

    @Override public int getCount() { return menuItems.size(); }
    @Override public MenuItem getItem(int position) { return menuItems.get(position); }
    @Override public long getItemId(int position) { return position; }
    @Override public int getViewTypeCount() { return 2; }
    @Override public int getItemViewType(int position) {
        return getItem(position) == dividerMenuItem ? TYPE_DIVIDER : TYPE_ITEM;
    }
    @Override public boolean isEnabled(int position) {
        MenuItem item = getItem(position);
        return item != dividerMenuItem && item.isEnabled();
    }
    @Override public boolean isGroupStart(int position) {
        return position > 0 && getItemViewType(position - 1) == TYPE_DIVIDER;
    }

    /**
     * Sets an exact-item action callback. When present it takes precedence over the public-SDK
     * {@link Menu#performIdentifierAction(int, int)} fallback.
     */
    public void setOnMenuItemActionListener(OnMenuItemActionListener listener) {
        menuItemActionListener = listener;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == TYPE_DIVIDER) {
            View divider = convertView == null ? new View(parent.getContext()) : convertView;
            divider.setBackgroundResource(R.drawable.smartisan_rom_popup_menu_item_divider);
            divider.setEnabled(false);
            return divider;
        }
        SmartisanPopupMenuLongPressedListItem itemView =
                convertView instanceof SmartisanPopupMenuLongPressedListItem
                        ? (SmartisanPopupMenuLongPressedListItem) convertView
                        : new SmartisanPopupMenuLongPressedListItem(parent.getContext());
        itemView.initialize(getItem(position), position);
        return itemView;
    }

    @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MenuItem item = getItem(position);
        if (item == dividerMenuItem || !item.isEnabled()) return;
        if (menuItemActionListener != null) {
            menuItemActionListener.onMenuItemAction(item);
            return;
        }
        menu.performIdentifierAction(item.getItemId(), 0);
    }
}
