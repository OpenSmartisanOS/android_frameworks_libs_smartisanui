package org.opensmartisanos.ui.internal;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;

/** Public-SDK replacement for the ROM's hidden ExploreByTouchHelper dependency. */
public final class SmartisanVirtualIconAccessibility extends AccessibilityNodeProvider {
    public interface Callback {
        boolean isVisible();
        Rect getBounds();
        CharSequence getDescription();
        boolean performClick();
    }

    private static final int HOST_ID = View.NO_ID;
    private final View host;
    private final int virtualId;
    private final Callback callback;
    private int hoveredId = HOST_ID;
    private int focusedId = HOST_ID;

    public SmartisanVirtualIconAccessibility(View host, int virtualId, Callback callback) {
        this.host = host;
        this.virtualId = virtualId;
        this.callback = callback;
        host.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    @Override
    public AccessibilityNodeInfo createAccessibilityNodeInfo(int id) {
        if (id == HOST_ID) {
            AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(host);
            host.onInitializeAccessibilityNodeInfo(node);
            if (callback.isVisible()) node.addChild(host, virtualId);
            return node;
        }
        if (id != virtualId || !callback.isVisible()) return null;
        AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
        node.setSource(host, virtualId);
        node.setParent(host);
        node.setPackageName(host.getContext().getPackageName());
        node.setClassName(android.widget.ImageButton.class.getName());
        node.setContentDescription(callback.getDescription());
        node.setClickable(true);
        node.setEnabled(host.isEnabled());
        node.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);
        if (focusedId == virtualId) {
            node.setAccessibilityFocused(true);
            node.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
        } else {
            node.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_ACCESSIBILITY_FOCUS);
        }
        Rect parentBounds = new Rect(callback.getBounds());
        parentBounds.offset(-host.getScrollX(), -host.getScrollY());
        node.setBoundsInParent(parentBounds);
        int[] location = new int[2];
        host.getLocationOnScreen(location);
        Rect screenBounds = new Rect(parentBounds);
        screenBounds.offset(location[0], location[1]);
        node.setBoundsInScreen(screenBounds);
        return node;
    }

    @Override
    public boolean performAction(int id, int action, Bundle arguments) {
        if (id != virtualId) return host.performAccessibilityAction(action, arguments);
        if (action == AccessibilityNodeInfo.ACTION_CLICK) return callback.performClick();
        if (action == AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS && focusedId != virtualId) {
            focusedId = virtualId;
            sendEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
            host.invalidate();
            return true;
        }
        if (action == AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS && focusedId == virtualId) {
            focusedId = HOST_ID;
            sendEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED);
            host.invalidate();
            return true;
        }
        return false;
    }

    public boolean dispatchHoverEvent(MotionEvent event) {
        AccessibilityManager manager = host.getContext().getSystemService(AccessibilityManager.class);
        if (manager == null || !manager.isEnabled() || !manager.isTouchExplorationEnabled()) return false;
        int action = event.getActionMasked();
        int target = action == MotionEvent.ACTION_HOVER_EXIT ? HOST_ID : hitTest(event.getX(), event.getY());
        if (hoveredId != target) {
            int previous = hoveredId;
            hoveredId = target;
            if (target != HOST_ID) sendEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
            if (previous != HOST_ID) sendEvent(AccessibilityEvent.TYPE_VIEW_HOVER_EXIT);
        }
        return target != HOST_ID || action == MotionEvent.ACTION_HOVER_EXIT;
    }

    public void invalidateRoot() {
        host.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
        host.invalidate();
    }

    public void sendClickEvent() {
        sendEvent(AccessibilityEvent.TYPE_VIEW_CLICKED);
    }

    private int hitTest(float x, float y) {
        Rect bounds = callback.getBounds();
        return callback.isVisible() && bounds.contains((int) x + host.getScrollX(),
                (int) y + host.getScrollY()) ? virtualId : HOST_ID;
    }

    private void sendEvent(int type) {
        AccessibilityManager manager = host.getContext().getSystemService(AccessibilityManager.class);
        if (manager == null || !manager.isEnabled()) return;
        AccessibilityEvent event = AccessibilityEvent.obtain(type);
        event.setPackageName(host.getContext().getPackageName());
        event.setClassName(android.widget.ImageButton.class.getName());
        event.setContentDescription(callback.getDescription());
        event.setSource(host, virtualId);
        ViewParent parent = host.getParent();
        if (parent != null) parent.requestSendAccessibilityEvent(host, event);
    }
}
