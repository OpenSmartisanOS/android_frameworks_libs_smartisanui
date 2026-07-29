/* Ported from smartisanos.widget.BottomBar in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;
import org.opensmartisanos.ui.R;

public class SmartisanBottomBar extends FrameLayout
    implements View.OnClickListener, View.OnLongClickListener {
  public static final int NONE = -1, STYLE_DEFAULT = 0, STYLE_ALIGN_EDGE = 1, STYLE_BOTTOM_TAB = 2;

  public interface OnCheckedChangeListener {
    void onCheckedChanged(ViewGroup group, int id);
  }

  private final List<BarItem> items = new ArrayList<>();
  private LinearLayout container;
  private int checkedId = -1, styleFlag, availableWidth, itemWidth;
  private final int alignEdgeItemWidth;
  private final int heightOnlyIcon;
  private final int heightWithTextIcon;
  private boolean scalable, hasTextAndIcon;
  private OnCheckedChangeListener checkedListener;
  private View.OnClickListener clickListener;
  private View.OnLongClickListener longClickListener;
  private final ImageView shadow;
  private final View divider;

  public SmartisanBottomBar(Context c) {
    this(c, null);
  }

  public SmartisanBottomBar(Context c, AttributeSet a) {
    this(c, a, 0);
  }

  public SmartisanBottomBar(Context c, AttributeSet a, int s) {
    super(c, a, s);
    availableWidth = getResources().getDisplayMetrics().widthPixels;
    itemWidth = availableWidth / 5;
    alignEdgeItemWidth =
        getResources()
            .getDimensionPixelSize(R.dimen.smartisan_rom_smartisan_bottom_bar_align_edge_width);
    heightOnlyIcon =
        getResources()
            .getDimensionPixelSize(R.dimen.smartisan_rom_smartisan_bottom_bar_height_only_icon);
    heightWithTextIcon =
        getResources()
            .getDimensionPixelSize(
                R.dimen.smartisan_rom_smartisan_bottom_bar_height_with_text_icon);
    setBackgroundColor(0xffffffff);
    setClipToPadding(false);
    setElevation(.1f);
    shadow = new ImageView(c);
    shadow.setBackgroundResource(R.drawable.smartisan_rom_bottom_bar_shadow);
    int shadowHeight =
        getResources().getDimensionPixelSize(R.dimen.smartisan_rom_bottom_bar_shadow_height);
    addView(
        shadow,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, shadowHeight, Gravity.TOP));
    shadow.setTranslationY(-shadowHeight);
    divider = new View(c);
    divider.setBackgroundResource(R.drawable.smartisan_rom_divider_bg);
    int dividerHeight =
        getResources().getDimensionPixelSize(R.dimen.smartisan_rom_bar_divider_height);
    addView(
        divider,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight, Gravity.TOP));
    divider.setTranslationY(-dividerHeight);
    addOnLayoutChangeListener(
        (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
          if (getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).setClipChildren(false);
          }
        });
  }

  public void setDefaultSelectedItem(int id) {
    if (id != checkedId) {
      setChecked(id, true);
      checkedId = id;
    }
  }

  private void setChecked(int id, boolean value) {
    if (container != null && container.findViewById(id) instanceof SmartisanBottomBarItemView)
      ((SmartisanBottomBarItemView) container.findViewById(id)).setChecked(value);
  }

  public void setStyleFlag(int flag) {
    styleFlag = flag;
  }

  public void setup() {
    setup(scalable);
  }

  public void setup(boolean scale) {
    if (container != null) removeView(container);
    scalable = scale;
    if (items.isEmpty()) return;
    hasTextAndIcon = false;
    for (BarItem item : items) {
      if (!TextUtils.isEmpty(item.name)) {
        hasTextAndIcon = true;
        break;
      }
    }
    container = new LinearLayout(getContext());
    container.setOrientation(LinearLayout.HORIZONTAL);
    container.setGravity(Gravity.CENTER);
    for (BarItem item : items) {
      SmartisanBottomBarItemView child = create(item);
      container.addView(
          child,
          new LinearLayout.LayoutParams(
              itemWidth, hasTextAndIcon ? heightWithTextIcon : heightOnlyIcon));
    }
    fillStyle(true);
    addView(
        container,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            hasTextAndIcon ? heightWithTextIcon : heightOnlyIcon));
    shadow.bringToFront();
    divider.bringToFront();
    SmartisanBottomBarItemView selected = container.findViewById(checkedId);
    if (selected == null) selected = (SmartisanBottomBarItemView) container.getChildAt(0);
    selected.setChecked(true);
  }

  private SmartisanBottomBarItemView create(BarItem item) {
    SmartisanBottomBarItemView child = new SmartisanBottomBarItemView(getContext());
    child.setScaleable(scalable);
    child.setId(item.id);
    child.setOnCheckedChangeListener(
        (view, value) -> {
          if (value) {
            if (checkedId != -1 && checkedId != view.getId()) setChecked(checkedId, false);
            checkedId = view.getId();
            if (checkedListener != null) checkedListener.onCheckedChanged(container, checkedId);
          }
        });
    child.setOnClickListener(this);
    child.setOnLongClickListener(this);
    if (item.drawable != -1) {
      if (item.contentDescription != -1)
        child.setDrawableResource(item.drawable, getResources().getString(item.contentDescription));
      else child.setDrawableResource(item.drawable);
    }
    if (!TextUtils.isEmpty(item.name)) {
      child.setText(item.name);
      child.setTextColor(item.textColors);
      if (item.textSize != -1) child.setTextSize(item.textSize);
    }
    if (item.tint != -1) child.setDrawableColorList(item.tint);
    return child;
  }

  private void fillStyle(boolean first) {
    if (container == null) return;
    int size = items.size();
    if ((styleFlag & STYLE_ALIGN_EDGE) != 0 && first && size > 1) {
      ((LinearLayout.LayoutParams) container.getChildAt(0).getLayoutParams()).width =
          alignEdgeItemWidth;
      ((LinearLayout.LayoutParams) container.getChildAt(size - 1).getLayoutParams()).width =
          alignEdgeItemWidth;
      for (int i = 1, j = 1; i < size; i++, j += 2) {
        View spacer = new View(getContext());
        container.addView(spacer, j, new LinearLayout.LayoutParams(0, 1, 1));
      }
    } else if ((styleFlag & STYLE_BOTTOM_TAB) != 0 && size >= 3) {
      int gap =
          (availableWidth - getPaddingLeft() - getPaddingRight() - alignEdgeItemWidth * size)
              / (size - 1);
      for (int i = 0; i < size; i++) {
        LinearLayout.LayoutParams p =
            (LinearLayout.LayoutParams) container.getChildAt(i).getLayoutParams();
        p.width = alignEdgeItemWidth;
        p.leftMargin = i == 0 ? 0 : gap;
      }
    }
  }

  public View getShadowView() {
    return shadow;
  }

  public void setShadowViewVisible(boolean v) {
    shadow.setVisibility(v ? VISIBLE : GONE);
  }

  public void setOnCheckedChangeListener(OnCheckedChangeListener l) {
    checkedListener = l;
  }

  @Override
  public void setOnClickListener(View.OnClickListener l) {
    clickListener = l;
  }

  @Override
  public void setOnLongClickListener(View.OnLongClickListener l) {
    longClickListener = l;
  }

  @Override
  public void onClick(View v) {
    if (clickListener != null) clickListener.onClick(v);
  }

  @Override
  public boolean onLongClick(View v) {
    return longClickListener != null && longClickListener.onLongClick(v);
  }

  public BarItem addBarItem(int id, String name, int drawable, int tint, ColorStateList text) {
    BarItem item = new BarItem(id, name, drawable, tint, text);
    items.add(item);
    return item;
  }

  public BarItem addBarItem(int id, String name, int drawable, int tint) {
    return addBarItem(
        id,
        name,
        drawable,
        tint,
        getResources().getColorStateList(R.color.smartisan_rom_bottom_tab_text_color));
  }

  public BarItem addBarItem(int id, String name, int drawable) {
    return addBarItem(id, name, drawable, -1);
  }

  public void clearItems() {
    items.clear();
  }

  public final class BarItem {
    final int id, drawable, tint;
    final String name;
    final ColorStateList textColors;
    private int contentDescription = -1, textSize = -1;

    BarItem(int i, String n, int d, int t, ColorStateList c) {
      id = i;
      name = n;
      drawable = d;
      tint = t;
      textColors = c;
    }

    public void setContentDescription(int value) {
      contentDescription = value;
    }

    public void setTextSize(int value) {
      textSize = value;
    }
  }

  @Override
  protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
    super.onSizeChanged(width, height, oldWidth, oldHeight);
    if (width <= 0 || width == availableWidth || container == null) return;
    availableWidth = width;
    itemWidth = width / 5;
    for (int i = 0; i < container.getChildCount(); i++) {
      View view = container.getChildAt(i);
      if (view instanceof SmartisanBottomBarItemView) {
        LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) view.getLayoutParams();
        p.width = itemWidth;
        p.leftMargin = 0;
      }
    }
    fillStyle(false);
    container.requestLayout();
  }

  @Override
  public void setPadding(int left, int top, int right, int bottom) {
    super.setPadding(left, top, right, bottom);
    fillStyle(false);
  }

  @Override
  protected void onConfigurationChanged(Configuration config) {
    super.onConfigurationChanged(config);
    if (container == null) return;
    availableWidth = getWidth() > 0 ? getWidth() : getResources().getDisplayMetrics().widthPixels;
    itemWidth = availableWidth / 5;
    for (int i = 0; i < container.getChildCount(); i++) {
      View view = container.getChildAt(i);
      if (view instanceof SmartisanBottomBarItemView)
        view.setLayoutParams(
            new LinearLayout.LayoutParams(
                itemWidth, hasTextAndIcon ? heightWithTextIcon : heightOnlyIcon));
    }
    fillStyle(false);
  }
}
