/* Ported from smartisanos.widget.BottomBarItemView in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.dynamicanimation.DynamicAnimation;
import org.opensmartisanos.ui.internal.dynamicanimation.SpringAnimation;
import org.opensmartisanos.ui.internal.dynamicanimation.SpringForce;

public class SmartisanBottomBarItemView extends LinearLayout implements Checkable {
    private static final int[] CHECKED_STATE = {android.R.attr.state_checked};
    public interface OnCheckedChangeListener { void onCheckedChanged(SmartisanBottomBarItemView view, boolean checked); }
    private ImageView imageView; private TextView textView; private boolean checked, broadcasting, scalable;
    private OnCheckedChangeListener listener;
    public SmartisanBottomBarItemView(Context c) { this(c, null); }
    public SmartisanBottomBarItemView(Context c, AttributeSet a) { this(c, a, 0); }
    public SmartisanBottomBarItemView(Context c, AttributeSet a, int s) { super(c, a, s); setGravity(Gravity.CENTER); setOrientation(VERTICAL); setClickable(true); }
    public void setDrawableResource(int id) { image().setImageResource(id); }
    public void setDrawableResource(int id, String description) { setDrawableResource(id); image().setContentDescription(description); }
    public void setDrawableColorList(int id) { Drawable d = image().getDrawable(); if (scalable && d != null) d.setTintList(getResources().getColorStateList(id)); }
    public void setTextColor(ColorStateList colors) { text().setTextColor(colors); }
    public void setTextSize(float size) { text().setTextSize(size); }
    public void setText(String value) { if (value != null && !value.isEmpty()) text().setText(value); }
    public void setScaleable(boolean value) { scalable = value; }
    private ImageView image() { if (imageView == null) { imageView = new ImageView(getContext()); imageView.setDuplicateParentStateEnabled(true); if (scalable) imageView.setScaleX(.9f); if (scalable) imageView.setScaleY(.9f); addView(imageView, new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)); } return imageView; }
    private TextView text() { if (textView == null) { textView = new TextView(getContext()); textView.setTextSize(9); textView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL); textView.setSingleLine(); textView.setTypeface(Typeface.DEFAULT_BOLD); textView.setDuplicateParentStateEnabled(true); LayoutParams p = new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT); p.topMargin = getResources().getDimensionPixelOffset(R.dimen.smartisan_rom_smartisan_bottom_bar_drawablePadding); if (scalable) { textView.setScaleX(.9f); textView.setScaleY(.9f); } addView(textView, p); } return textView; }
    public void setOnCheckedChangeListener(OnCheckedChangeListener value) { listener = value; }
    @Override protected int[] onCreateDrawableState(int extra) { int[] state = super.onCreateDrawableState(extra + 1); if (checked) mergeDrawableStates(state, CHECKED_STATE); return state; }
    @Override public void setChecked(boolean value) { if (checked == value) return; checked = value; refreshDrawableState(); animateScale(value ? 1f : .9f); if (!broadcasting) { broadcasting = true; if (listener != null) listener.onCheckedChanged(this, checked); broadcasting = false; } }
    @Override public boolean isChecked() { return checked; }
    @Override public void toggle() { if (!checked) setChecked(true); }
    @Override public boolean performClick() { toggle(); return super.performClick(); }
    @Override public boolean onTouchEvent(MotionEvent event) { if (event.getActionMasked() == MotionEvent.ACTION_DOWN) animateScale(1.1f); else if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) animateScale(checked ? 1f : .9f); return super.onTouchEvent(event); }
    private void animateScale(float scale) { if (!scalable) return; SpringForce force = new SpringForce(scale).setDampingRatio(.55f).setStiffness(800f); if (imageView != null) { new SpringAnimation(imageView, DynamicAnimation.SCALE_X).setSpring(force).start(); new SpringAnimation(imageView, DynamicAnimation.SCALE_Y).setSpring(force).start(); } if (textView != null) { new SpringAnimation(textView, DynamicAnimation.SCALE_X).setSpring(force).start(); new SpringAnimation(textView, DynamicAnimation.SCALE_Y).setSpring(force).start(); } }
}
