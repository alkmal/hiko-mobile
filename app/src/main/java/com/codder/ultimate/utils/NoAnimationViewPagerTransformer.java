package com.codder.ultimate.utils;


import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

/**
 * A custom PageTransformer that disables default ViewPager animations and applies
 * a simple translation with opacity effect.
 */
public class NoAnimationViewPagerTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(@NonNull View page, float position) {
        if (page == null) return;

        // Translate the page based on its position
        page.setTranslationX(-position * page.getWidth());

        // Optionally fade pages in/out (can be removed if not needed)
        float alpha = 1.0f - Math.abs(position);
        page.setAlpha(Math.max(0f, Math.min(1f, alpha)));
    }
}

