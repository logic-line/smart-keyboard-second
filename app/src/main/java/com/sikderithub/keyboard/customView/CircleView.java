package com.sikderithub.keyboard.customView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class CircleView extends View {

    private Paint circlePaint;
    private int circleColor;
    private Drawable backgroundDrawable;


    public CircleView(Context context) {
        super(context);
        init();
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2;

        // Draw background drawable
        if(backgroundDrawable!=null){
            backgroundDrawable.setBounds(0, 0, width, height);
            backgroundDrawable.draw(canvas);
        }

        // Draw circle
        circlePaint.setColor(circleColor);
        canvas.drawCircle(width / 2f, height / 2f, radius, circlePaint);
    }

    public void setBackgroundDrawable(Drawable drawable) {
        if (drawable != null) {
            backgroundDrawable = drawable;
            invalidate(); // Redraw the view with new background drawable
        }
    }



}
