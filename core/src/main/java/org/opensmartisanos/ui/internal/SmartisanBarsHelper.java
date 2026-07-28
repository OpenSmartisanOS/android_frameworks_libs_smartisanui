/* Ported from smartisanos.widget.BarsHelper in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.internal;

import android.view.MotionEvent;
import android.view.View;

import org.opensmartisanos.ui.internal.dynamicanimation.SpringAnimation;
import org.opensmartisanos.ui.internal.dynamicanimation.SpringForce;

public final class SmartisanBarsHelper {
    private static final float PRESSED_SCALE = 1.33f;
    private static final float NORMAL_SCALE = 1.0f;
    private static final float DAMPING_RATIO = 0.55f;
    private static final float STIFFNESS = 800.0f;

    private SmartisanBarsHelper() {}

    public static void setBarIconScaleTouchListener(View target) {
        setBarIconScaleTouchListener(target, true);
    }

    public static void setBarIconScaleTouchListener(final View target, boolean enabled) {
        target.setClickable(true);
        target.setOnTouchListener(enabled ? new View.OnTouchListener() {
            private float endScale;
            private final SpringAnimation animationX =
                    new SpringAnimation(target, SpringAnimation.SCALE_X);
            private final SpringAnimation animationY =
                    new SpringAnimation(target, SpringAnimation.SCALE_Y);

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        play(view, true);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        play(view, view.isPressed());
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        play(view, false);
                        break;
                    default:
                        break;
                }
                return false;
            }

            private void play(View view, boolean pressed) {
                float scale = pressed ? PRESSED_SCALE : NORMAL_SCALE;
                if (Float.compare(endScale, scale) == 0) return;
                endScale = scale;
                SpringForce force = new SpringForce(scale)
                        .setDampingRatio(DAMPING_RATIO)
                        .setStiffness(STIFFNESS);
                animationX.setSpring(force).start();
                animationY.setSpring(force).start();
            }
        } : null);
    }
}
