/* Ported from smartisanos.widget.ShadowComponent in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.internal;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public final class SmartisanShadowComponent {
  public static final int ORIENTATION_ABOVE_HOST = 1;
  public static final int ORIENTATION_BELOW_HOST = 2;

  private final Context context;
  private final View hostView;
  private final int orientation;
  private final ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;
  private int shadowDrawable;
  private ImageView shadowView;
  private boolean shadowVisible;

  public SmartisanShadowComponent(
      View hostView, int shadowDrawable, boolean shadowVisible, int orientation) {
    this.context = hostView.getContext();
    this.hostView = hostView;
    this.shadowDrawable = shadowDrawable;
    this.shadowVisible = shadowVisible;
    this.orientation = orientation;
    globalLayoutListener =
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            invalidateShadow();
            ViewTreeObserver observer = hostView.getViewTreeObserver();
            if (observer.isAlive()) observer.removeOnGlobalLayoutListener(this);
          }
        };
    hostView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    hostView.addOnLayoutChangeListener(
        (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
          if (shadowView != null) invalidateShadow();
        });
    hostView.addOnAttachStateChangeListener(
        new View.OnAttachStateChangeListener() {
          @Override
          public void onViewAttachedToWindow(View view) {
            view.post(SmartisanShadowComponent.this::invalidateShadow);
          }

          @Override
          public void onViewDetachedFromWindow(View view) {
            removeShadow();
          }
        });
  }

  public void invalidateShadow() {
    ViewParent parent = hostView.getParent();
    if (parent == null) return;
    if (!shadowVisible) {
      removeShadow();
      return;
    }
    if (!(parent instanceof RelativeLayout) && !(parent instanceof FrameLayout)) {
      throw new IllegalParentException(
          "parent must be RelativeLayout or FrameLayout if you want to show shadow");
    }

    ViewGroup parentGroup = (ViewGroup) parent;
    if (shadowView == null) shadowView = new ImageView(context);
    shadowView.setBackgroundResource(shadowDrawable);
    ViewParent shadowParent = shadowView.getParent();
    if (shadowParent != null && shadowParent != parentGroup) {
      ((ViewGroup) shadowParent).removeView(shadowView);
      shadowParent = null;
    }
    if (shadowParent == null) parentGroup.addView(shadowView, createLayoutParams(parentGroup));
    else shadowView.setLayoutParams(createLayoutParams(parentGroup));
  }

  private ViewGroup.LayoutParams createLayoutParams(ViewGroup parent) {
    if (parent instanceof FrameLayout) {
      FrameLayout frameLayout = (FrameLayout) parent;
      FrameLayout.LayoutParams params =
          new FrameLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      if (orientation == ORIENTATION_ABOVE_HOST) {
        params.bottomMargin = frameLayout.getHeight() - hostView.getTop();
        params.gravity = Gravity.BOTTOM;
      } else {
        params.topMargin = hostView.getBottom();
      }
      return params;
    }

    RelativeLayout relativeLayout = (RelativeLayout) parent;
    RelativeLayout.LayoutParams params =
        new RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    if (orientation == ORIENTATION_ABOVE_HOST) {
      if (hostView.getId() != View.NO_ID)
        params.addRule(RelativeLayout.ABOVE, hostView.getId());
      else {
        params.bottomMargin = relativeLayout.getHeight() - hostView.getTop();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
      }
    } else if (hostView.getId() != View.NO_ID) {
      params.addRule(RelativeLayout.BELOW, hostView.getId());
    } else {
      params.topMargin = hostView.getBottom();
    }
    return params;
  }

  public void setShadowDrawable(int resId) {
    if (resId == shadowDrawable) return;
    shadowDrawable = resId;
    if (shadowVisible) invalidateShadow();
  }

  public void setShadowVisible(boolean visible) {
    if (visible == shadowVisible) return;
    shadowVisible = visible;
    invalidateShadow();
  }

  public ImageView getShadowView() {
    return shadowView;
  }

  private void removeShadow() {
    if (shadowView == null) return;
    ViewParent parent = shadowView.getParent();
    if (parent instanceof ViewGroup) ((ViewGroup) parent).removeView(shadowView);
    shadowView = null;
  }

  public static final class IllegalParentException extends RuntimeException {
    public IllegalParentException(String message) {
      super(message);
    }
  }
}
