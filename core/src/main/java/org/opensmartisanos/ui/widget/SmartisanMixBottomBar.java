/* Ported from smartisanos.widget.MixBottomBar in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import org.opensmartisanos.ui.R;

public class SmartisanMixBottomBar extends LinearLayout {
  private final ImageButton left, center, right;

  public SmartisanMixBottomBar(Context c) {
    this(c, null);
  }

  public SmartisanMixBottomBar(Context c, AttributeSet a) {
    this(c, a, 0);
  }

  public SmartisanMixBottomBar(Context c, AttributeSet a, int s) {
    super(c, a, s);
    LayoutInflater.from(c).inflate(R.layout.smartisan_rom_mix_bottom_bar, this, true);
    left = findViewById(R.id.smartisan_rom_left_button);
    center = findViewById(R.id.smartisan_rom_center_button);
    right = findViewById(R.id.smartisan_rom_right_button);
    prepareButton(left);
    prepareButton(center);
    prepareButton(right);
    center.setMinimumWidth(
        getResources().getDimensionPixelSize(R.dimen.smartisan_rom_mix_bottom_bar_action_width));
    TypedArray v = c.obtainStyledAttributes(a, R.styleable.SmartisanMixBottomBar, s, 0);
    Drawable l = v.getDrawable(R.styleable.SmartisanMixBottomBar_leftButtonBackground),
        m = v.getDrawable(R.styleable.SmartisanMixBottomBar_centerButtonBackground),
        r = v.getDrawable(R.styleable.SmartisanMixBottomBar_rightButtonBackground);
    v.recycle();
    if (l != null) left.setBackground(l);
    if (m != null) center.setBackground(m);
    if (r != null) right.setBackground(r);
  }

  private static void prepareButton(ImageButton button) {
    button.setPadding(0, 0, 0, 0);
    button.setMinimumWidth(0);
    button.setMinimumHeight(0);
    button.setStateListAnimator(null);
  }

  public void setLeftButtonClickListener(View.OnClickListener l) {
    left.setOnClickListener(l);
  }

  public void setCenterButtonClickListener(View.OnClickListener l) {
    center.setOnClickListener(l);
  }

  public void setRightButtonClickListener(View.OnClickListener l) {
    right.setOnClickListener(l);
  }

  public View getLeftButton() {
    return left;
  }

  public View getCenterButton() {
    return center;
  }

  public View getRightButton() {
    return right;
  }
}
