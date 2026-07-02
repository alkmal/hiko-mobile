package com.codder.ultimate.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class TicksOverlayView extends View {

    private float from = -100f, to = 100f, step = 5f;
    private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public TicksOverlayView(Context c, AttributeSet a) {
        super(c, a);
        tickPaint.setStrokeWidth(dp(1));
        tickPaint.setAlpha(140); // subtle
        centerPaint.setStrokeWidth(dp(2));
        centerPaint.setAlpha(255); // strong
    }

    public void configure(float from, float to, float step) {
        this.from = from; this.to = to; this.step = step <= 0 ? 5f : step;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight();
        float cx = w / 2f;
        float startX = dp(8), endX = w - dp(8);
        float y = h / 2f;

        // Slim iOS-like track (we draw the track; Material track stays transparent)
        Paint left = new Paint(Paint.ANTI_ALIAS_FLAG);
        left.setStrokeCap(Paint.Cap.ROUND);
        left.setStrokeWidth(dp(4));
        left.setARGB(160, 255, 255, 255);

        Paint right = new Paint(left);
        right.setARGB(110, 255, 255, 255);

        canvas.drawLine(startX, y, cx, y, left);
        canvas.drawLine(cx, y, endX, y, right);

        // Minor ticks each 'step', with a bold center tick
        float span = to - from;
        for (float v = from; v <= to; v += step) {
            float x = startX + (v - from) / span * (endX - startX);
            boolean isCenter = (Math.abs(v) < 0.0001f);
            float tickH = isCenter ? dp(16) : dp(8);
            Paint p = isCenter ? centerPaint : tickPaint;
            canvas.drawLine(x, y - tickH/2f, x, y + tickH/2f, p);
        }
    }


    private float dp(float d) {
        return d * getResources().getDisplayMetrics().density;
    }
}
