/* Ported from smartisanos.widget.ShadowDrawable in Smartisan OS 8.5.3. */
package org.opensmartisanos.ui.internal;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.view.View;

public final class SmartisanShadowDrawable extends RippleDrawable {
    private final int insetHorizontal;
    private final int insetVertical;
    private final Drawable shadow;
    private final Drawable target;
    private boolean projectBackwards = true;

    public SmartisanShadowDrawable(Drawable shadow, Drawable target, int insetHorizontal,
            int insetVertical) {
        super(ColorStateList.valueOf(0), null, null);
        this.shadow = shadow;
        this.target = target;
        this.insetHorizontal = insetHorizontal;
        this.insetVertical = insetVertical;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        target.setBounds(bounds);
        Rect shadowBounds = new Rect(bounds);
        shadowBounds.inset(-insetHorizontal, -insetVertical);
        shadow.setBounds(shadowBounds);
    }

    @Override
    public boolean getPadding(Rect padding) {
        return target.getPadding(padding);
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        boolean changed = target.isStateful() && target.setState(stateSet);
        return shadow.isStateful() ? changed | shadow.setState(stateSet) : changed;
    }

    @Override
    public void setAlpha(int alpha) {
        super.setAlpha(alpha);
        target.setAlpha(alpha);
        shadow.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public boolean isStateful() {
        return target.isStateful() || shadow.isStateful();
    }

    @Override
    public void draw(Canvas canvas) {
        shadow.draw(canvas);
        target.draw(canvas);
    }

    @Override
    public int getNumberOfLayers() {
        return projectBackwards ? 0 : 2;
    }

    public Drawable getBackgroundDrawable() {
        return target;
    }

    public void setProjectBackwards(boolean shouldProject) {
        projectBackwards = shouldProject;
    }

    public static SmartisanShadowDrawable setViewBackground(View view, Drawable background,
            int shadowId, boolean shouldProject) {
        Drawable shadow = view.getResources().getDrawable(shadowId);
        Rect padding = new Rect();
        shadow.getPadding(padding);
        SmartisanShadowDrawable result = new SmartisanShadowDrawable(
                shadow, background, padding.left, padding.top);
        result.setProjectBackwards(shouldProject);
        view.setBackground(result);
        return result;
    }
}
