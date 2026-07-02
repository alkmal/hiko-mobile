package com.codder.ultimate.profile.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.FragmentAvatarListBinding;
import com.codder.ultimate.fragments.BaseFragment;
import com.codder.ultimate.viewModel.AvatarViewModel;
import com.codder.ultimate.viewModel.ViewModelFactory;

public class AvatarListFragment extends BaseFragment {

    private FragmentAvatarListBinding binding;
    private AvatarViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_avatar_list, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, new ViewModelFactory(new AvatarViewModel())).get(AvatarViewModel.class);

        setupBindings();
        setupObservers();
        setupListeners();
    }

    private void setupBindings() {
        binding.setViewModel(viewModel);
        viewModel.init(requireContext());
        binding.setLifecycleOwner(getViewLifecycleOwner());
        viewModel.getAvatarList(false);
    }

    private void setupObservers() {
        viewModel.isLoadingComplete.observe(getViewLifecycleOwner(), complete -> {
            if (Boolean.TRUE.equals(complete)) {
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();
            }
        });

        viewModel.isPurchased.observe(getViewLifecycleOwner(), purchased -> {
            if (Boolean.TRUE.equals(purchased) && customDialogClass != null) {
                customDialogClass.dismiss();
            }
        });
    }

    private void setupListeners() {
        viewModel.avatarListAdapter.setOnAvatarClickListener((svgaItem, itemBinding) -> {
            if (svgaItem == null || itemBinding == null || sessionManager == null || sessionManager.getUser() == null) {
                return;
            }

            if (!svgaItem.isIsPurchase()) {
                if (svgaItem.getDiamond() <= sessionManager.getUser().getDiamond()) {
                    viewModel.purchaseSvga(svgaItem.getId(), svgaItem.getType(), itemBinding, svgaItem);
                    customDialogClass.show();
                } else {
                    Toast.makeText(requireContext(), R.string.you_don_t_have_required_diamonds, Toast.LENGTH_SHORT).show();
                }
            } else {
                boolean select = !svgaItem.isIsSelected();
                viewModel.selectSvga(svgaItem.getId(), svgaItem.getType(), itemBinding, svgaItem, select);
                customDialogClass.show();
            }
        });

        binding.swipeRefresh.setOnRefreshListener(refreshLayout -> viewModel.getAvatarList(false));
        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> viewModel.getAvatarList(true));
    }

}
