/* Ported from smartisanos.widget.MenuDialogTitleBar in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

/** Direct public-SDK layout port of the original MenuDialogTitleBar. */
public class SmartisanDialogTitleBar extends LinearLayout {
    private final ImageView leftImageView;
    private final ImageView rightImageView;
    private final TextView titleView;
    private final ViewGroup titleBarContainer;
    private final View shadowView;
    private final View dividerView;
    // The original text-button APIs are deprecated no-ops. Keep detached views for API stability.
    private final TextView leftButtonView;
    private final TextView rightButtonView;
    private View.OnClickListener leftClickListener;
    private View.OnClickListener rightClickListener;
    private boolean requestAccessibilityFocus = true;
    private final float titleTextSizeSp;

    public SmartisanDialogTitleBar(Context context) { this(context, null); }
    public SmartisanDialogTitleBar(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanDialogTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.smartisan_rom_menu_dialog_title_bar,
                this, true);
        titleBarContainer = findViewById(R.id.smartisan_rom_menu_dialog_title_bar_container);
        titleView = findViewById(R.id.smartisan_rom_menu_dialog_title);
        leftImageView = findViewById(R.id.smartisan_rom_menu_dialog_cancel_left);
        rightImageView = findViewById(R.id.smartisan_rom_menu_dialog_cancel_right);
        leftButtonView = new TextView(context);
        rightButtonView = new TextView(context);
        titleTextSizeSp = titleView.getTextSize()
                / getResources().getDisplayMetrics().scaledDensity;

        TypedArray values = context.obtainStyledAttributes(attrs,
                R.styleable.SmartisanDialogTitleBar, defStyleAttr, 0);
        boolean showDivider = values.getBoolean(
                R.styleable.SmartisanDialogTitleBar_smartisanShowDivider, false);
        values.recycle();

