package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.RomPageIndicator;
import org.opensmartisanos.ui.internal.RomPagedView;
import org.opensmartisanos.ui.internal.SmartisanMenuMath;

import java.util.ArrayList;
import java.util.List;

/** Public-SDK pager port of the original Smartisan icon-grid popup menu. */
public class SmartisanGridIconPopupMenu extends SmartisanPopupMenu {
    public interface OnMenuItemClickListener {
        void onMenuItemClick(int position);
    }

    private static final int MAX_ROWS = 3;

    private final int columns;
    private final RomPagedView pages;
    private final RomPageIndicator indicator;
    private ArrayList<Drawable> icons;
    private int iconSize;
    private OnMenuItemClickListener listener;
    private int pageCount;

    public SmartisanGridIconPopupMenu(Context context, int columns) {
        super(context);
        if (columns <= 0) {
            throw new IllegalArgumentException(
                    "invalid args, the numColumn should be positive integer");
        }
        this.columns = columns;
        mContentAreaWidth = context.getResources().getDimensionPixelSize(
                R.dimen.smartisan_rom_popup_grid_menu_default_width);
        mMenuPanelView = LayoutInflater.from(context).inflate(
                R.layout.smartisan_rom_grid_menu, null);
        pages = mMenuPanelView.findViewById(R.id.smartisan_rom_grid_menu_pager);
        indicator = mMenuPanelView.findViewById(R.id.smartisan_rom_grid_menu_indicator);
        pages.setOnPageChangeListener(position -> updateIndicator());
    }

    public void setIcons(ArrayList<Drawable> values) {
        // R2 deliberately keeps the caller-owned list so drawable replacements are visible on
        // the next show without another setter call.
        iconSize = values.size();
        icons = values;
    }

    public void setIcons(int[] resources) {
        int count = resources.length;
        iconSize = count;
        if (icons == null) icons = new ArrayList<>(count);
        icons.clear();
        for (int resource : resources) icons.add(mContext.getDrawable(resource));
    }

    /** @deprecated Use {@link #setIcons(int[])} or the original Drawable-list API. */
    @Deprecated
    public void setIconResources(List<Integer> resources) {
        ArrayList<Drawable> values = new ArrayList<>();
        if (resources != null) {
            for (int resource : resources) values.add(mContext.getDrawable(resource));
        }
        setIcons(values);
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener value) {
        listener = value;
    }

    @Override
    protected void prepareShow() {
        int selected = pages.getDisplayedChild();
        createPageViews();
        if (pageCount > 0) pages.setDisplayedChild(Math.min(selected, pageCount - 1));
        updateIndicator();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        pages.removeAllViews();
        pageCount = 0;
    }

    private void createPageViews() {
        pages.removeAllViews();
        int iconCount = iconSize;
        pageCount = SmartisanMenuMath.gridPageCount(iconCount, columns);
        int pageSize = columns * MAX_ROWS;
        for (int page = 0; page < pageCount; page++) {
            int start = page * pageSize;
            int end = Math.min(iconCount, start + pageSize);
            GridView grid = (GridView) LayoutInflater.from(mContext).inflate(
                    R.layout.smartisan_rom_grid_menu_page, pages, false);
            grid.setNumColumns(columns);
            grid.setAdapter(new IconAdapter(start, end));
            grid.setOnItemClickListener((parent, view, position, id) -> {
                if (listener != null) listener.onMenuItemClick(start + position);
            });
            pages.addView(grid);
        }
    }

    private boolean showPage(int delta) {
        int next = pages.getDisplayedChild() + delta;
        if (next < 0 || next >= pageCount) return false;
        pages.setCurrentPage(next, true);
        return true;
    }

    private void updateIndicator() {
        indicator.setState(pageCount, pageCount == 0 ? 0 : pages.getDisplayedChild());
    }

    private final class IconAdapter extends BaseAdapter {
        private final int start;
        private final int end;

        IconAdapter(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override public int getCount() { return end - start; }
        @Override public Object getItem(int position) { return position; }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView image;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.smartisan_rom_grid_menu_item, parent, false);
                image = convertView.findViewById(R.id.smartisan_rom_grid_menu_icon);
                convertView.setTag(image);
            } else {
                image = (ImageView) convertView.getTag();
            }
            int absolute = start + position;
            image.setImageDrawable(icons.get(absolute));
            image.setContentDescription(Integer.toString(absolute + 1));
            return convertView;
        }
    }
}
