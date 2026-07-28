/* Ported from smartisanos.widget.TitleBar in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opensmartisanos.ui.R;

import java.util.ArrayList;
import java.util.List;

public class SmartisanTitleBar extends RelativeLayout {
    public static final int BACK_ICON_RES = R.drawable.smartisan_rom_standard_icon_back_selector;

    private final Context context;
    private final int marginEdge;
    private final int marginView;
    private final int imageViewSize;
    private final int centerLimit;
    private final int defaultTextSize;
    private int titleBarHeight;
    private final List<View> leftViews = new ArrayList<>();
    private final List<View> rightViews = new ArrayList<>();
    private View leftLastView;
    private View rightLastView;
    private View centerView;
    private TextView titleView;
    private View shadowView;
    private View dividerView;
    private boolean hasLimit;
    private boolean autoAdjustCenterVisibility = true;
    private boolean imageScaleEnabled = true;
    private boolean autoAdapterEnabled;
    private ValueAnimator navigationAnimator;

    public SmartisanTitleBar(Context context) { this(context, null); }
    public SmartisanTitleBar(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SmartisanTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        marginEdge = getResources().getDimensionPixelOffset(R.dimen.smartisan_rom_bar_margin_edge);
        marginView = getResources().getDimensionPixelOffset(R.dimen.smartisan_rom_title_bar_margin_view);
        imageViewSize = getResources().getDimensionPixelOffset(R.dimen.smartisan_rom_standard_icon_size);
        titleBarHeight = getResources().getDimensionPixelOffset(R.dimen.smartisan_rom_title_bar_height);
        centerLimit = getResources().getDimensionPixelOffset(R.dimen.smartisan_rom_title_bar_center_limit);
        defaultTextSize = getResources().getDimensionPixelOffset(R.dimen.smartisan_rom_title_bar_title_size);
        TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.SmartisanTitleBar,
                defStyleAttr, 0);
        if (values.hasValue(R.styleable.SmartisanTitleBar_titleBarCenterText)) {
            setCenterText(values.getString(R.styleable.SmartisanTitleBar_titleBarCenterText));
        }
        boolean bottomType = values.getBoolean(
                R.styleable.SmartisanTitleBar_titleBarBottomType, false);
        values.recycle();
        setBackgroundColor(0xffffffff);
        addShadowAndDivider(bottomType);
        setElevation(0.11f);
    }

    private void addShadowAndDivider(boolean bottomType) {
        int shadowHeight = getResources().getDimensionPixelOffset(bottomType
                ? R.dimen.smartisan_rom_bottom_bar_shadow_height
                : R.dimen.smartisan_rom_title_bar_shadow_height);
        int dividerHeight = getResources().getDimensionPixelSize(
                R.dimen.smartisan_rom_bar_divider_height);
        shadowView = new View(context);
        shadowView.setBackgroundResource(bottomType ? R.drawable.smartisan_rom_bottom_bar_shadow
                : R.drawable.smartisan_rom_title_bar_shadow);
        LayoutParams shadowParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, shadowHeight);
        shadowParams.addRule(bottomType ? ALIGN_PARENT_TOP : ALIGN_PARENT_BOTTOM);
        shadowView.setTranslationY(bottomType ? -shadowHeight : shadowHeight);
        addView(shadowView, shadowParams);
        dividerView = new View(context);
        dividerView.setBackgroundResource(R.drawable.smartisan_rom_divider_bg);
        LayoutParams dividerParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight);
        dividerParams.addRule(bottomType ? ALIGN_PARENT_TOP : ALIGN_PARENT_BOTTOM);
        dividerView.setTranslationY(bottomType ? -dividerHeight : dividerHeight);
        addView(dividerView, dividerParams);
        addOnLayoutChangeListener((view, l, t, r, b, oldL, oldT, oldR, oldB) -> {
            ViewGroup parent = getParent() instanceof ViewGroup ? (ViewGroup) getParent() : null;
            if (parent != null) parent.setClipChildren(false);
            setClipToPadding(false);
        });
    }

    public void setShadowVisible(boolean visible) { shadowView.setVisibility(visible ? VISIBLE : GONE); }
    public View getShadowView() { return shadowView; }
    public void setCenterView(View view) {
        if (view != null) setCenterView(view, findLayoutParams(view, true));
    }
    private void setCenterView(View view, LayoutParams params) {
        if (centerView == view) return;
        removeCenterView();
        centerView = view;
        addView(view, params);
    }
    public void setAutoAdjustCenterViewVisibility(boolean auto) { autoAdjustCenterVisibility = auto; }
    private void setCenterViewVisibility(int visibility) {
        if (autoAdjustCenterVisibility && centerView != null) centerView.setVisibility(visibility);
    }
    public boolean isCenterTitleTextView() { return centerView != null && centerView == titleView; }
    public void setCenterText(int resId) { setCenterText(context.getString(resId)); }
    public void setCenterText(String title) { getTitleViewOrCreate().setText(title); }
    public void setCenterTextColor(int color) { getTitleViewOrCreate().setTextColor(color); }
    public TextView getTitleView() { return titleView; }

    private TextView getTitleViewOrCreate() {
        if (centerView == titleView && titleView != null) return titleView;
        if (titleView == null) {
            titleView = new TextView(context);
            titleView.setMaxLines(1);
            titleView.setEllipsize(TextUtils.TruncateAt.END);
            titleView.setTextSize(defaultTextSize);
            titleView.setPaintFlags(33);
            titleView.setTextColor(getResources().getColor(R.color.smartisan_rom_title_bar_text));
        }
        removeCenterView();
        centerView = titleView;
        addView(titleView, centerLayoutParams());
        return titleView;
    }
    private void removeCenterView() {
        if (centerView != null) { removeView(centerView); centerView = null; }
    }
    private LayoutParams centerLayoutParams() {
        LayoutParams params = defaultLayoutParams();
        params.addRule(CENTER_IN_PARENT);
        return params;
    }
    public LayoutParams generateSingleCenterViewLayoutParams(boolean needMargin) {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.leftMargin = needMargin ? marginEdge : 0;
        params.rightMargin = needMargin ? marginEdge : 0;
        return params;
    }

    public void addLeftView(View view) { addSideView(view, findLayoutParams(view, false), -1, true); }
    public void addRightView(View view) { addSideView(view, findLayoutParams(view, false), -1, false); }
    private void addSideView(View view, LayoutParams params, int index, boolean left) {
        if (hasLimit) return;
        List<View> list = left ? leftViews : rightViews;
        View last = left ? leftLastView : rightLastView;
        params.alignWithParent = true;
        view.setId(View.generateViewId());
        if (last == null) {
            index = 0;
            int margin = view instanceof SmartisanButton ? 0 : marginEdge;
            if (left) { params.leftMargin = margin; params.addRule(ALIGN_PARENT_LEFT); }
            else { params.rightMargin = margin; params.addRule(ALIGN_PARENT_RIGHT); }
        } else if (index < 0 || index >= list.size()) {
            index = list.size();
            params.addRule(left ? RIGHT_OF : LEFT_OF, last.getId());
            if (left) params.leftMargin = marginView; else params.rightMargin = marginView;
        } else {
            View before = index == 0 ? null : list.get(index - 1);
            View next = list.get(index);
            if (before == null) params.addRule(left ? ALIGN_PARENT_LEFT : ALIGN_PARENT_RIGHT);
            else params.addRule(left ? RIGHT_OF : LEFT_OF, before.getId());
            LayoutParams nextParams = (LayoutParams) next.getLayoutParams();
            nextParams.addRule(left ? RIGHT_OF : LEFT_OF, view.getId());
        }
        params.addRule(CENTER_VERTICAL);
        list.add(index, view);
        if (index == list.size() - 1) {
            if (left) leftLastView = view; else rightLastView = view;
        }
        addView(view, params);
    }

    public void removeAllLeftViews() { removeAllSideViews(leftViews, true); }
    public void removeAllRightViews() { removeAllSideViews(rightViews, false); }
    public void removeLeftView(int index) { removeSideView(leftViews, index, true); }
    public void removeRightView(int index) { removeSideView(rightViews, index, false); }
    private void removeSideView(List<View> list, int index, boolean left) {
        if (index < 0 || index >= list.size()) return;
        View removed = list.remove(index);
        removeView(removed);
        if (left) leftLastView = list.isEmpty() ? null : list.get(list.size() - 1);
        else rightLastView = list.isEmpty() ? null : list.get(list.size() - 1);
        for (int i = 0; i < list.size(); i++) {
            View child = list.get(i);
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            params.removeRule(left ? RIGHT_OF : LEFT_OF);
            params.removeRule(left ? ALIGN_PARENT_LEFT : ALIGN_PARENT_RIGHT);
            if (i == 0) params.addRule(left ? ALIGN_PARENT_LEFT : ALIGN_PARENT_RIGHT);
            else params.addRule(left ? RIGHT_OF : LEFT_OF, list.get(i - 1).getId());
            child.setLayoutParams(params);
        }
    }
    private void removeAllSideViews(List<View> list, boolean left) {
        for (View view : list) removeView(view);
        list.clear();
        if (left) leftLastView = null; else rightLastView = null;
    }
    public ImageView addLeftImageView(int drawableId) { return addImage(drawableId, true); }
    public ImageView addRightImageView(int drawableId) { return addImage(drawableId, false); }
    public ImageView addLeftImageView(int drawableId, int index) { return addImage(drawableId, index, true); }
    public ImageView addRightImageView(int drawableId, int index) { return addImage(drawableId, index, false); }
    private ImageView addImage(int drawableId, boolean left) {
        return addImage(drawableId, -1, left);
    }
    private ImageView addImage(int drawableId, int index, boolean left) {
        ImageView view = generateImageView(drawableId);
        addSideView(view, new LayoutParams(imageViewSize, imageViewSize), index, left);
        return view;
    }
    public SmartisanButton addLeftButton(int style, int text) { return addButton(style, text, true); }
    public SmartisanButton addRightButton(int style, int text) { return addButton(style, text, false); }
    private SmartisanButton addButton(int style, int text, boolean left) {
        SmartisanButton button = new SmartisanButton(context);
        button.setButtonStyle(style);
        button.setButtonText(text);
        if (left) addLeftView(button); else addRightView(button);
        return button;
    }
    public boolean hasLeftView() { return visibleCount(leftViews) > 0; }
    public boolean hasRightView() { return visibleCount(rightViews) > 0; }
    public int getLeftViewCount() { return visibleCount(leftViews); }
    public int getRightViewCount() { return visibleCount(rightViews); }
    public View getLeftViewByIndex(int index) { return visibleAt(leftViews, index); }
    public View getRightViewByIndex(int index) { return visibleAt(rightViews, index); }
    private static int visibleCount(List<View> views) {
        int count = 0; for (View view : views) if (view.getVisibility() != GONE) count++; return count;
    }
    private static View visibleAt(List<View> views, int index) {
        int current = 0;
        for (View view : views) if (view.getVisibility() != GONE && current++ == index) return view;
        return null;
    }

    protected ImageView generateImageView(int drawableId) {
        ImageView view = new ImageView(context);
        view.setImageResource(drawableId);
        if (imageScaleEnabled) setBarIconScaleTouchListener(view, true);
        return view;
    }
    private static void setBarIconScaleTouchListener(View target, boolean enabled) {
        org.opensmartisanos.ui.internal.SmartisanBarsHelper
                .setBarIconScaleTouchListener(target, enabled);
    }
    private LayoutParams findLayoutParams(View view, boolean center) {
        return view.getLayoutParams() instanceof LayoutParams ? (LayoutParams) view.getLayoutParams()
                : center ? centerLayoutParams() : defaultLayoutParams();
    }
    private LayoutParams defaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int exactHeight = MeasureSpec.makeMeasureSpec(titleBarHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, exactHeight);
        if (centerView == null) return;
        int leftWidth = sideWidth(leftViews, true);
        int rightWidth = sideWidth(rightViews, false);
        int max = Math.max(leftWidth, rightWidth);
        int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int valid = width - max * 2 + (max == 0 ? 0 : marginView * 2);
        int realValid = width - leftWidth - rightWidth;
        if (max > centerLimit) setCenterViewVisibility(GONE);
        else {
            setCenterViewVisibility(VISIBLE);
            if (centerView.getMeasuredWidth() > valid) {
                if (centerView == titleView) titleView.setMaxWidth(valid);
                else centerView.getLayoutParams().width = valid;
                super.onMeasure(widthMeasureSpec, exactHeight);
            }
        }
        hasLimit = realValid < imageViewSize;
    }
    private int sideWidth(List<View> views, boolean left) {
        int total = 0, visible = 0;
        for (View child : views) if (child.getVisibility() != GONE) {
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            int margin = visible++ == 0 ? (child instanceof SmartisanButton ? 0 : marginEdge) : marginView;
            if (left) params.leftMargin = margin; else params.rightMargin = margin;
            total += child.getMeasuredWidth() + marginView;
        }
        return total;
    }

    public void setAutoAdapterEnabled(boolean enabled) { autoAdapterEnabled = enabled; }
    public boolean isAutoAdapterEnabled() { return autoAdapterEnabled; }
    public void applyNavigationBarVisibility(boolean shown, int hiddenHorizontalPadding) {
        if (navigationAnimator != null) navigationAnimator.cancel();
        navigationAnimator = ValueAnimator.ofInt(getPaddingLeft(), shown ? 0 : hiddenHorizontalPadding);
        navigationAnimator.setDuration(200L);
        navigationAnimator.setInterpolator(new DecelerateInterpolator());
        navigationAnimator.addUpdateListener(value -> {
            int padding = (Integer) value.getAnimatedValue();
            setPadding(padding, getPaddingTop(), padding, getPaddingBottom());
        });
        navigationAnimator.start();
    }
    public void setTitleBarHeight(int height) { titleBarHeight = height; }
    public void setImageScaleEnable(boolean enabled) { imageScaleEnabled = enabled; }
    public void avoidImageViewScale(ImageView target) { setBarIconScaleTouchListener(target, false); }
}
