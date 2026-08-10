package org.opensmartisanos.ui.app;

import android.view.MotionEvent;
import android.view.View;

import org.opensmartisanos.ui.internal.dynamicanimation.DynamicAnimation;
import org.opensmartisanos.ui.internal.dynamicanimation.FloatPropertyCompat;
import org.opensmartisanos.ui.internal.dynamicanimation.SpringAnimation;
import org.opensmartisanos.ui.internal.dynamicanimation.SpringForce;

/** Public-SDK counterpart of the Smartisan alert button scale touch helper. */
final class SmartisanAlertButtonScaleHelper implements View.OnTouchListener {
    private static final float DOWN_SCALE = 0.99f;
    private static final float UP_SCALE = 1.0f;
    private static final float DOWN_ALPHA = 0.9f;
    private static final float UP_ALPHA = 1.0f;
    private static final float SCALE_DAMPING_RATIO = 0.7f;
    private static final float SCALE_STIFFNESS = 500.0f;
    private static final float ALPHA_DAMPING_RATIO = 0.6f;
    private static final float ALPHA_STIFFNESS = 400.0f;
    private static final float MINIMUM_VISIBLE_CHANGE = 0.00390625f;

    private static final FloatPropertyCompat<View> SCALE =
            new FloatPropertyCompat<View>("scale") {
                @Override
                public float getValue(View view) {
                    return view.getScaleX();
                }

                @Override
                public void setValue(View view, float value) {
                    view.setScaleX(value);
                    view.setScaleY(value);
                }
            };

    private final View owner;
    private final SpringAnimation scaleAnimation;
    private final SpringAnimation alphaAnimation;

    SmartisanAlertButtonScaleHelper(View owner) {
        this.owner = owner;
        // The original helper deliberately animates the dialog root while the button owns touch.
        View animationTarget = owner.getRootView();
        scaleAnimation = new SpringAnimation(animationTarget, SCALE)
                .setMinimumVisibleChange(MINIMUM_VISIBLE_CHANGE)
                .setSpring(new SpringForce()
                        .setDampingRatio(SCALE_DAMPING_RATIO)
                        .setStiffness(SCALE_STIFFNESS));
        alphaAnimation = new SpringAnimation(animationTarget, DynamicAnimation.ALPHA)
                .setMinimumVisibleChange(MINIMUM_VISIBLE_CHANGE)
                .setSpring(new SpringForce()
                        .setDampingRatio(ALPHA_DAMPING_RATIO)
                        .setStiffness(ALPHA_STIFFNESS));
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (!owner.isClickable() || !owner.isAttachedToWindow()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                animate(true);
                break;
            case MotionEvent.ACTION_MOVE:
                animate(owner.isPressed());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                animate(false);
                break;
            default:
                break;
        }
        return false;
    }

    private void animate(boolean pressed) {
        float scale = pressed ? DOWN_SCALE : UP_SCALE;
        float alpha = pressed ? DOWN_ALPHA : UP_ALPHA;
        SpringForce scaleSpring = scaleAnimation.getSpring();
        SpringForce alphaSpring = alphaAnimation.getSpring();
        if (Float.compare(scale, scaleSpring.getFinalPosition()) == 0
                && Float.compare(alpha, alphaSpring.getFinalPosition()) == 0) {
            return;
        }
        scaleSpring.setDampingRatio(SCALE_DAMPING_RATIO)
                .setStiffness(SCALE_STIFFNESS)
                .setFinalPosition(scale);
        scaleAnimation.start();
        alphaSpring.setDampingRatio(ALPHA_DAMPING_RATIO)
                .setStiffness(ALPHA_STIFFNESS)
                .setFinalPosition(alpha);
        alphaAnimation.start();
    }
}
