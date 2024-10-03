package com.rock.rockui.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

public class BiCircleDrawable extends Drawable {
    private Paint paint;

    public BiCircleDrawable(int startColor,int midColor, int endColor, float[] positions) {
        paint = new Paint();
        // Define the colors for the gradient
        int[] colors = {startColor,midColor, endColor};
        // Define the positions of the gradient colors relative to the radius


        // Create a radial gradient shader
        Shader shader = new RadialGradient(0, 0, 1200, colors, positions, Shader.TileMode.MIRROR);

        paint.setShader(shader);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        // Draw the shape with the gradient
        canvas.drawRect(bounds, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        // Set alpha value for transparency
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // Set color filter if needed
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        // Return opacity level
        return paint.getAlpha();
    }
}
