package com.codder.ultimate.live.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.codder.ultimate.live.fragment.EmojiFragment;
import com.codder.ultimate.live.model.GiftCategoryRoot;
import com.codder.ultimate.live.utils.OnEmojiSelectLister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmojiViewPagerAdapter extends FragmentPagerAdapter {

    private List<GiftCategoryRoot.CategoryItem> category = new ArrayList<>();
    private OnEmojiSelectLister onEmojiSelectLister;

    public EmojiViewPagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    /**
     * Defensive setter, never allows null.
     */
    public void addData(List<GiftCategoryRoot.CategoryItem> category) {
        if (category != null) {
            this.category = new ArrayList<>(category);
        } else {
            this.category = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public Fragment getItem(int position) {
        if (category == null || category.isEmpty() || position < 0 || position >= category.size()) {
            return new Fragment();
        }
        GiftCategoryRoot.CategoryItem categoryItem = category.get(position);
        EmojiFragment emojiFragment = new EmojiFragment(categoryItem);

        if (onEmojiSelectLister != null) {
            emojiFragment.setOnEmojiSelectLister(onEmojiSelectLister);
        }
        return emojiFragment;
    }

    @Override
    public int getCount() {
        return category != null ? category.size() : 0;
    }

    public void setOnEmojiSelectLister(OnEmojiSelectLister onEmojiSelectLister) {
        this.onEmojiSelectLister = onEmojiSelectLister;
    }
}
