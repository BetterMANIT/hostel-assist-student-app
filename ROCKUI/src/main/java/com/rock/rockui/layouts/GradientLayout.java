package com.rock.rockui.layouts;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import androidx.annotation.NonNull;

import com.rock.rockui.R;
import com.rock.rockui.drawable.BiCircleDrawable;

/**
 * Custom FrameLayout with a gradient background and animation.
 */
public class GradientLayout extends FrameLayout {
    private long animDuration = 10000L;
    private ObjectAnimator animator;
    private float cornerRadius = 0.0F;
    private int gradientEnd;
    private int gradientMiddle;
    private int gradientStart;
    boolean gradientSet = false;
    public GradientLayout(Context context) {
        super(context);
        init(null);
    }

    public GradientLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public GradientLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public GradientLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    /**
     * Initializes the layout with custom attributes if provided.
     */
    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.GradientLayout);
            gradientStart = ta.getColor(R.styleable.GradientLayout_gradientStart, -16776961);
            gradientMiddle = ta.getColor(R.styleable.GradientLayout_gradientMiddle, -16777216);
            gradientEnd = ta.getColor(R.styleable.GradientLayout_gradientEnd, -16776961);
            cornerRadius = ta.getDimension(R.styleable.GradientLayout_cornerRadius, 0.0F);
            animDuration = ta.getInt(R.styleable.GradientLayout_animDuration, 10000);
            ta.recycle();
        }

    }

    /**
     * Sets up the animated gradient background.
     */
    public void setupGradient() {
        int oldHeight = getHeight();
        if(!gradientSet){
            LinearLayout gradientView = new LinearLayout(getContext());
            gradientView.setBackground(new BiCircleDrawable(gradientStart, gradientMiddle, gradientEnd, new float[]{0.2F, 0.6F, 1.0F}));
            gradientView.setLayoutParams(new LayoutParams(getWidth() * 2, getHeight() * 2));
            Log.d("GradientLayout", "setupGradient" + gradientView.getHeight() + " " + gradientView.getWidth());
            animator = new ObjectAnimator();
            animator.setTarget(gradientView);
            animator.setDuration(animDuration);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setPropertyName("translationY");
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setRepeatMode(ObjectAnimator.REVERSE);
            animator.setFloatValues(0.0F, -getHeight() / 2.0F);
            gradientView.setElevation(0);
            addView(gradientView, 0);
            setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, oldHeight));
            setCornerRadius(cornerRadius);
            animator.start();
            gradientSet = true;
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d("GradientLayout", "onAttachedToWindow");
        if (animator != null && !animator.isRunning()) {
            animator.start();
            Log.d("GradientLayout", "animator started");
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("GradientLayout", "onDetachedFromWindow");
        if (animator != null && animator.isRunning()) {
            animator.cancel();
            Log.d("GradientLayout", "animator cancelled");
        }
    }

    /**
     * Sets the corner radius for the layout.
     * @param radius the corner radius to set
     */
    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
            }
        });
        setClipToOutline(true);
    }
}
