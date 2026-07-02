package com.codder.ultimate.socialView;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.codder.ultimate.R;
import com.google.android.material.tabs.TabLayout;

public class CustomTabLayout extends TabLayout {

    public CustomTabLayout(@NonNull Context context) {
        super(context);
    }

    public CustomTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addTab(@NonNull Tab tab, boolean setSelected) {
        super.addTab(tab, setSelected);
        updateTabIcons();
    }

    private void updateTabIcons() {
        int iconColor = ContextCompat.getColor(getContext(), R.color.white);

        for (int i = 0; i < getTabCount(); i++) {
            Tab tab = getTabAt(i);
            if (tab != null) {
                View customView = tab.getCustomView();
                if (customView instanceof ImageView) {
                    ((ImageView) customView).setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                } else if (tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                }
            }
        }
    }
}
