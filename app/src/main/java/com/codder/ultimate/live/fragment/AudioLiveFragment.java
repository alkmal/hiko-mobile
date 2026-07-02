package com.codder.ultimate.live.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.FragmentAudioLiveBinding;

public class AudioLiveFragment extends Fragment {
    FragmentAudioLiveBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_audio_live, container, false);
        return binding.getRoot();
    }
}