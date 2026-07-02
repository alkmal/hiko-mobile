package com.codder.ultimate.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.recyclerview.widget.RecyclerView;

public class SingleTouchRecyclerView extends RecyclerView {

    public SingleTouchRecyclerView(Context context) {
        super(context);
    }

    public SingleTouchRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SingleTouchRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1) {
            return false;
        }

        return super.onTouchEvent(event);
    }
}
