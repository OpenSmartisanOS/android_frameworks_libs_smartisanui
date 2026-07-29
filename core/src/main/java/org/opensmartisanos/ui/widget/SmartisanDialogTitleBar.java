/* Ported from smartisanos.widget.MenuDialogTitleBar in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;
import org.opensmartisanos.ui.internal.SmartisanBarsHelper;

public class SmartisanDialogTitleBar extends LinearLayout {
    private final ImageView leftImageView;
    private final ImageView rightImageView;
    private final TextView titleView;
    private final ViewGroup titleBarContainer;
    private final View shadowView;
    private final View dividerView;
    private View.OnClickListener leftClickListener;
    private View.OnClickListener rightClickListener;
    private boolean requestAccessibilityFocus = true;

    public SmartisanDialogTitleBar(Context context) { this(context, null); }
    public SmartisanDialogTitleBar(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanDialogTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        titleBarContainer = new RelativeLayout(context);
        titleBarContainer.setBackgroundResource(R.drawable.smartisan_rom_bottom_sheet_title_bar_bg);
        titleBarContainer.setMinimumHeight(dp(48));
        addView(titleBarContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        leftImageView = iconView(context, R.drawable.smartisan_rom_standard_icon_cancel_selector);
        leftImageView.setId(View.generateViewId());
        leftImageView.setVisibility(INVISIBLE);
        RelativeLayout.LayoutParams left = iconParams();
        left.leftMargin = dp(6);
        left.addRule(RelativeLayout.ALIGN_PARENT_START);
        titleBarContainer.addView(leftImageView, left);

        rightImageView = iconView(context, R.drawable.smartisan_rom_standard_icon_cancel_selector);
        rightImageView.setId(View.generateViewId());
        RelativeLayout.LayoutParams right = iconParams();
        right.rightMargin = dp(6);
        right.addRule(RelativeLayout.ALIGN_PARENT_END);
        titleBarContainer.addView(rightImageView, right);

        titleView = new TextView(context);
        titleView.setId(View.generateViewId());
        titleView.setTextSize(13.5f);
        titleView.setTypeface(titleView.getTypeface(), android.graphics.Typeface.BOLD);
        titleView.setTextColor(0x99000000);
        titleView.setGravity(android.view.Gravity.CENTER);
        titleView.setMaxLines(2);
        titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        RelativeLayout.LayoutParams title = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        title.leftMargin = dp(6);
        title.rightMargin = dp(6);
        title.addRule(RelativeLayout.END_OF, leftImageView.getId());
        title.addRule(RelativeLayout.START_OF, rightImageView.getId());
        title.addRule(RelativeLayout.CENTER_VERTICAL);
        titleBarContainer.addView(titleView, title);

        shadowView = new View(context);
        shadowView.setBackgroundResource(R.drawable.smartisan_rom_title_bar_shadow);
        RelativeLayout.LayoutParams shadow = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, dp(14));
        shadow.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        shadowView.setTranslationY(dp(14));
        titleBarContainer.addView(shadowView, shadow);

        dividerView = new View(context);
        dividerView.setBackgroundResource(R.drawable.smartisan_rom_divider_bg);
        RelativeLayout.LayoutParams divider = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(
                        R.dimen.smartisan_rom_bar_divider_height));
        divider.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        dividerView.setTranslationY(divider.height);
        titleBarContainer.addView(dividerView, divider);
        titleBarContainer.addOnLayoutChangeListener((view, l, t, r, b,
                oldL, oldT, oldR, oldB) -> {
            ViewGroup parent = view.getParent() instanceof ViewGroup
                    ? (ViewGroup) view.getParent() : null;
            if (parent != null) parent.setClipChildren(false);
            ((ViewGroup) view).setClipToPadding(false);
        });
        setElevation(0.1f);
        leftImageView.setOnClickListener(v -> { if (leftClickListener != null) leftClickListener.onClick(v); });
        rightImageView.setOnClickListener(v -> { if (rightClickListener != null) rightClickListener.onClick(v); });
        SmartisanBarsHelper.setBarIconScaleTouchListener(leftImageView);
        SmartisanBarsHelper.setBarIconScaleTouchListener(rightImageView);
        installAccessibilityOrder();
    }

    private ImageView iconView(Context context, int drawable) {
        ImageView view = new ImageView(context);
        view.setImageResource(drawable);
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        view.setContentDescription(context.getString(android.R.string.cancel));
        return view;
    }

    private RelativeLayout.LayoutParams iconParams() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(dp(36), dp(36));
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        return params;
    }

    private void installAccessibilityOrder() {
        titleView.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setTraversalBefore(rightImageView);
            }
        });
        rightImageView.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setTraversalAfter(titleView);
                info.setTraversalBefore(leftImageView);
            }
        });
    }

    private int dp(float value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
    private void setImageResource(boolean left, int resource) {
        ImageView view = left ? leftImageView : rightImageView;
        view.setImageResource(resource);
        SmartisanBarsHelper.setBarIconScaleTouchListener(view);
    }

    public void addCancelImage(boolean left) { setImageResource(left, R.drawable.smartisan_rom_standard_icon_cancel_selector); }
    public void addCompleteImage(boolean left) { setImageResource(left, R.drawable.smartisan_rom_standard_icon_complete_selector); }
    public void forceRequestAccessibilityFocusWhenAttached(boolean request) { requestAccessibilityFocus = request; }
    public ImageView getLeftImageView() { return leftImageView; }
    public ImageView getRightImageView() { return rightImageView; }
    public ViewGroup getTitleBarContainer() { return titleBarContainer; }
    public TextView getTitleView() { return titleView; }
    public void setLeftButtonVisibility(int visibility) { leftImageView.setVisibility(visibility); }
    public void setRightButtonVisibility(int visibility) { rightImageView.setVisibility(visibility); }
    public void setLeftImageViewResource(int resource) { setImageResource(true, resource); }
    public void setRightImageViewResource(int resource) { setImageResource(false, resource); }
    public void setOnLeftButtonClickListener(View.OnClickListener listener) { leftClickListener = listener; }
    public void setOnRightButtonClickListener(View.OnClickListener listener) { rightClickListener = listener; }
    public void setShadowVisible(boolean visible) { shadowView.setVisibility(visible ? VISIBLE : GONE); }
    public void setTitle(int resource) { setTitle(getResources().getText(resource)); }
    public void setTitle(CharSequence title) { titleView.setText(title); }
    public void setTitleBarBackgroundResource(int resource) { titleBarContainer.setBackgroundResource(resource); }
    public void setTitleSingleLine(boolean singleLine) { titleView.setSingleLine(singleLine); }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getResources().getDimensionPixelSize(R.dimen.smartisan_rom_title_bar_height);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.setClassName(getClass().getName());
        event.setPackageName(getContext().getPackageName());
        return false;
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getParent() instanceof ViewGroup) ((ViewGroup) getParent()).setClipChildren(false);
        if (requestAccessibilityFocus) titleView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
    }
}
