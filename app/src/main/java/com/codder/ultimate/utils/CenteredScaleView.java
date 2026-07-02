package com.codder.ultimate.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewParent;
import android.widget.OverScroller;

import androidx.annotation.Nullable;

public class CenteredScaleView extends View {

    public interface OnValueChangeListener {
        void onValueChanged(float value, boolean fromUser);

        void onGestureStart();

        void onGestureEnd(float value);
    }

    // NEW: progress baseline options
    public enum Baseline {MIN, ZERO}

    private OnValueChangeListener listener;

    public void setOnValueChangeListener(OnValueChangeListener l) {
        this.listener = l;
    }

    // Range/step
    private float min = -100f, max = 100f, step = 5f;

    public void configure(float min, float max, float step) {
        this.min = min;
        this.max = max;
        this.step = step > 0 ? step : 5f;
        invalidate();
    }

    // Current value
    private float value = 0f;

    public float getValue() {
        return value;
    }

    public void setValue(float v, boolean fromUser) {
        float clamped = clamp(v, min, max);
        if (clamped != value) {
            value = clamped;
            if (listener != null) listener.onValueChanged(value, fromUser);
            invalidate();
        }
    }

    // NEW: control progress & dot
    private boolean showDot = false;
    private boolean showZeroMarker = false;
    private boolean showProgress = true;
    private Baseline baseline = Baseline.ZERO; // good for -100..100 (exposure)

    /** Configure progress & dot visuals. Call per-control. */
    public void setProgressAppearance(boolean showProgress, boolean showDot, Baseline baseline) {
        this.showProgress = showProgress;
        this.showDot = showDot;
        if (baseline != null) this.baseline = baseline;
        invalidate();
    }

    // Visuals
    private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackLeft = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackRight;

    // NEW: progress & dot paints
    private final Paint progressPosPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressNegPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // (kept even if unused)
    private float pxPerUnit = 3f;

    // Gesture
    private final OverScroller scroller;
    private VelocityTracker velocityTracker;
    private float lastX;
    private boolean dragging = false;
    private int lastDetent = Integer.MIN_VALUE;

    // Misc
    private final float density;
    private static final float EPS = 1e-4f;
    private static final int MAJOR_EVERY = 5;

    private boolean hapticsEnabled = true;

    public void setHapticsEnabled(boolean enabled) {
        this.hapticsEnabled = enabled;
    }

    private void tick(int constant) {
        if (hapticsEnabled) performHapticFeedback(constant);
    }

    // Visual ticks only (you already added this earlier)
    private float tickStep = 5f;
    public void setTickStep(float s) { tickStep = Math.max(0.1f, s); invalidate(); }

    // NEW: haptic detent spacing while dragging
    private float detentStep = 5f;
    public void setDetentStep(float s) { detentStep = Math.max(0.1f, s); }

    /** Fire a haptic for *each* detent crossed between fromDetent -> toDetent. */
    private void tickDetentsPassed(int fromDetent, int toDetent, float prevVal, float newVal) {
        if (fromDetent == toDetent) return;
        final int dir = toDetent > fromDetent ? 1 : -1;
        for (int d = fromDetent + dir; d != toDetent + dir; d += dir) {
            float detentValue = d * step;
            boolean zeroCrossHere = (prevVal <= 0 && detentValue > 0) || (prevVal >= 0 && detentValue < 0);
            tick(zeroCrossHere ? HapticFeedbackConstants.CONTEXT_CLICK
                    : HapticFeedbackConstants.CLOCK_TICK);
        }
    }

    private boolean snapToStep = true; // allow disabling snap if you need exact values like 37

    public void setSnapToStep(boolean snap) {
        this.snapToStep = snap;
    }

    private int activePointerId = MotionEvent.INVALID_POINTER_ID;


    public CenteredScaleView(Context c, @Nullable AttributeSet a) {
        super(c, a);

        density = getResources().getDisplayMetrics().density;

        // Tick + center line styling
        tickPaint.setStrokeWidth(dp(1));
        tickPaint.setARGB(180, 255, 255, 255);

        centerPaint.setStrokeWidth(dp(2));
        centerPaint.setARGB(255, 255, 255, 255);

        // Base track: left & right (different alpha levels)
        trackLeft.setStrokeCap(Paint.Cap.ROUND);
        trackLeft.setStrokeWidth(dp(1.5f));
        trackLeft.setARGB(160, 255, 255, 255);
        trackRight = new Paint(trackLeft);
        trackRight.setARGB(110, 255, 255, 255);

        // Optional text/tick labels
        textPaint.setTextSize(dp(13));
        textPaint.setARGB(230, 255, 255, 255);

        // Marker circle above zero tick
        markerPaint.setARGB(255, 255, 255, 255);

        // Progress segment styling (positive vs negative sides)
        progressPosPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPosPaint.setStrokeWidth(dp(6));
        progressPosPaint.setARGB(230, 255, 255, 255); // brighter for positive
        progressNegPaint.setStrokeCap(Paint.Cap.ROUND);
        progressNegPaint.setStrokeWidth(dp(6));
        progressNegPaint.setARGB(160, 255, 255, 255); // dimmer for negative

        // Dot (thumb) styling
        dotFillPaint.setStyle(Paint.Style.FILL);
        dotFillPaint.setARGB(255, 255, 255, 255);
        dotStrokePaint.setStyle(Paint.Style.STROKE);
        dotStrokePaint.setStrokeWidth(dp(2));
        dotStrokePaint.setARGB(120, 0, 0, 0); // subtle outline

        // Gesture physics
        scroller = new OverScroller(getContext());
        setClickable(true);

        setHapticFeedbackEnabled(true);
    }


