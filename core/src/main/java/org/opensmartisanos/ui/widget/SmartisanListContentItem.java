/* Ported from smartisanos.widget.ListContentItem in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.SmartisanShadowDrawable;

public abstract class SmartisanListContentItem extends RelativeLayout {
    public static final int BG_STYLE_SINGLE = 1;
    public static final int BG_STYLE_TOP = 2;
    public static final int BG_STYLE_MIDDLE = 3;
    public static final int BG_STYLE_BOTTOM = 4;

    private final int[] backgrounds = {
            R.drawable.smartisan_rom_group_list_item_bg_single,
            R.drawable.smartisan_rom_group_list_item_bg_top,
            R.drawable.smartisan_rom_group_list_item_bg_mid,
            R.drawable.smartisan_rom_group_list_item_bg_bottom
    };
    private final int[] shadows = {
            R.drawable.smartisan_rom_list_content_item_single_shadow,
            R.drawable.smartisan_rom_list_content_item_top_shadow,
            R.drawable.smartisan_rom_list_content_item_middle_shadow,
            R.drawable.smartisan_rom_list_content_item_bottom_shadow
    };

    protected ImageView icon;
    protected FrameLayout leftContainer;
    protected LinearLayout midContainer;
    protected LinearLayout rightContainer;
    protected TextView title;
    protected TextView summary;
    protected boolean customMidView;
    protected boolean customRightView;
    protected boolean pressable;
    protected int disabledReasonStringId;
    protected Toast toast;
    private int backgroundStyle = -1;
    private boolean shadowEnabled = true;
    private boolean shadowShouldProject = true;
    private OnDisabledClickListener disabledClickListener;

    public interface OnDisabledClickListener {
        void onDisabledClick();
    }

    protected abstract int getDefaultRightLayout();
    protected abstract void initRightWidget();

    public SmartisanListContentItem(Context context) {
        this(context, null);
    }

    public SmartisanListContentItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmartisanListContentItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.smartisan_rom_list_content_item_layout, this, true);
        leftContainer = root.findViewById(R.id.smartisan_rom_left_container);
        midContainer = root.findViewById(R.id.smartisan_rom_mid_container);
        rightContainer = root.findViewById(R.id.smartisan_rom_right_container);

        TypedArray values = context.obtainStyledAttributes(attrs,
                R.styleable.SmartisanListContentItem, defStyleAttr, 0);
        int iconId = values.getResourceId(R.styleable.SmartisanListContentItem_icon, -1);
        String titleText = values.getString(R.styleable.SmartisanListContentItem_title);
        String summaryText = values.getString(R.styleable.SmartisanListContentItem_summary);
        backgroundStyle = values.getInt(
                R.styleable.SmartisanListContentItem_backgroundStyle, -1);
        shadowEnabled = values.getBoolean(
                R.styleable.SmartisanListContentItem_shadow_enabled, true);
        shadowShouldProject = values.getBoolean(
                R.styleable.SmartisanListContentItem_shadow_project, true);
        pressable = values.getBoolean(R.styleable.SmartisanListContentItem_isPressable, true);
        int customMid = values.getResourceId(
                R.styleable.SmartisanListContentItem_customMidView, -1);
        int customRight = values.getResourceId(
                R.styleable.SmartisanListContentItem_customRightView, -1);
        values.recycle();
        customMidView = customMid > 0;
        customRightView = customRight > 0;

        inflateView(getDefaultLeftLayout(), leftContainer, inflater);
        inflateView(customMidView ? customMid : getDefaultMidLayout(), midContainer, inflater);
        inflateView(customRightView ? customRight : getDefaultRightLayout(), rightContainer,
                inflater);
        initViews();
        setTitle(titleText);
        setSummary(summaryText);
        setIcon(iconId);
        if (backgroundStyle != -1 && getBackground() != null) {
            setBackground(getBackground());
        }
    }

    protected void inflateView(int layout, ViewGroup container, LayoutInflater inflater) {
        if (layout > 0) {
            inflater.inflate(layout, container);
        }
    }

    private void initViews() {
        icon = findViewById(R.id.smartisan_rom_left_icon);
        if (!customMidView) initMidWidget();
        if (!customRightView) {
            initRightWidget();
        }
    }

    protected int getDefaultLeftLayout() {
        return R.layout.smartisan_rom_list_content_left_image_view;
    }

    protected int getDefaultMidLayout() {
        return R.layout.smartisan_rom_list_content_mid_primary_2line;
    }

    protected void initMidWidget() {
        title = findViewById(R.id.smartisan_rom_item_title);
        summary = findViewById(R.id.smartisan_rom_item_summary);
    }

    public void setMaxTitleSize(float maxTextSize) {
        fitText(title, maxTextSize);
    }

    public void setMaxSummarySize(float maxTextSize) {
        fitText(summary, maxTextSize);
    }

    private static void fitText(TextView view, float minSize) {
        if (view == null || view.getText() == null) {
            return;
        }
        float available = view.getWidth() - view.getPaddingLeft() - view.getPaddingRight();
        float size = view.getTextSize();
        while (available > 0 && size > minSize
                && view.getPaint().measureText(view.getText().toString()) > available) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, --size);
        }
    }

    public void setOnDisabledClickListener(OnDisabledClickListener listener) {
        disabledClickListener = listener;
    }

    public void setTitle(CharSequence value) {
        if (title != null) {
            title.setText(value);
        }
    }

    public void setTitle(int resId) { setTitle(getContext().getString(resId)); }
    public CharSequence getTitle() { return title == null ? null : title.getText(); }
    public TextView getTitleView() { return title; }

    public void setSummary(CharSequence value) {
        if (summary != null) {
            summary.setText(value);
            summary.setVisibility(TextUtils.isEmpty(value) ? GONE : VISIBLE);
        }
    }

    public void setSummary(int resId) { setSummary(getContext().getString(resId)); }
    public CharSequence getSummary() { return summary == null ? null : summary.getText(); }
    public TextView getSummaryView() { return summary; }

    public void setIcon(int resId) {
        if (resId > 0 && icon != null) {
            icon.setImageResource(resId);
        }
        setLeftContainerVisible(resId > 0);
    }

    public void setIcon(Drawable drawable) {
        if (icon != null) {
            icon.setImageDrawable(drawable);
        }
        setLeftContainerVisible(drawable != null);
    }

    public ImageView getIconView() { return icon; }
    public void setDisabledTips(int stringId) { disabledReasonStringId = stringId; }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return shouldHandleDisabledTouchEvent(event) || super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!shouldHandleDisabledTouchEvent(event)) {
            return super.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (disabledClickListener != null) {
                disabledClickListener.onDisabledClick();
            }
            if (disabledReasonStringId > 0) {
                if (toast != null) {
                    toast.cancel();
                }
                toast = Toast.makeText(getContext(), disabledReasonStringId, Toast.LENGTH_LONG);
                toast.show();
            }
        }
        return true;
    }

    protected boolean shouldHandleDisabledTouchEvent(MotionEvent event) {
        if (isEnabled() || (disabledClickListener == null && disabledReasonStringId <= 0)) {
            return false;
        }
        Rect bounds = new Rect();
        getGlobalVisibleRect(bounds);
        return bounds.contains((int) event.getRawX(), (int) event.getRawY());
    }

    public void cancelToast() {
        if (toast != null) {
            toast.cancel();
        }
    }

    public void setLeftContainerVisible(boolean visible) {
        leftContainer.setVisibility(visible ? VISIBLE : GONE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) midContainer.getLayoutParams();
        if (visible) {
            params.removeRule(ALIGN_PARENT_LEFT);
            params.addRule(RIGHT_OF, R.id.smartisan_rom_left_container);
        } else {
            params.addRule(ALIGN_PARENT_LEFT);
        }
        midContainer.setLayoutParams(params);
        setMidContentPaddingLeft(visible ? 0
                : getResources().getDimensionPixelSize(R.dimen.smartisan_rom_flexible_space));
    }

    protected void setMidContainerWidth(int width) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) midContainer.getLayoutParams();
        params.width = width;
        midContainer.setLayoutParams(params);
    }

    protected boolean rightContainerHasFixedWidth() { return true; }
    protected float getLeftContentWidth() {
        return leftContainer.getVisibility() == VISIBLE
                ? getResources().getDimensionPixelSize(R.dimen.smartisan_rom_left_icon_area_width) : 0f;
    }
    protected float getMidContentWidth() {
        return Math.max(textWidth(title), textWidth(summary)) + midContainer.getPaddingLeft();
    }
    protected static float textWidth(TextView view) {
        return view == null || view.getText() == null ? 0f
                : view.getPaint().measureText(view.getText().toString());
    }
    protected void setMidContentPaddingLeft(int padding) {
        midContainer.setPadding(padding, midContainer.getPaddingTop(),
                midContainer.getPaddingRight(), midContainer.getPaddingBottom());
    }
    protected float getRightContentWidth() { throw new IllegalStateException("calculate your width"); }

    public void setBackgroundStyle(int style) {
        if (backgroundStyle != style && style >= BG_STYLE_SINGLE && style <= BG_STYLE_BOTTOM) {
            backgroundStyle = style;
            setBackgroundResource(backgrounds[style - 1]);
        }
    }

    @Override
    public void setPressed(boolean pressed) {
        if (!pressable) {
            return;
        }
        boolean fromParent = pressed && !isClickable() && !isLongClickable();
        if (!fromParent || isInsideScrollingContainer()) {
            super.setPressed(pressed);
        }
    }

    private boolean isInsideScrollingContainer() {
        ViewParent parent = getParent();
        while (parent instanceof View) {
            if (parent instanceof AbsListView || parent instanceof ScrollView
                    || parent instanceof HorizontalScrollView
                    || ((View) parent).isNestedScrollingEnabled()) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    @Override
    public void setBackground(Drawable background) {
        Drawable current = getBackground();
        if (current instanceof SmartisanShadowDrawable
                && ((SmartisanShadowDrawable) current).getBackgroundDrawable() == background) {
            ((SmartisanShadowDrawable) current).setProjectBackwards(shadowShouldProject);
            return;
        }
        if (shadowEnabled && !(background instanceof SmartisanShadowDrawable)
                && backgroundStyle >= BG_STYLE_SINGLE && backgroundStyle <= BG_STYLE_BOTTOM) {
            SmartisanShadowDrawable.setViewBackground(this, background,
                    shadows[backgroundStyle - 1], shadowShouldProject);
        } else {
            super.setBackground(background);
        }
    }

    public void setPressable(boolean value) { pressable = value; }
    public boolean isPressable() { return pressable; }
    public void setShadowEnabled(boolean enabled) { shadowEnabled = enabled; }
    public void setShadowShouldProjects(boolean projects) { shadowShouldProject = projects; }
}