        shadowView = new View(context);
        shadowView.setBackgroundResource(R.drawable.smartisan_rom_title_bar_shadow);
        RelativeLayout.LayoutParams shadow = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, dp(14));
        // The original BarsHelper anchors both overlays to the top while measuring a
        // wrap-content RelativeLayout, then translates them below the measured title bar.
        // ALIGN_PARENT_BOTTOM makes RelativeLayout consume the whole dialog height.
        shadow.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        shadowView.setTranslationY(dp(14));
        titleBarContainer.addView(shadowView, shadow);

        dividerView = new View(context);
        dividerView.setId(R.id.smartisan_rom_shadow_divider);
        dividerView.setBackgroundResource(R.drawable.smartisan_rom_divider_bg);
        RelativeLayout.LayoutParams divider = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(
                        R.dimen.smartisan_rom_bar_divider_height));
        divider.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        dividerView.setTranslationY(divider.height);
        dividerView.setVisibility(showDivider ? VISIBLE : GONE);
        titleBarContainer.addView(dividerView, divider);

        titleBarContainer.addOnLayoutChangeListener((view, l, t, r, b,
                oldL, oldT, oldR, oldB) -> {
            if (view.getParent() instanceof ViewGroup) {
                ((ViewGroup) view.getParent()).setClipChildren(false);
            }
            ((ViewGroup) view).setClipToPadding(false);
            shadowView.setTranslationY(view.getMeasuredHeight());
            dividerView.setTranslationY(view.getMeasuredHeight());
            shadowView.layout(0, shadowView.getTop(), view.getMeasuredWidth(),
                    shadowView.getBottom());
            dividerView.layout(0, dividerView.getTop(), view.getMeasuredWidth(),
                    dividerView.getBottom());
        });
        setElevation(0.1f);
        leftImageView.setOnClickListener(view -> {
            if (leftClickListener != null) leftClickListener.onClick(view);
        });
        rightImageView.setOnClickListener(view -> {
            if (rightClickListener != null) rightClickListener.onClick(view);
        });
        installOriginalIconTouch(leftImageView);
        installOriginalIconTouch(rightImageView);
        installAccessibilityOrder();
    }

    private void installAccessibilityOrder() {
        titleView.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override public void onInitializeAccessibilityNodeInfo(
                    View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setTraversalBefore(rightImageView);
            }
        });
        rightImageView.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            @Override public void onInitializeAccessibilityNodeInfo(
                    View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setTraversalAfter(titleView);
                info.setTraversalBefore(leftImageView);
            }
        });
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void setImageResource(boolean left, int resource) {
        ImageView view = left ? leftImageView : rightImageView;
        view.setImageResource(resource);
        installOriginalIconTouch(view);
    }

    private static void installOriginalIconTouch(View target) {
        target.setClickable(true);
        target.setOnTouchListener(new View.OnTouchListener() {
            private float endScale;

            private void animate(View view, boolean pressed) {
                float scale = pressed ? 1.33f : 1f;
                if (Float.compare(endScale, scale) == 0) return;
                endScale = scale;
                view.animate().scaleX(scale).scaleY(scale).setDuration(200L).start();
            }

            @Override public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        animate(view, true);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        animate(view, view.isPressed());
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        animate(view, false);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    public void addCancelImage(boolean left) {
        setImageResource(left, R.drawable.smartisan_rom_standard_icon_cancel_selector);
    }
    public void addCompleteImage(boolean left) {
        setImageResource(left, R.drawable.smartisan_rom_standard_icon_complete_selector);
    }
    public void forceRequestAccessibilityFocusWhenAttached(boolean request) {
        requestAccessibilityFocus = request;
    }
    public ImageView getLeftImageView() { return leftImageView; }
    public ImageView getRightImageView() { return rightImageView; }
    public ViewGroup getTitleBarContainer() { return titleBarContainer; }
    public TextView getTitleView() { return titleView; }
    public void setLeftButtonVisibility(int visibility) { leftImageView.setVisibility(visibility); }
    public void setRightButtonVisibility(int visibility) { rightImageView.setVisibility(visibility); }
    public void setLeftImageViewResource(int resource) { setImageResource(true, resource); }
    public void setLeftImageViewRes(int resource) { setLeftImageViewResource(resource); }
    public void setRightImageViewResource(int resource) { setImageResource(false, resource); }
    public void setRightImageRes(int resource) { setRightImageViewResource(resource); }

    /** Deprecated no-op retained from the original class. */
    @Deprecated public void setLeftButtonText(int resource) {
        setLeftButtonText(getResources().getText(resource));
    }
    /** Deprecated no-op retained from the original class. */
    @Deprecated public void setLeftButtonText(CharSequence text) { leftButtonView.setText(text); }
    /** Deprecated no-op retained from the original class. */
    @Deprecated public void setRightButtonText(int resource) {
        setRightButtonText(getResources().getText(resource));
    }
    /** Deprecated no-op retained from the original class. */
    @Deprecated public void setRightButtonText(CharSequence text) { rightButtonView.setText(text); }
    @Deprecated public TextView getLeftButton() { return new TextView(getContext()); }
    @Deprecated public TextView getRightButton() { return new TextView(getContext()); }
    @Deprecated public int getTopShadowHeight() { return 0; }
    @Deprecated public TextView getLeftButtonView() { return leftButtonView; }
    @Deprecated public TextView getRightButtonView() { return rightButtonView; }

    public void setOnLeftButtonClickListener(View.OnClickListener listener) {
        leftClickListener = listener;
    }
    public void setOnRightButtonClickListener(View.OnClickListener listener) {
        rightClickListener = listener;
    }
    public void setShadowVisible(boolean visible) {
        shadowView.setVisibility(visible ? VISIBLE : GONE);
    }
    public void setTitle(int resource) { setTitle(getResources().getText(resource)); }
    public void setTitle(CharSequence title) {
        titleView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, titleTextSizeSp);
        titleView.setText(title);
    }
    public void setTitleBarBackgroundResource(int resource) {
        titleBarContainer.setBackgroundResource(resource);
    }
    public void setTitleSingleLine(boolean singleLine) { titleView.setSingleLine(singleLine); }

    @Override public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        event.setClassName(getClass().getName());
        event.setPackageName(getContext().getPackageName());
        return false;
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getParent() instanceof ViewGroup) ((ViewGroup) getParent()).setClipChildren(false);
        if (requestAccessibilityFocus) {
            titleView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
        }
    }
}
