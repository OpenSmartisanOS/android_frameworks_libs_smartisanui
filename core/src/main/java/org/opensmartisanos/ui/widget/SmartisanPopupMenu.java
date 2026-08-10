package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;

import org.opensmartisanos.ui.R;

/** Public-SDK port of the directional Smartisan popup-menu surface. */
public abstract class SmartisanPopupMenu {
    public static final int ARROW_TOP = 0;
    public static final int ARROW_BOTTOM = 1;
    public static final int ARROW_LEFT = 2;
    public static final int ARROW_RIGHT = 3;
    public static final int DIRECTION_TOP = ARROW_TOP;
    public static final int DIRECTION_BOTTOM = ARROW_BOTTOM;
    public static final int DIRECTION_LEFT = ARROW_LEFT;
    public static final int DIRECTION_RIGHT = ARROW_RIGHT;

    // Keep the original R2 protected surface. A number of applications subclassed the popup
    // implementations and accessed these fields directly.
    protected Context mContext;
    protected PopupWindow mPopupWindow;
    protected View mAnchorView;
    protected View mMenuPanelView;
    protected int mContentAreaWidth;
    protected int mBgLeftRightShadowWidth;
    protected int mBgTopBottomShadowHeight;

    private final int arrowHeight;
    private final int arrowWidth;
    private final int menuPanelRoundCornerRadius;
    private final int minDistance;
    private PopupWindow.OnDismissListener dismissListener;
    private boolean showAboveAnchor;
    private boolean requestFocusable = true;
    private boolean arrowVisible;
    private boolean clipToScreen = true;
    private boolean menuPopup;
    private boolean autoAdjustPopupDirection;

    public SmartisanPopupMenu(Context context) {
        mContext = context;
        mPopupWindow = new PopupWindow(context);
        mBgLeftRightShadowWidth = dimension(R.dimen.smartisan_rom_popup_bg_left_right_shadow_width);
        mBgTopBottomShadowHeight = dimension(R.dimen.smartisan_rom_popup_bg_top_bottom_shadow_height);
        menuPanelRoundCornerRadius = dimension(
                R.dimen.smartisan_rom_menu_panel_bg_round_corner_radius);
        arrowWidth = dimension(R.dimen.smartisan_rom_menu_panel_bg_arrow_width);
        arrowHeight = dimension(R.dimen.smartisan_rom_menu_panel_bg_arrow_height);
        minDistance = dimension(R.dimen.smartisan_rom_menu_panel_bg_min_distance);
    }

    protected abstract void prepareShow();

    public void show(int direction, int xOffset, int yOffset,
            int arrowXOffset, int arrowYOffset) {
        show(direction, xOffset, yOffset, arrowXOffset, arrowYOffset, false);
    }

