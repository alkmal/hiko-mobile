package com.codder.ultimate.profile.fragment;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.codder.ultimate.R;
import com.codder.ultimate.databinding.FragmentSvgaListBinding;
import com.codder.ultimate.databinding.ItemSvgaListBinding;
import com.codder.ultimate.fragments.BaseFragment;
import com.codder.ultimate.popups.PopupSvgaPreview;
import com.codder.ultimate.profile.adapter.SvgaListAdapter;
import com.codder.ultimate.profile.modelclass.SvgaListRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.viewModel.SvgaViewModel;
import com.codder.ultimate.viewModel.ViewModelFactory;

public class SvgaListFragment extends BaseFragment {

    private static final String TAG = "SvgaListFragment";
    private FragmentSvgaListBinding binding;
    private SvgaViewModel viewModel;
    private SvgaListAdapter svgaListAdapter;

    public SvgaListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSvgaListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViewModel();
        initializeView();
        initializeListeners();
    }

    private void initializeViewModel() {
        Application application = requireActivity().getApplication();
        ViewModelFactory factory = new ViewModelFactory(new SvgaViewModel(application));
        viewModel = new ViewModelProvider(this, factory).get(SvgaViewModel.class);
    }

    private void initializeView() {
        if (binding == null || viewModel == null) return;

        svgaListAdapter = new SvgaListAdapter();
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.rvHostList.setAdapter(svgaListAdapter);

        viewModel.getSvgaList(false, Const.SVGA);

        viewModel.isLoadingComplete.observe(getViewLifecycleOwner(), isComplete -> {
            if (isComplete != null && isComplete) {
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();
            }
        });

        binding.swipeRefresh.setOnRefreshListener(refreshLayout -> viewModel.getSvgaList(false, Const.SVGA));
        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> viewModel.getSvgaList(true, Const.SVGA));

        viewModel.isPurchased.observe(getViewLifecycleOwner(), isPurchased -> {
            if (isPurchased != null && isPurchased) {
                if (customDialogClass != null) {
                    customDialogClass.dismiss();
                }
            }
        });
    }

    private void initializeListeners() {
        viewModel.svgaListAdapter.setOnSvgaClickListener(new SvgaListAdapter.onSvgaClickListener() {
            @Override
            public void onPurchaseClick(SvgaListRoot.DataItem svgaItem, ItemSvgaListBinding binding) {
                Log.d(TAG, "onPurchaseClick: " + svgaItem.toString());

                if (!svgaItem.isIsPurchase()) {
                    if (svgaItem.getDiamond() <= sessionManager.getUser().getDiamond()) {
                        viewModel.purchaseSvga(svgaItem.getId(), svgaItem.getType(), binding, svgaItem);
                        customDialogClass.show();
                    } else {
                        if (!requireActivity().isFinishing()) {
                            Toast.makeText(requireActivity(), getString(R.string.you_don_t_have_required_diamonds), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (svgaItem.isIsPurchase() && !svgaItem.isIsSelected()) {
                    viewModel.selectSvga(svgaItem.getId(), svgaItem.getType(), binding, svgaItem, true);
                    customDialogClass.show();
                } else if (svgaItem.isIsPurchase() && svgaItem.isIsSelected()) {
                    viewModel.selectSvga(svgaItem.getId(), svgaItem.getType(), binding, svgaItem, false);
                    customDialogClass.show();
                }
            }

            @Override
            public void onSvgaClick(SvgaListRoot.DataItem svgaItem) {
                requireActivity().runOnUiThread(() ->
                        new PopupSvgaPreview(requireActivity(), svgaItem.getImage(),
                                sessionManager.getUser().getAvatarFrame() != null &&
                                        !sessionManager.getUser().getAvatarFrame().getImage().isEmpty() ?
                                        sessionManager.getUser().getAvatarFrame().getImage() : "", sessionManager.getUser().getImage()));
            }
        });
    }
}
