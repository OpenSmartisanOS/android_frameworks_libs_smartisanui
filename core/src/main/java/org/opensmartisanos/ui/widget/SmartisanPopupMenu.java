/* Ported from smartisanos.widget.support.SmartisanPopupMenu in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;

import org.opensmartisanos.ui.R;

public abstract class SmartisanPopupMenu {
    public static final int ARROW_TOP = 0;
    public static final int ARROW_BOTTOM = 1;
    public static final int ARROW_LEFT = 2;
    public static final int ARROW_RIGHT = 3;
    public static final int DIRECTION_TOP = ARROW_TOP;
    public static final int DIRECTION_BOTTOM = ARROW_BOTTOM;
    public static final int DIRECTION_LEFT = ARROW_LEFT;
    public static final int DIRECTION_RIGHT = ARROW_RIGHT;

    protected final Context context;
    protected final PopupWindow popupWindow;
    protected View anchorView;
    protected View menuPanelView;
    protected int contentAreaWidth;
    protected final int bgLeftRightShadowWidth;
    protected final int bgTopBottomShadowHeight;

    private final int arrowHeight;
    private final int arrowWidth;
    private final int menuPanelRoundCornerRadius;
    private final int minDistance;
    private final int screenWidth;
    private final int screenHeight;
    private PopupWindow.OnDismissListener dismissListener;
    private boolean showAboveAnchor;
    private boolean requestFocusable = true;
    private boolean arrowVisible;
    private boolean clipToScreen = true;
    private boolean menuPopup;
    private boolean autoAdjustPopupDirection;

    public SmartisanPopupMenu(Context context) {
        this.context = context;
        popupWindow = new PopupWindow(context);
        bgLeftRightShadowWidth = dimension(R.dimen.smartisan_rom_popup_bg_left_right_shadow_width);
        bgTopBottomShadowHeight = dimension(R.dimen.smartisan_rom_popup_bg_top_bottom_shadow_height);
        menuPanelRoundCornerRadius = dimension(R.dimen.smartisan_rom_menu_panel_bg_round_corner_radius);
        arrowWidth = dimension(R.dimen.smartisan_rom_menu_panel_bg_arrow_width);
        arrowHeight = dimension(R.dimen.smartisan_rom_menu_panel_bg_arrow_height);
        minDistance = dimension(R.dimen.smartisan_rom_menu_panel_bg_min_distance);
        Point size = new Point();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) windowManager.getDefaultDisplay().getRealSize(size);
        if (size.x == 0 || size.y == 0) {
            size.x = context.getResources().getDisplayMetrics().widthPixels;
            size.y = context.getResources().getDisplayMetrics().heightPixels;
        }
        screenWidth = size.x;
        screenHeight = size.y;
    }

    protected abstract void prepareShow();

    public void show(int direction, int xOffset, int yOffset, int arrowXOffset, int arrowYOffset) {
        show(direction, xOffset, yOffset, arrowXOffset, arrowYOffset, false);
    }

    public void show(int direction, int xOffset, int yOffset, int arrowXOffset,
            int arrowYOffset, boolean arrowXOffsetIncludesArrowWidth) {
        prepareShow();
        if (anchorView == null || menuPanelView == null) {
            throw new IllegalStateException("Anchor and menu content are required");
        }
        popupWindow.setWidth(getPopupWindowWidth());
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= 29) popupWindow.setIsClippedToScreen(true);
        else popupWindow.setClippingEnabled(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(requestFocusable);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOnDismissListener(dismissListener);

        int[] location = new int[2];
        anchorView.getLocationInWindow(location);
        View content = createContentViewWithArrow(direction, arrowXOffset, arrowYOffset);
        setArrowVisible(content, arrowVisible);
        setArrowLocationAfterLayout(content, arrowXOffsetIncludesArrowWidth);
        popupWindow.setContentView(content);

        int x = location[0] + xOffset;
        int y = location[1] + yOffset;
        if (menuPopup) {
            menuPanelView.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.makeMeasureSpec(screenHeight, View.MeasureSpec.AT_MOST));
            y -= menuPanelView.getMeasuredHeight()
                    + (arrowVisible ? arrowHeight : 0) + anchorView.getMeasuredHeight();
        }
        if (!clipToScreen) {
            menuPanelView.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.makeMeasureSpec(screenHeight, View.MeasureSpec.AT_MOST));
            int overflowX = bgLeftRightShadowWidth + x + contentAreaWidth + minDistance - screenWidth;
            int overflowY = y + bgTopBottomShadowHeight
                    + menuPanelView.getMeasuredHeight() + minDistance - screenHeight;
            if (autoAdjustPopupDirection && overflowX > 0) {
                x = Math.max(x - contentAreaWidth, -(bgLeftRightShadowWidth - minDistance));
            } else if (overflowX > 0) {
                x -= overflowX;
            }
            if (overflowY > 0) y -= overflowY;
            y = Math.max(minDistance - bgTopBottomShadowHeight, y);
        }
        popupWindow.showAtLocation(anchorView, Gravity.TOP | Gravity.START, x, y);
    }

    protected View createContentViewWithArrow(int direction, int arrowXOffset, int arrowYOffset) {
        int layout;
        if (direction == ARROW_TOP) layout = R.layout.smartisan_rom_popup_menu_layout_for_top;
        else if (direction == ARROW_BOTTOM) {
            layout = R.layout.smartisan_rom_popup_menu_layout_for_bottom;
            showAboveAnchor = true;
        } else if (direction == ARROW_LEFT) layout = R.layout.smartisan_rom_popup_menu_layout_for_left;
        else if (direction == ARROW_RIGHT) layout = R.layout.smartisan_rom_popup_menu_layout_for_right;
        else throw new IllegalArgumentException("unknown direction value: " + direction);

        View root = LayoutInflater.from(context).inflate(layout, null);
        FrameLayout holder = root.findViewById(R.id.smartisan_rom_place_holder);
        ImageView arrow = root.findViewById(R.id.smartisan_rom_arrow);
        onArrowViewInflated(arrow);
        if (direction == ARROW_TOP || direction == ARROW_BOTTOM) {
            int x = Math.min(arrowXOffset + menuPanelRoundCornerRadius,
                    contentAreaWidth - menuPanelRoundCornerRadius - arrowWidth);
            arrow.setX(bgLeftRightShadowWidth + x);
        } else {
            arrow.setY(bgTopBottomShadowHeight + arrowYOffset + menuPanelRoundCornerRadius);
        }
        if (menuPanelView.getParent() instanceof ViewGroup) {
            ((ViewGroup) menuPanelView.getParent()).removeView(menuPanelView);
        }
        holder.addView(menuPanelView);
        holder.setBackgroundResource(R.drawable.smartisan_rom_popup_menu_bg_shadow);
        menuPanelView.setOnClickListener(view -> { });
        holder.setOnClickListener(view -> dismiss());
        return root;
    }

    protected void onArrowViewInflated(ImageView arrowView) { }

    private void setArrowVisible(View content, boolean visible) {
        View arrow = content.findViewById(R.id.smartisan_rom_arrow);
        if (arrow != null) arrow.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setArrowLocationAfterLayout(View content, boolean includeArrowWidth) {
        ImageView arrow = content.findViewById(R.id.smartisan_rom_arrow);
        if (arrow == null || !includeArrowWidth) return;
        arrow.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override public void onLayoutChange(View view, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                arrow.setX(arrow.getX() - arrow.getWidth() / 2f);
                arrow.removeOnLayoutChangeListener(this);
            }
        });
    }

    private int dimension(int resource) { return context.getResources().getDimensionPixelSize(resource); }
    protected int dp(float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public int getPopupWindowWidth() { return contentAreaWidth + bgLeftRightShadowWidth * 2; }
    public int getLeftRightShadowWidth() { return bgLeftRightShadowWidth; }
    public int getMenuPanelBgRoundCornerRadius() { return menuPanelRoundCornerRadius; }
    public void showCenter() { showCenter(0, 0); }
    public void showCenter(int yOffset, int arrowYOffset) {
        int xOffset = anchorView.getWidth() / 2 - getPopupWindowWidth() / 2;
        int arrowXOffset = getPopupWindowWidth() / 2 - bgLeftRightShadowWidth
                - menuPanelRoundCornerRadius;
        show(ARROW_TOP, xOffset, yOffset, arrowXOffset, arrowYOffset, true);
    }
    public void setShowAboveAnchor(boolean above) { showAboveAnchor = above; }
    public boolean isShowing() { return popupWindow.isShowing(); }
    public void dismiss() { popupWindow.dismiss(); popupWindow.setContentView(null); }
    public void setOnDismissListener(PopupWindow.OnDismissListener listener) { dismissListener = listener; }
    public void setAnchorView(View anchor) { anchorView = anchor; }
    public View getAnchorView() { return anchorView; }
    public void setContentAreaWidth(int width) { contentAreaWidth = width; }
    public void setFocusable(boolean focusable) { requestFocusable = focusable; }
    public void setArrowInvisible() { arrowVisible = false; }
    public void setArrowVisible(boolean visible) { arrowVisible = visible; }
    public PopupWindow getPopupWindow() { return popupWindow; }
    public void setClipToScreenEnabled(boolean enabled) { clipToScreen = enabled; }
    public void setAutoAdjustPopupDirection(boolean enabled) { autoAdjustPopupDirection = enabled; }
    public void setMenuPopup(boolean popup) { menuPopup = popup; }
}