    public void show(int direction, int xOffset, int yOffset,
            int arrowXOffset, int arrowYOffset, boolean arrowXOffsetIncludesArrowWidth) {
        validateDirection(direction);
        prepareShow();
        if (mAnchorView == null || mMenuPanelView == null) {
            throw new IllegalStateException("Anchor and menu content are required");
        }

        mPopupWindow.setWidth(getPopupWindowWidth());
        mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // R2 always enabled PopupWindow's own clipping. mClipToScreen controls only the ROM's
        // extra position correction below; disabling it must not disable platform safety.
        if (Build.VERSION.SDK_INT >= 29) mPopupWindow.setIsClippedToScreen(true);
        mPopupWindow.setClippingEnabled(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(requestFocusable);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.setOnDismissListener(dismissListener);

        int[] anchorWindow = new int[2];
        int[] rootScreen = new int[2];
        mAnchorView.getLocationInWindow(anchorWindow);
        View rootView = mAnchorView.getRootView();
        rootView.getLocationOnScreen(rootScreen);
        Rect visible = new Rect();
        mAnchorView.getWindowVisibleDisplayFrame(visible);

        View content = createContentViewWithArrow(
                direction, arrowXOffset, arrowYOffset);
        setArrowVisible(content, arrowVisible);
        setArrowLocationAfterLayout(content, arrowXOffsetIncludesArrowWidth);
        mPopupWindow.setContentView(content);

        int maxHeight = Math.max(dp(96), visible.height() - minDistance * 2);
        content.measure(
                View.MeasureSpec.makeMeasureSpec(getPopupWindowWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST));
        mMenuPanelView.measure(View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.makeMeasureSpec(visible.height(), View.MeasureSpec.AT_MOST));
        int menuPanelHeight = mMenuPanelView.getMeasuredHeight();
        int anchorLeft = anchorWindow[0];
        int anchorTop = anchorWindow[1];
        // Keep these as the raw visible-frame edges. The R2 formulas below already add the
        // minimum edge distance once; baking it into the frame as well would double the margin.
        int visibleLeft = visible.left - rootScreen[0];
        int visibleTop = visible.top - rootScreen[1];
        int visibleRight = visible.right - rootScreen[0];
        int visibleBottom = visible.bottom - rootScreen[1];

        int x = anchorLeft + xOffset;
        int y = anchorTop + yOffset;
        // menuPopup is the only R2 flag that changes coordinates. ARROW_BOTTOM merely selects the
        // bottom-arrow layout and showAboveAnchor merely selects the matching animation.
        if (menuPopup) {
            y -= menuPanelHeight + mAnchorView.getMeasuredHeight();
            if (arrowVisible) y -= arrowHeight;
        }

        // R2 always enables PopupWindow's platform clipping. Its mClipToScreen flag has the
        // counter-intuitive legacy meaning "skip the ROM's additional coordinate correction";
        // grouped long-press menus set it to false to opt into the correction below.
        if (!clipToScreen) {
            int overflowX = x + mBgLeftRightShadowWidth + mContentAreaWidth
                    + minDistance - visibleRight;
            int overflowY = y + mBgTopBottomShadowHeight + menuPanelHeight
                    + minDistance - visibleBottom;
            if (autoAdjustPopupDirection) {
                if (overflowX > 0) {
                    x = Math.max(x - mContentAreaWidth,
                            visibleLeft - (mBgLeftRightShadowWidth - minDistance));
                }
                int topSpace = y - menuPanelHeight + mBgTopBottomShadowHeight - minDistance;
                if (topSpace < visibleTop) {
                    if (overflowY > 0) y -= overflowY;
                } else {
                    y = minDistance + topSpace - mBgTopBottomShadowHeight;
                }
            } else {
                if (overflowX > 0) x -= overflowX;
                if (overflowY > 0) y -= overflowY;
            }
        }

        mPopupWindow.setAnimationStyle(animationStyle());
        mPopupWindow.showAtLocation(rootView, Gravity.TOP | Gravity.LEFT, x, y);
    }

    protected View createContentViewWithArrow(int direction,
            int arrowXOffset, int arrowYOffset) {
        int layout;
        if (direction == ARROW_TOP) {
            layout = R.layout.smartisan_rom_popup_menu_layout_for_top;
        } else if (direction == ARROW_BOTTOM) {
            layout = R.layout.smartisan_rom_popup_menu_layout_for_bottom;
            // The original bottom-arrow branch also selects the above-anchor animation. It does
            // not move the popup; callers continue to own the y offset.
            setShowAboveAnchor(true);
        } else if (direction == ARROW_LEFT) {
            layout = R.layout.smartisan_rom_popup_menu_layout_for_left;
        } else if (direction == ARROW_RIGHT) {
            layout = R.layout.smartisan_rom_popup_menu_layout_for_right;
        } else {
            throw new IllegalArgumentException("unknown direction value: " + direction);
        }

        View root = LayoutInflater.from(mContext).inflate(layout, null);
        FrameLayout holder = root.findViewById(R.id.smartisan_rom_place_holder);
        ImageView arrow = root.findViewById(R.id.smartisan_rom_arrow);
        onArrowViewInflated(arrow);
        if (direction == ARROW_TOP || direction == ARROW_BOTTOM) {
            int maximum = mContentAreaWidth - menuPanelRoundCornerRadius - arrowWidth;
            int arrowX = Math.min(arrowXOffset + menuPanelRoundCornerRadius, maximum);
            arrow.setX(mBgLeftRightShadowWidth + arrowX);
        } else {
            arrow.setY(mBgTopBottomShadowHeight
                    + arrowYOffset + menuPanelRoundCornerRadius);
        }
        if (mMenuPanelView.getParent() instanceof ViewGroup) {
            ((ViewGroup) mMenuPanelView.getParent()).removeView(mMenuPanelView);
        }
        // R2's private ViewPager reports its adapter width during the WRAP_CONTENT measure pass.
        // The public-SDK pager cannot break the FrameLayout WRAP_CONTENT/MATCH_PARENT cycle that
        // way, so bind the panel to the same explicit content width already used by the original
        // PopupWindow geometry. Without this equivalent, a grid collapses to its tiny icon
        // intrinsic widths while the arrow and outer window still use the correct 336dp width.
        holder.addView(mMenuPanelView, new FrameLayout.LayoutParams(
                mContentAreaWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        holder.setBackgroundResource(R.drawable.smartisan_rom_popup_menu_bg_shadow);
        mMenuPanelView.setOnClickListener(view -> { });
        holder.setOnClickListener(view -> dismiss());
        return root;
    }

    protected void onArrowViewInflated(ImageView arrowView) {}

    private void setArrowVisible(View content, boolean visible) {
        View arrow = content.findViewById(R.id.smartisan_rom_arrow);
        if (arrow != null) arrow.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setArrowLocationAfterLayout(View content, boolean includeArrowWidth) {
        ImageView arrow = content.findViewById(R.id.smartisan_rom_arrow);
        if (arrow == null || !includeArrowWidth) return;
        arrow.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                arrow.setX(arrow.getX() - arrow.getWidth() / 2f);
                arrow.removeOnLayoutChangeListener(this);
            }
        });
    }

    private int animationStyle() {
        if (showAboveAnchor) {
            return R.style.Animation_SmartisanUi_Popup_Above;
        }
        return R.style.Animation_SmartisanUi_Popup_Below;
    }

    private static void validateDirection(int direction) {
        if (direction < ARROW_TOP || direction > ARROW_RIGHT) {
            throw new IllegalArgumentException("unknown direction value: " + direction);
        }
    }

    private int dimension(int resource) {
        return mContext.getResources().getDimensionPixelSize(resource);
    }

    protected int dp(float value) {
        return Math.round(value * mContext.getResources().getDisplayMetrics().density);
    }

    public int getPopupWindowWidth() {
        return mContentAreaWidth + mBgLeftRightShadowWidth * 2;
    }

    public int getLeftRightShadowWidth() {
        return mBgLeftRightShadowWidth;
    }

    public int getMenuPanelBgRoundCornerRadius() {
        return menuPanelRoundCornerRadius;
    }

    public void showCenter() {
        showCenter(0, 0);
    }

    public void showCenter(int yOffset, int arrowYOffset) {
        if (mAnchorView == null) throw new IllegalStateException("Anchor is required");
        int xOffset = mAnchorView.getWidth() / 2 - getPopupWindowWidth() / 2;
        int arrowXOffset = getPopupWindowWidth() / 2 - mBgLeftRightShadowWidth
                - menuPanelRoundCornerRadius;
        show(ARROW_TOP, xOffset, yOffset, arrowXOffset, arrowYOffset, true);
    }

    public void setShowAboveAnchor(boolean above) {
        showAboveAnchor = above;
    }

    public boolean isShowing() {
        return mPopupWindow.isShowing();
    }

    public void dismiss() {
        mPopupWindow.dismiss();
        mPopupWindow.setContentView(null);
    }

    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        dismissListener = listener;
    }

    public void setAnchorView(View anchor) {
        mAnchorView = anchor;
    }

    public View getAnchorView() {
        return mAnchorView;
    }

    public void setContentAreaWidth(int width) {
        mContentAreaWidth = width;
    }

    public void setFocusable(boolean focusable) {
        requestFocusable = focusable;
    }

    public void setArrowInvisible() {
        arrowVisible = false;
    }

    public void setArrowVisible(boolean visible) {
        arrowVisible = visible;
    }

    public PopupWindow getPopupWindow() {
        return mPopupWindow;
    }

    public void setClipToScreenEnabled(boolean enabled) {
        clipToScreen = enabled;
    }

    public void setAutoAdjustPopupDirection(boolean enabled) {
        autoAdjustPopupDirection = enabled;
    }

    public void setMenuPopup(boolean popup) {
        menuPopup = popup;
    }
}
