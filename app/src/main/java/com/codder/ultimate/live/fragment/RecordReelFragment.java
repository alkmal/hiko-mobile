package com.codder.ultimate.live.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.FragmentRecordReelBinding;

public class RecordReelFragment extends Fragment {

    private FragmentRecordReelBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_record_reel, container, false);
        return binding.getRoot();
    }
}