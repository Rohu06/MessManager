package com.example.messmanager.util;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

/**
 * AnimationUtils
 *
 * Reusable micro-animation helpers used across the app for polished
 * UI transitions: number count-ups, view pulses, and press-scale effects.
 */
public final class AnimationUtils {

    private AnimationUtils() { /* no instances */ }

    /**
     * Animates a TextView's text from one integer value to another,
     * producing a smooth count-up (or count-down) effect.
     *
     * @param tv         the TextView to animate
     * @param from       starting integer value
     * @param to         ending integer value
     * @param durationMs animation duration in milliseconds
     */
    public static ValueAnimator animateCountUp(TextView tv, int from, int to, long durationMs) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(durationMs);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(anim ->
                tv.setText(String.valueOf((int) anim.getAnimatedValue())));
        animator.start();
        return animator;
    }

    /**
     * Plays a quick scale pulse on a view: 1.0 → 1.15 → 1.0.
     * Useful for drawing attention after a value change.
     *
     * @param view the view to pulse
     */
    public static void pulseView(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.15f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(350);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    /**
     * Plays a tactile press-scale animation on a button:
     * shrink to 0.92 over 80 ms, then spring back to 1.0 over 200 ms
     * with an overshoot interpolator. Designed to pair with the
     * MaterialButton's built-in ripple for a satisfying click feel.
     *
     * @param button the view (typically a MaterialButton) to animate
     */
    public static void pressScaleAnimation(View button) {
        // Shrink phase
        ObjectAnimator shrinkX = ObjectAnimator.ofFloat(button, View.SCALE_X, 1f, 0.92f);
        ObjectAnimator shrinkY = ObjectAnimator.ofFloat(button, View.SCALE_Y, 1f, 0.92f);
        AnimatorSet shrink = new AnimatorSet();
        shrink.playTogether(shrinkX, shrinkY);
        shrink.setDuration(80);

        // Spring-back phase
        ObjectAnimator expandX = ObjectAnimator.ofFloat(button, View.SCALE_X, 0.92f, 1f);
        ObjectAnimator expandY = ObjectAnimator.ofFloat(button, View.SCALE_Y, 0.92f, 1f);
        AnimatorSet expand = new AnimatorSet();
        expand.playTogether(expandX, expandY);
        expand.setDuration(200);
        expand.setInterpolator(new OvershootInterpolator(2.5f));

        AnimatorSet full = new AnimatorSet();
        full.playSequentially(shrink, expand);
        full.start();
    }
}
