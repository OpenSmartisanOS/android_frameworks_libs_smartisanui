package org.opensmartisanos.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

/** Circular long-press progress popup with the original 100/300ms scale transitions. */
public class SmartisanCircleProgressPopup extends PopupWindow {
    private static final int DEFAULT_CIRCLE_ANIM_DURATION = 1000;

    public interface CircleProgressListener {
        void cancel();
        void complete();
    }

    public abstract static class CircleProgressListenerAdapter implements CircleProgressListener {
        @Override public void complete() { }
        @Override public void cancel() { }
    }

    private final SmartisanCircleProgressView progressView;
    private final AnimatorSet startAnimator = new AnimatorSet();
    private final AnimatorSet endAnimator = new AnimatorSet();
    private final ValueAnimator circleAnimator = ValueAnimator.ofFloat(0f, 360f);
    private int circleAnimatorDuration = DEFAULT_CIRCLE_ANIM_DURATION;
    private CircleProgressListener listener;

    public SmartisanCircleProgressPopup(Context context) {
        progressView = new SmartisanCircleProgressView(context);
        setContentView(progressView);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(false);
        setTouchable(false);
        setWindowLayoutType(WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL);
        setClippingEnabled(false);
        setAttachedInDecor(false);
        initAnimators();
    }

    public void show(View anchor, int x, int y, boolean withAnimation) {
        if (isShowing()) dismissImmediate();
        int[] position = new int[2];
        anchor.getLocationOnScreen(position);
        int popupX = position[0] + x - progressView.getCircleWidth() / 2;
        int popupY = position[1] + y - progressView.getCircleHeight() / 2;
        progressView.reset();
        showAtLocation(anchor, Gravity.NO_GRAVITY, popupX, popupY);
        playStartAnimation(withAnimation);
    }

    @Override public void dismiss() {
        if (!endAnimator.isRunning() && isShowing()) {
            cancelCircleAnimation();
            resetAnimators();
            playEndAnimation();
        }
    }

    public void setCircleProgressListener(CircleProgressListener listener) {
        this.listener = listener;
    }

    public void setCircleAnimDuration(int duration) { circleAnimatorDuration = duration; }

    private void dismissImmediate() {
        cancelCircleAnimation();
        resetAnimators();
        super.dismiss();
    }

    private void initAnimators() {
        startAnimator.playTogether(
                ObjectAnimator.ofFloat(progressView, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(progressView, View.SCALE_X, 0.5f, 1f),
                ObjectAnimator.ofFloat(progressView, View.SCALE_Y, 0.5f, 1f));
        startAnimator.setDuration(100L);
        endAnimator.playTogether(
                ObjectAnimator.ofFloat(progressView, View.ALPHA, 1f, 0f),
                ObjectAnimator.ofFloat(progressView, View.SCALE_X, 1f, 0.5f),
                ObjectAnimator.ofFloat(progressView, View.SCALE_Y, 1f, 0.5f));
        endAnimator.setDuration(300L);
        circleAnimator.setDuration(circleAnimatorDuration);
        circleAnimator.addUpdateListener(animation ->
                progressView.setSweepAngle((Float) animation.getAnimatedValue()));
    }

    private void cancelCircleAnimation() {
        if (startAnimator.isRunning()) {
            if (listener != null) listener.cancel();
        } else if (circleAnimator.isRunning()) {
            circleAnimator.cancel();
        }
    }

    private void resetAnimators() {
        circleAnimator.removeAllListeners();
        startAnimator.removeAllListeners();
        endAnimator.removeAllListeners();
        if (circleAnimator.isRunning()) circleAnimator.cancel();
        if (startAnimator.isRunning()) startAnimator.end();
        if (endAnimator.isRunning()) endAnimator.end();
    }

    private void playStartAnimation(boolean nextAnimation) {
        startAnimator.start();
        startAnimator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (nextAnimation) playCircleAnimation();
            }
        });
    }

    private void playEndAnimation() {
        endAnimator.start();
        endAnimator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                if (isShowing()) {
                    resetAnimators();
                    SmartisanCircleProgressPopup.super.dismiss();
                }
            }
        });
    }

    private void playCircleAnimation() {
        circleAnimator.setDuration(circleAnimatorDuration);
        if (listener != null) {
            circleAnimator.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animation) {
                    if ((int) Math.floor(progressView.getSweepAngle()) == 360) listener.complete();
                }
                @Override public void onAnimationCancel(Animator animation) { listener.cancel(); }
            });
        }
        circleAnimator.start();
    }
}
