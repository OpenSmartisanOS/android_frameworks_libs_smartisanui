/* Ported from smartisanos.widget.SmartisanBottomBar in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.SmartisanShadowComponent;

public class SmartisanIconBottomBar extends LinearLayout {
  public interface BottomBarInnerClickCallback {
    void onClick(int index, View view);
  }

  private BottomBarInnerClickCallback callback;
  private ImageButton[] buttons;
  private int[] icons;
  private boolean backgroundMode;
  private int width, buttonMargin;
  private final int fixedHeight;
  private final SmartisanShadowComponent shadowComponent;

  public SmartisanIconBottomBar(Context c) {
    this(c, null);
  }

  public SmartisanIconBottomBar(Context c, AttributeSet a) {
    this(c, a, 0);
  }

  public SmartisanIconBottomBar(Context c, AttributeSet a, int s) {
    super(c, a, s);
    setGravity(Gravity.CENTER_VERTICAL);
    TypedArray v = c.obtainStyledAttributes(a, R.styleable.SmartisanIconBottomBar);
    int array = v.getResourceId(R.styleable.SmartisanIconBottomBar_bottomBarIconArray, -1);
    backgroundMode =
        v.getBoolean(R.styleable.SmartisanIconBottomBar_bottomBarIconShowBackgroundMode, false);
    v.recycle();
    setBackgroundResource(R.drawable.smartisan_rom_bottom_bar);
    int p =
        getResources()
            .getDimensionPixelSize(R.dimen.smartisan_rom_smartisan_small_blank_spacing_width);
    setPadding(p, 0, p, 0);
    fixedHeight = getResources().getDimensionPixelSize(R.dimen.smartisan_button_fixed_height);
    shadowComponent =
        new SmartisanShadowComponent(
            this,
            R.drawable.smartisan_rom_bottom_bar_shadow,
            true,
            SmartisanShadowComponent.ORIENTATION_ABOVE_HOST);
    if (array >= 0) setIconRefArray(array, backgroundMode);
  }

  public void setBottomInnerClickListener(BottomBarInnerClickCallback value) {
    callback = value;
  }

  @Override
  protected void onSizeChanged(int w, int h, int ow, int oh) {
    super.onSizeChanged(w, h, ow, oh);
    post(this::buildItems);
  }

  private void buildItems() {
    if (buttons == null || getWidth() <= 0 || getWidth() == width) return;
    width = getWidth();
    int all = 0;
    for (ImageButton b : buttons) {
      b.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
      boolean square = b.getMeasuredWidth() == b.getMeasuredHeight();
      b.setTag(square);
      all += square ? fixedHeight : b.getMeasuredWidth();
    }
    buttonMargin =
        buttons.length > 1
            ? (width - getPaddingLeft() - getPaddingRight() - all) / (buttons.length - 1)
            : 0;
    for (int i = 0; i < buttons.length; i++) {
      LayoutParams p =
          new LayoutParams(
              Boolean.TRUE.equals(buttons[i].getTag())
                  ? fixedHeight
                  : android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
              fixedHeight);
      p.gravity = buttons.length == 1 ? Gravity.CENTER : Gravity.CENTER_VERTICAL;
      p.leftMargin = i == 0 ? 0 : buttonMargin;
      buttons[i].setLayoutParams(p);
    }
  }

  private void createItems() {
    removeAllViews();
    buttons = new ImageButton[icons.length];
    for (int i = 0; i < icons.length; i++) {
      final int index = i;
      ImageButton b = new ImageButton(getContext());
      b.setBackground(null);
      b.setPadding(0, 0, 0, 0);
      b.setMinimumWidth(0);
      b.setMinimumHeight(0);
      b.setStateListAnimator(null);
      if (backgroundMode) b.setBackgroundResource(icons[i]);
      else b.setImageResource(icons[i]);
      b.setOnClickListener(
          v -> {
            if (callback != null) callback.onClick(index, v);
          });
      buttons[i] = b;
      addView(b);
    }
  }

  public void setIconRefArray(int[] values, boolean mode) {
    if (values == null || values.length == 0) return;
    icons = values;
    backgroundMode = mode;
    createItems();
    buildItems();
  }

  public void setIconRefArray(int[] values) {
    setIconRefArray(values, false);
  }

  public void setIconRefArray(int array, boolean mode) {
    TypedArray a = getResources().obtainTypedArray(array);
    int[] values = new int[a.length()];
    for (int i = 0; i < values.length; i++) values[i] = a.getResourceId(i, -1);
    a.recycle();
    setIconRefArray(values, mode);
  }

  public void setIconRefArray(int array) {
    setIconRefArray(array, false);
  }

  public ImageButton getBottomItem(int i) {
    return buttons == null ? null : buttons[i];
  }

  public ImageButton[] getBottomItems() {
    return buttons;
  }

  public void setShadowDrawable(int id) {
    shadowComponent.setShadowDrawable(id);
  }

  public void setShadowVisible(boolean visible) {
    shadowComponent.setShadowVisible(visible);
  }

  public ImageView getShadowView() {
    return shadowComponent.getShadowView();
  }
}
