package com.rock.rockui.anim;

import android.app.Activity;
import android.view.View;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

public class AnimateView {

    public static void fadeINInflate(Activity activity, View view, int duration) {
        view.postDelayed(() -> fadeIn(view, duration), 100L);
    }

    public static void focusZIn(View view, int duration) {
        view.animate().translationZ(0.0F).setDuration(duration);
    }

    public static void focusZOut(View view, int duration) {
        view.animate().translationZ(10.0F).setDuration(duration);
    }

    private static void fadeIn(View view, int duration) {
        float originalAlpha = view.getAlpha();
        view.setAlpha(0.0F);
        view.animate().alpha(originalAlpha).setDuration(duration);
        view.setTranslationZ(50.0F);
        view.animate().translationZ(0.0F).setDuration(duration * 2);
    }

    public static void popInflate(View view, int duration) {
        view.postDelayed(() -> pop(view, duration), 100L);
    }

    private static void pop(View view, int duration) {
        float originalScaleX = view.getScaleX();
        float originalScaleY = view.getScaleY();
        view.setScaleX(0.5F);
        view.setScaleY(0.5F);
        view.animate()
                .scaleX(originalScaleX)
                .scaleY(originalScaleY)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setDuration(duration);
    }

    public static void slideUPInflate(Activity activity, View view, int duration) {
        view.postDelayed(() -> slideUp(view, duration), 100L);
    }

    private static void slideUp(View view, int duration) {
        float originalTranslationY = view.getTranslationY();
        view.setTranslationY(view.getHeight() + originalTranslationY);
        view.animate().translationY(originalTranslationY).setDuration(duration);
        view.setTranslationZ(50.0F);
        view.animate().translationZ(2.0F).setDuration(duration * 2);
    }
}
