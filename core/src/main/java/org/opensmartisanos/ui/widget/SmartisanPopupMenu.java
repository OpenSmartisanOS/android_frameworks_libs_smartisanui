/* Ported from smartisanos.widget.support.SmartisanPopupMenu in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import org.opensmartisanos.ui.R;

public abstract class SmartisanPopupMenu {
    public static final int ARROW_TOP = 0;
    public static final int ARROW_BOTTOM = 1;
    public static final int ARROW_LEFT = 2;
    public static final int ARROW_RIGHT = 3;
    protected final Context context;
    protected final PopupWindow popupWindow;
    protected View anchorView;
    protected View menuPanelView;
    private boolean showAboveAnchor;
    private boolean autoAdjustDirection = true;
    private boolean arrowVisible = true;
    private boolean requestFocusable = true;
    private int contentAreaWidth;

    public SmartisanPopupMenu(Context context) {
        this.context = context;
        popupWindow = new PopupWindow(context);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
    }

    protected abstract void prepareShow();

    public void show(int direction, int xOffset, int yOffset, int arrowXOffset, int arrowYOffset) {
        prepareShow();
        if (anchorView == null || menuPanelView == null) throw new IllegalStateException("Anchor and menu content are required");
        int actualDirection = autoAdjustDirection ? adjustDirection(direction) : direction;
        View content = createContentViewWithArrow(actualDirection, arrowXOffset, arrowYOffset);
        popupWindow.setContentView(content);
        popupWindow.setWidth(contentAreaWidth > 0 ? contentAreaWidth : ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(requestFocusable);
        if (Build.VERSION.SDK_INT >= 29) popupWindow.setIsClippedToScreen(true);
        int[] location = new int[2]; anchorView.getLocationInWindow(location);
        content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int width = content.getMeasuredWidth(); int height = content.getMeasuredHeight();
        int x = location[0] + xOffset; int y = location[1] + yOffset;
        switch (actualDirection) {
            case ARROW_BOTTOM: x += (anchorView.getWidth() - width) / 2; y -= height; break;
            case ARROW_LEFT: x += anchorView.getWidth(); y += (anchorView.getHeight() - height) / 2; break;
            case ARROW_RIGHT: x -= width; y += (anchorView.getHeight() - height) / 2; break;
            case ARROW_TOP:
            default: x += (anchorView.getWidth() - width) / 2; y += anchorView.getHeight(); break;
        }
        popupWindow.showAtLocation(anchorView, Gravity.TOP | Gravity.START, x, Math.max(0, y));
    }

    private int adjustDirection(int requested) {
        if (showAboveAnchor) return ARROW_BOTTOM;
        int[] location = new int[2]; anchorView.getLocationOnScreen(location);
        View root = anchorView.getRootView();
        int bottomInset = 0;
        if (Build.VERSION.SDK_INT >= 23 && root.getRootWindowInsets() != null) {
            WindowInsets insets = root.getRootWindowInsets();
            bottomInset = Build.VERSION.SDK_INT >= 30 ? insets.getInsets(WindowInsets.Type.navigationBars()).bottom
                    : insets.getStableInsetBottom();
        }
        int below = root.getHeight() - bottomInset - location[1] - anchorView.getHeight();
        return requested == ARROW_TOP && below < dp(180) ? ARROW_BOTTOM : requested;
    }

    protected View createContentViewWithArrow(int direction, int arrowXOffset, int arrowYOffset) {
        if (menuPanelView.getParent() instanceof ViewGroup) {
            ((ViewGroup) menuPanelView.getParent()).removeView(menuPanelView);
        }
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(direction == ARROW_LEFT || direction == ARROW_RIGHT ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        ImageView arrow = new ImageView(context);
        int arrowResource = direction == ARROW_BOTTOM ? R.drawable.smartisan_rom_pop_up_menu_arrow_bottom
                : direction == ARROW_LEFT ? R.drawable.smartisan_rom_pop_up_menu_arrow_left
                : direction == ARROW_RIGHT ? R.drawable.smartisan_rom_pop_up_menu_arrow_right
                : R.drawable.smartisan_rom_pop_up_menu_arrow_top;
        arrow.setImageResource(arrowResource); arrow.setVisibility(arrowVisible ? View.VISIBLE : View.INVISIBLE);
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (direction == ARROW_TOP || direction == ARROW_BOTTOM) arrowParams.leftMargin = Math.max(0, arrowXOffset);
        else arrowParams.topMargin = Math.max(0, arrowYOffset);
        if (direction == ARROW_BOTTOM || direction == ARROW_RIGHT) root.addView(menuPanelView);
        root.addView(arrow, arrowParams);
        if (direction == ARROW_TOP || direction == ARROW_LEFT) root.addView(menuPanelView);
        return root;
    }

    protected int dp(float value) { return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f); }
    public void showCenter() { showCenter(0, 0); }
    public void showCenter(int yOffset, int arrowYOffset) { show(ARROW_TOP, 0, yOffset, 0, arrowYOffset); }
    public void setShowAboveAnchor(boolean above) { showAboveAnchor = above; }
    public boolean isShowing() { return popupWindow.isShowing(); }
    public void dismiss() { popupWindow.dismiss(); popupWindow.setContentView(null); }
    public void setOnDismissListener(PopupWindow.OnDismissListener listener) { popupWindow.setOnDismissListener(listener); }
    public void setAnchorView(View anchor) { anchorView = anchor; }
    public View getAnchorView() { return anchorView; }
    public void setContentAreaWidth(int width) { contentAreaWidth = width; }
    public int getPopupWindowWidth() { return contentAreaWidth > 0 ? contentAreaWidth : dp(240); }
    public void setFocusable(boolean focusable) { requestFocusable = focusable; }
    public void setArrowInvisible() { arrowVisible = false; }
    public PopupWindow getPopupWindow() { return popupWindow; }
    public void setClipToScreenEnabled(boolean enabled) { if (Build.VERSION.SDK_INT >= 29) popupWindow.setIsClippedToScreen(enabled); else popupWindow.setClippingEnabled(enabled); }
    public void setAutoAdjustPopupDirection(boolean enabled) { autoAdjustDirection = enabled; }
}