    private float dp(float d) {
        return d * density;
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0) pxPerUnit = Math.max(1f, w / 80f); // ~50 units across feels natural
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float w = getWidth(), h = getHeight();
        final float cx = w / 2f, cy = h / 2f;

        // Track bounds inside the view
        final float pad = dp(12);
        final float l = pad, r = w - pad;
        final float trackY = cy;


        // Ruler moves under a fixed center; positive value -> ruler shifts accordingly
        final float offsetPx = -value * pxPerUnit;
        final float xZero = cx + offsetPx;

        final float rr = dp(3);      // tiny radius
        final float gapDp = dp(4);      // visual gap between tick top and dot edge
        final float tickH0 = dp(20);    // height of the v==0 tick in your ruler
        final float tickTopY = trackY - tickH0;

        // dot center sits above tick by (gap + radius)
        final float dotY = tickTopY - gapDp - rr;

        // only draw if visible inside padded bounds
        if (xZero >= l && xZero <= r) {
            canvas.drawCircle(xZero, dotY, rr, markerPaint);     // subtle fill
            // optional faint ring (keep if you like the look)
            // canvas.drawCircle(xZero, dotY, rr, dotStrokePaint);
        }

        // ---- Marker above the 0 index (hide when centered) ----
        final int detentIndex = Math.round(value / step);
        if (showZeroMarker && detentIndex != 0) {
            final float x0 = cx + offsetPx;
            final float markerY = cy - dp(20);
            canvas.drawCircle(x0, markerY, dp(4), markerPaint);
        }

        // Where min/max land on screen for the current offset
        final float minX = cx + (min * pxPerUnit) + offsetPx;
        final float maxX = cx + (max * pxPerUnit) + offsetPx;

        // Clamp the rendered track segment to the visible area
        final float segL = clamp(minX, l, r);
        final float segR = clamp(maxX, l, r);

        // Draw left (negative) and right (positive) segments separately to keep the center blend
        if (segL < cx) {
            canvas.drawLine(segL, trackY, Math.min(cx, segR), trackY, trackLeft);
        }
        if (segR > cx) {
            canvas.drawLine(Math.max(cx, segL), trackY, segR, trackY, trackRight);
        }

        // Tick styling
        final float minorH = dp(14);
        final float majorH = dp(14);
        final float centerH = dp(20);

        // Compute only the visible tick range
        final float extra = dp(12);
        final float vStart = clamp(((l - extra) - cx - offsetPx) / pxPerUnit, min, max);
        final float vEnd = clamp(((r + extra) - cx - offsetPx) / pxPerUnit, min, max);

// Start at first tick aligned to tickStep
        final float firstTick = (float) Math.ceil(vStart / tickStep) * tickStep;
        int index = Math.round(firstTick / tickStep);
        for (float v = firstTick; v <= vEnd + EPS; v += tickStep, index++) {
            final float x = cx + (v * pxPerUnit) + offsetPx;
            final boolean isCenter = Math.abs(v) < EPS;
            final boolean isMajor  = (index % MAJOR_EVERY) == 0;
            final float tickH = isCenter ? centerH : (isMajor ? majorH : minorH);
            final Paint p     = isCenter ? centerPaint : tickPaint;
            canvas.drawLine(x, trackY - tickH, x, trackY, p);
        }


        // Fixed center hairline (current value)
//        canvas.drawLine(cx, 0, cx, h, centerPaint);
// Center tick styled like iOS: short + rounded caps (no full-height hairline)
        final float centerTickLen = dp(26);     // length of the center tick
        final float centerTickW = dp(3);      // a bit thicker than normal ticks
        final float centerDotR = dp(2.5f);   // optional tiny dot above the tick
        final float centerDotGap = dp(2);

// Temporarily adjust the paint for the center tick
        final Paint.Cap oldCap = centerPaint.getStrokeCap();
        final float oldW = centerPaint.getStrokeWidth();

        centerPaint.setStrokeCap(Paint.Cap.ROUND);
        centerPaint.setStrokeWidth(centerTickW);

// draw the short vertical tick around the track baseline (trackY)
        canvas.drawLine(cx, trackY - centerTickLen, cx, trackY, centerPaint);

// restore original paint settings for any later use
        centerPaint.setStrokeCap(oldCap);
        centerPaint.setStrokeWidth(oldW);

