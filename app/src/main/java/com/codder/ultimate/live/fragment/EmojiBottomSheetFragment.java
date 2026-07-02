package com.codder.ultimate.live.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.FragmentEmojiBottomsheetBinding;
import com.codder.ultimate.databinding.ItemEmojiGridBinding;
import com.codder.ultimate.live.adapter.EmojiViewPagerAdapter;
import com.codder.ultimate.live.adapter.UserListAdapter;
import com.codder.ultimate.live.model.GiftCategoryRoot;
import com.codder.ultimate.live.utils.UserSelectableClass;
import com.codder.ultimate.live.viewModel.EmojiSheetViewModel;
import com.codder.ultimate.profile.fragment.MyWalletActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class EmojiBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String TAG = "EmojiBottomSheetFragment";
    private FragmentEmojiBottomsheetBinding binding;
    private final String[] country = {" 1 ", " 2 ", " 3 ", " 4 ", " 5 ", " 6 ", " 7 ", " 8 ", " 9 ", " 10"};
    private boolean isMultiUserSelectable = false;
    private EmojiViewPagerAdapter emojiViewPagerAdapter;
    private ItemEmojiGridBinding lastBinding = null;
    private EmojiSheetViewModel parentViewModel;
    private SessionManager sessionManager;

    public EmojiBottomSheetFragment() {
    }

    public EmojiBottomSheetFragment(boolean isMultiUserSelectable) {
        this.isMultiUserSelectable = isMultiUserSelectable;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_emoji_bottomsheet, container, false);
        if (getActivity() == null) return binding.getRoot();

        parentViewModel = new ViewModelProvider(requireActivity()).get(EmojiSheetViewModel.class);
        sessionManager = new SessionManager(requireActivity());

        binding.setViewmodel(parentViewModel);

        initMain();

        if (isMultiUserSelectable) {
            UserListAdapter userListAdapter = new UserListAdapter(requireContext(), userSelectableClass -> {
                updateAllButtonState();
                updateSendButtonState();
            });
            parentViewModel.userListAdapter = userListAdapter;
            binding.rvUsers.setAdapter(userListAdapter);

            if (parentViewModel.users != null) {
                List<UserSelectableClass> filteredUsers = parentViewModel.userListAdapter.filterOutSessionUser(parentViewModel.users);
                parentViewModel.userListAdapter.addData(filteredUsers);
            }
        } else {
            binding.userSelectionLayout.setVisibility(View.GONE);
        }

        setupListeners();

        return binding.getRoot();
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        }
        View view = getView();
        if (view != null && view.getParent() instanceof View) {
            view.post(() -> {
                View parent = (View) view.getParent();
                if (parent.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) parent.getLayoutParams();
                    CoordinatorLayout.Behavior behavior = params.getBehavior();
                    if (behavior instanceof BottomSheetBehavior) {
                        BottomSheetBehavior<?> bottomSheetBehavior = (BottomSheetBehavior<?>) behavior;
                        bottomSheetBehavior.setPeekHeight(view.getMeasuredHeight());
                    }
                }
            });
        }
    }

    private void setupListeners() {
        binding.tvRecharge.setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), MyWalletActivity.class));
            }
        });

        // LiveData observer for coins
        parentViewModel.localUserCoin.observe(getViewLifecycleOwner(), value -> {
            if (binding != null) {
                double v = value != null ? value : 0d;
                binding.tvCoin.setText(RayziUtils.formatCoin(v)); // format only for UI
            }
        });

        emojiViewPagerAdapter.setOnEmojiSelectLister((binding1, giftRoot) -> {
            if (lastBinding != null && lastBinding.itemEmoji != null) {
                lastBinding.itemEmoji.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_gift_unselected));
            }
            if (binding1 != null && binding1.itemEmoji != null && getActivity() != null) {
                binding1.itemEmoji.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_gift_selected));
            }
            lastBinding = binding1;
            parentViewModel.selectedGift.setValue(giftRoot);
        });

        // Selected gift observer
        parentViewModel.selectedGift.observe(getViewLifecycleOwner(), giftItem -> {
            updateSendButtonState();
        });


        binding.btnAll.setOnClickListener(view -> {
            parentViewModel.userListAdapter.getCurrentList().forEach(user -> {
                Log.d(TAG, "User ID: " + user.getSeatItem().getUserId() + " Selected: " + user.isSelected());
            });


            parentViewModel.userListAdapter.selectAll();
            updateAllButtonState();

            List<UserSelectableClass> selectedUsers = parentViewModel.userListAdapter.getCurrentList().stream()
                    .filter(UserSelectableClass::isSelected) // Get the selected users
                    .collect(Collectors.toList());

            boolean hasSelectedUsers = !selectedUsers.isEmpty();

            if (parentViewModel.userListAdapter.getVisibleItems() != null &&
                    !parentViewModel.userListAdapter.getVisibleItems().isEmpty()) {
                if (hasSelectedUsers) {
                    binding.btnAll.setBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.bg_selected_filter));
                } else {
                    binding.btnAll.setBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.bg_all_unselected));
                }
            } else {
                Toast.makeText(getContext(), R.string.no_user_found, Toast.LENGTH_SHORT).show();
                binding.btnAll.setBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.bg_all_unselected));
            }
            updateSendButtonState();
        });

        binding.btnSend.setOnClickListener(v -> {
            boolean isGiftSelected = parentViewModel.selectedGift.getValue() != null;
            boolean hasSelectedUsers = true;

            if (isMultiUserSelectable) {
                List<String> selectedUsers = safeSelectedUserIds();
                hasSelectedUsers = !selectedUsers.isEmpty();
            }

            if (isGiftSelected && hasSelectedUsers) {
                parentViewModel.selectedGift.getValue().setCount(  Integer.parseInt(binding.tvGiftCount.getText().toString().replace("x","").trim()));
                parentViewModel.finalGift.setValue(parentViewModel.selectedGift.getValue());
                dismiss();
            } else {
                String message = !isGiftSelected
                        ? getString(R.string.please_select_a_gift)
                        : getString(R.string.please_select_at_least_one_user);
                RayziUtils.showToast(getActivity(), message);
            }
        });

    }

    private void updateSendButtonState() {
        if (binding == null || parentViewModel == null || getActivity() == null) return;

        boolean isGiftSelected = parentViewModel.selectedGift.getValue() != null;
        boolean hasSelectedUsers = true;

        if (isMultiUserSelectable) {
            List<String> selectedUsers = safeSelectedUserIds();
            hasSelectedUsers = !selectedUsers.isEmpty();
        }

        Log.d(TAG, "isGiftSelected: " + isGiftSelected);
        Log.d(TAG, "hasSelectedUsers: " + hasSelectedUsers);

        int sendBtnColor = (isGiftSelected && hasSelectedUsers) ? R.color.pink : R.color.light_gray_color;
    }

    private void updateAllButtonState() {
        if (binding == null || parentViewModel == null || getContext() == null) return;

        List<UserSelectableClass> visibleUsers = parentViewModel.userListAdapter.getVisibleItems();

        if (visibleUsers == null || visibleUsers.isEmpty()) {
            binding.btnAll.setBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.bg_all_unselected));
            return;
        }

        boolean allSelected = true;
        for (UserSelectableClass user : visibleUsers) {
            if (!user.isSelected()) {
                allSelected = false;
                break;
            }
        }


        if (allSelected) {
            binding.btnAll.setBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.bg_selected_filter));
        } else {
            binding.btnAll.setBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.bg_all_unselected));
        }

    }


    private List<String> safeSelectedUserIds() {
        if (parentViewModel == null || parentViewModel.userListAdapter == null)
            return Collections.emptyList();
        try {
            return parentViewModel.userListAdapter.getCurrentList().stream()
                    .filter(UserSelectableClass::isSelected)
                    .map(user -> {
                        if (user != null && user.getSeatItem() != null)
                            return user.getSeatItem().getUserId();
                        else
                            return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            Log.e(TAG, "Error in safeSelectedUserIds", e);
            return Collections.emptyList();
        }
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialogTheme;
    }

    private void initMain() {
        parentViewModel.selectedGift.setValue(null);
        parentViewModel.finalGift.setValue(null);

        double diamond = (sessionManager.getUser() != null) ? sessionManager.getUser().getDiamond() : 0;
        binding.tvCoin.setText(RayziUtils.formatCoin(diamond));
        emojiViewPagerAdapter = new EmojiViewPagerAdapter(getChildFragmentManager());
        if (parentViewModel.categoryItemMutableLiveData.getValue() != null)
            emojiViewPagerAdapter.addData(parentViewModel.categoryItemMutableLiveData.getValue());
        binding.viewPager.setAdapter(emojiViewPagerAdapter);
        binding.tabLayout.setupWithViewPager(binding.viewPager);

        TabLayout.Tab first = binding.tabLayout.getTabAt(0);
        if (first != null) first.select();

        setTab(parentViewModel.categoryItemMutableLiveData.getValue());
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                if (v != null && getActivity() != null) {
                    TextView tv = v.findViewById(R.id.tvTab);
                    tv.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                    tv.setBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.home_tab_selectedbg));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                if (v != null && getActivity() != null) {
                    TextView tv = v.findViewById(R.id.tvTab);
                    tv.setBackground(ContextCompat.getDrawable(requireActivity(),R.drawable.gift_tab_unselectedbg));
                    tv.setTextColor(ContextCompat.getColor(getActivity(), R.color.white_76));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        binding.tvGiftCount.setText("1x");

        binding.ivPlus.setOnClickListener(v -> {
            try {
                int count = Integer.parseInt(binding.tvGiftCount.getText().toString().replace("x","").trim());
                if (count == 10){
                    return;
                }else {
                    count++;
                }
                binding.tvGiftCount.setText(String.valueOf(count) + "x");
            } catch (Exception e) {
                binding.tvGiftCount.setText("1x");
            }
        });

        binding.ivMinus.setOnClickListener(v -> {
            try {
                int count = Integer.parseInt(binding.tvGiftCount.getText().toString().replace("x","").trim());
                if (count > 1) {
                    count--;
                }
                binding.tvGiftCount.setText(String.valueOf(count) + "x");
            } catch (Exception e) {
                binding.tvGiftCount.setText("1x");
            }
        });


    }

    private void setTab(List<GiftCategoryRoot.CategoryItem> country) {
        binding.tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        binding.tabLayout.removeAllTabs();
        if (getActivity() == null || country == null) return;
        for (int i = 0; i < country.size(); i++) {
            binding.tabLayout.addTab(binding.tabLayout.newTab()
                    .setCustomView(createCustomView(i, country.get(i).getName())));
        }

        ViewGroup tabStrip = (ViewGroup) binding.tabLayout.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            View tabView = tabStrip.getChildAt(i);
            tabView.setPadding(0, 0, 10, 0);
        }

    }

    private View createCustomView(int i, String s) {
        if (getActivity() == null) return null;
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.custom_gift_tab, null);
        TextView tv = v.findViewById(R.id.tvTab);
        tv.setText(s);
        if (i == 0) {
            tv.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white));
            tv.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.home_tab_selectedbg));
        } else {
            tv.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white_76));
            tv.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.gift_tab_unselectedbg));
        }


        return v;
    }

    public void setCoin(int coin) {
        if (binding != null) {
            binding.tvCoin.setText(RayziUtils.formatCoin(coin));
        }
    }
}