/* Direct public port of smartisanos.widget.ButtonTabGroup in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import org.opensmartisanos.ui.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmartisanSegmentedControl extends LinearLayout implements View.OnClickListener {
    public interface OnItemClickListener { void onItemClick(View view, int index); }
    private final List<SmartisanShadowButton> buttons = new ArrayList<>();
    private List<? extends CharSequence> items = Collections.emptyList();
    private List<Integer> drawables = Collections.emptyList();
    private int forcedWidth = Integer.MIN_VALUE;
    private int selectedIndex = -1;
    private boolean hasGap;
    private boolean alwaysNotify;
    private OnItemClickListener listener;

    public SmartisanSegmentedControl(Context context) { this(context, null); }
    public SmartisanSegmentedControl(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanSegmentedControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); setOrientation(HORIZONTAL); setGravity(Gravity.CENTER); setMeasureWithLargestChildEnabled(true);
    }
    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        if (forcedWidth > 0) widthSpec = View.MeasureSpec.makeMeasureSpec(forcedWidth, View.MeasureSpec.EXACTLY);
        super.onMeasure(widthSpec, heightSpec);
    }
    public void setControlWidth(int width) { forcedWidth = width; requestLayout(); }
    public void setItems(List<? extends CharSequence> values) { setItems(values, false); }
    public void setItems(List<? extends CharSequence> values, boolean withGap) {
        List<? extends CharSequence> newItems = new ArrayList<>(values);
        if (items.equals(newItems) && hasGap == withGap) return;
        items = newItems;
        hasGap = withGap;
        setup();
    }
    public void setItemDrawables(List<Integer> values) { drawables = values == null ? Collections.emptyList() : new ArrayList<>(values); setup(); }
    public void setup() {
        int previousSelection = selectedIndex;
        removeAllViews(); buttons.clear(); selectedIndex = -1;
        for (int i = 0; i < items.size(); i++) {
            SmartisanShadowButton button = new SmartisanShadowButton(getContext());
            button.setText(items.get(i)); button.setEllipsize(TextUtils.TruncateAt.END);
            button.setMaxLines(1); button.setOnClickListener(this);
            if (hasGap || items.size() == 1) {
                button.setBackgroundResource(R.drawable.smartisan_rom_selector_small_btn_standard);
            } else if (i == 0) {
                button.setBackgroundResource(isLayoutRtl()
                        ? R.drawable.smartisan_rom_selector_small_btn_filter_right
                        : R.drawable.smartisan_rom_selector_small_btn_filter_left);
            } else if (i == items.size() - 1) {
                button.setBackgroundResource(isLayoutRtl()
                        ? R.drawable.smartisan_rom_selector_small_btn_filter_left
                        : R.drawable.smartisan_rom_selector_small_btn_filter_right);
            } else {
                button.setBackgroundResource(R.drawable.smartisan_rom_selector_small_btn_filter_middle);
            }
            if (i < drawables.size()) {
                Drawable icon = getContext().getDrawable(drawables.get(i));
                button.setCompoundDrawablesWithIntrinsicBounds(
                        new InsetDrawable(icon, dp(4), 0, dp(4), 0), null, null, null);
            }
            buttons.add(button);
            LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
            addView(button, params);
        }
        if (previousSelection >= 0 && previousSelection < buttons.size()) {
            setSelectedIndex(previousSelection);
        }
    }
    private int dp(float value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
    public SmartisanShadowButton getItemView(int index) { checkIndex(index); return buttons.get(index); }
    public void setSelectedIndex(int index) { setSelectedIndex(index, false); }
    private void setSelectedIndex(int index, boolean fromUser) {
        checkIndex(index);
        if (index == selectedIndex) { if (fromUser && (hasGap || alwaysNotify) && listener != null) listener.onItemClick(buttons.get(index), index); return; }
        if (selectedIndex >= 0) {
            buttons.get(selectedIndex).setActivated(false);
            buttons.get(selectedIndex).setSelected(false);
        }
        selectedIndex = index;
        buttons.get(index).setActivated(true);
        buttons.get(index).setSelected(true);
        if (fromUser && listener != null) listener.onItemClick(buttons.get(index), index);
    }
    public int getSelectedIndex() { return selectedIndex; }
    private void checkIndex(int index) { if (index < 0 || index >= buttons.size()) throw new IndexOutOfBoundsException("index=" + index); }
    @Override public void onClick(View view) { setSelectedIndex(buttons.indexOf(view), true); }
    public void setOnItemClickListener(OnItemClickListener value) { listener = value; }
    public void setItemEnabled(int index, boolean enabled) { SmartisanShadowButton button = getItemView(index); button.setEnabled(enabled); button.setAlpha(enabled ? 1f : 0.3f); }
    public void setAlwaysNotifyOnReselect(boolean value) { alwaysNotify = value; }
    private boolean isLayoutRtl() { return getLayoutDirection() == View.LAYOUT_DIRECTION_RTL; }
    @Override public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);
        // View can dispatch this callback from its constructor, before subclass fields initialize.
        if (items != null && !items.isEmpty()) setup();
    }
}