        // ---- NEW: progress from baseline → current (cx) ----
        if (showProgress) {
            final float xCurrent = cx;
            final float xBase = (baseline == Baseline.ZERO)
                    ? (cx + offsetPx)                        // value = 0 tick
                    : (cx + (min * pxPerUnit) + offsetPx);   // value = min tick (e.g., 0 for 0..100)

            final float a = clamp(xBase, l, r);
            final float b = clamp(xCurrent, l, r);

            final boolean negativeSide = (baseline == Baseline.ZERO) && (value < 0f);
            final Paint p = negativeSide ? progressNegPaint : progressPosPaint;

            canvas.drawLine(a, trackY, b, trackY, p);
        }

       /* // ---- Dot at index 0 tick ----
        if (showDot) {
          final float rr = dp(6);

            // Position of index 0 tick on screen
            final float xZero = cx + offsetPx;

            canvas.drawCircle(xZero, trackY, rr, dotFillPaint);
            canvas.drawCircle(xZero, trackY, rr, dotStrokePaint);
        }*/

    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (velocityTracker == null) velocityTracker = VelocityTracker.obtain();
        velocityTracker.addMovement(e);

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                activePointerId = e.getPointerId(0);
                lastX = e.getX();
                if (!scroller.isFinished()) scroller.abortAnimation();
                dragging = true;
                if (listener != null) listener.onGestureStart();
                final ViewParent parent = getParent();
                if (parent != null) parent.requestDisallowInterceptTouchEvent(true);

                // was: Math.round(value / step)
                lastDetent = Math.round(value / detentStep);
                return true;
            }

            case MotionEvent.ACTION_MOVE: {
                final float dx = e.getX() - lastX;
                lastX = e.getX();

                float prevValue = value;
                float newValue = clamp(value - (dx / pxPerUnit), min, max);

                // was: old/new detent using 'step'
                int oldDetent = lastDetent;
                int newDetent = Math.round(newValue / detentStep);

                if (newDetent != oldDetent) {
                    int dir = newDetent > oldDetent ? 1 : -1;
                    for (int d = oldDetent + dir; d != newDetent + dir; d += dir) {
                        float detentVal = d * detentStep;
                        boolean zeroCross = (prevValue <= 0 && detentVal > 0) ||
                                (prevValue >= 0 && detentVal < 0);
                        performHapticFeedback(
                                zeroCross ? HapticFeedbackConstants.CONTEXT_CLICK
                                        : HapticFeedbackConstants.CLOCK_TICK
                        );
                    }
                    lastDetent = newDetent;
                }

                setValue(newValue, true);
                return true;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                dragging = false;

                if (velocityTracker != null) {
                    velocityTracker.computeCurrentVelocity(1000);
                    // use the active pointer id when available
                    float vx = activePointerId != MotionEvent.INVALID_POINTER_ID
                            ? velocityTracker.getXVelocity(activePointerId)
                            : velocityTracker.getXVelocity();

                    velocityTracker.recycle();
                    velocityTracker = null;
                    activePointerId = MotionEvent.INVALID_POINTER_ID;

                    // Only fling if we exceeded the system minimum fling velocity
                    int minFling = android.view.ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
                    if (Math.abs(vx) >= minFling) {
                        final float velocityUnitsPerSec = -vx / pxPerUnit;
                        scroller.fling(
                                Math.round(value * 1000), 0,
                                Math.round(velocityUnitsPerSec * 1000), 0,
                                Math.round(min * 1000), Math.round(max * 1000),
                                0, 0
                        );
                        postInvalidateOnAnimation();
                    } else {
                        // No fling: snap immediately (or don’t, if disabled)
                        if (snapToStep) {
                            float snapped = Math.round(value / step) * step;
                            setValue(snapped, true);
                            tick(HapticFeedbackConstants.CLOCK_TICK);
                        } else {
                            // just ensure we’re in bounds
                            setValue(clamp(value, min, max), true);
                        }
                    }
                } else {
                    // No velocity tracker: just snap (or not)
                    if (snapToStep) {
                        float snapped = Math.round(value / step) * step;
                        setValue(snapped, true);
                        tick(HapticFeedbackConstants.CLOCK_TICK);
                    }
                }

                if (listener != null) listener.onGestureEnd(value);
                final ViewParent parent = getParent();
                if (parent != null) parent.requestDisallowInterceptTouchEvent(false);
                return true;
            }
        }
        return super.onTouchEvent(e);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            final float vThousand = scroller.getCurrX();
            final float newValue = vThousand / 1000f;
            setValue(newValue, true);
            postInvalidateOnAnimation();
        } else if (!dragging && snapToStep) {
            final float snapped = Math.round(value / step) * step;
            if (Math.abs(snapped - value) > EPS) {
                setValue(snapped, true);
                tick(HapticFeedbackConstants.CLOCK_TICK);
            }
        }
    }
}