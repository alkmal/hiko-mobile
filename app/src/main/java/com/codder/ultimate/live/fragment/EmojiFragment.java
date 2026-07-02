package com.codder.ultimate.live.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.FragmentEmojiBinding;
import com.codder.ultimate.fragments.BaseFragment;
import com.codder.ultimate.live.adapter.EmojiGridAdapter;
import com.codder.ultimate.live.model.GiftCategoryRoot;
import com.codder.ultimate.live.utils.OnEmojiSelectLister;

import java.util.Collections;
import java.util.List;

public class EmojiFragment extends BaseFragment {
    private static final String TAG = "EmojiFragment";

    private FragmentEmojiBinding binding;
    private EmojiGridAdapter emojiGridAdapter;
    private OnEmojiSelectLister onEmojiSelectLister;
    private GiftCategoryRoot.CategoryItem categoryRoot;

    public EmojiFragment(@NonNull GiftCategoryRoot.CategoryItem categoryRoot) {
        this.categoryRoot = categoryRoot;
    }

    public OnEmojiSelectLister getOnEmojiSelectLister() {
        return onEmojiSelectLister;
    }

    public void setOnEmojiSelectLister(OnEmojiSelectLister onEmojiSelectLister) {
        this.onEmojiSelectLister = onEmojiSelectLister;
        if (emojiGridAdapter != null) {
            emojiGridAdapter.setOnEmojiSelectLister(onEmojiSelectLister);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_emoji, container, false);
        return (binding != null) ? binding.getRoot() : new View(requireContext());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initMain();
        getData();
    }

    private void getData() {
        binding.layoutNoData.setVisibility(View.GONE);
        Log.d("TAG", "getData: gifts  size " + emojiGridAdapter.getItemCount());
        if (emojiGridAdapter.getItemCount() <= 0) {  //for shimmer issue
            binding.shimmerTab.setVisibility(View.VISIBLE);
        }
        emojiGridAdapter.giftRootDummies.clear();
        emojiGridAdapter.addData(sessionManager.getGiftsList(categoryRoot.getId()));
        Log.d(TAG, "getData: gift List ni data " + sessionManager.getGiftsList(categoryRoot.getId()));

        binding.shimmerTab.setVisibility(View.GONE);
    }

    private void initMain() {
        if (binding == null) return;
        emojiGridAdapter = new EmojiGridAdapter();
        binding.rvEmoji.setAdapter(emojiGridAdapter);

        // Listener may be set before or after adapter init, so always set current value
        if (onEmojiSelectLister != null) {
            emojiGridAdapter.setOnEmojiSelectLister(onEmojiSelectLister);
        }
    }
}
