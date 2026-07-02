package com.codder.ultimate.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.codder.ultimate.MyLoader;
import com.codder.ultimate.R;
import com.codder.ultimate.activity.MainActivity;
import com.codder.ultimate.chat.activity.ChatActivity;
import com.codder.ultimate.chat.activity.FakeChatActivity;
import com.codder.ultimate.chat.adapter.ChatUserAdapter;
import com.codder.ultimate.chat.viewmodel.MessageViewModel;
import com.codder.ultimate.chat.viewmodel.MessageViewModelFactory;
import com.codder.ultimate.databinding.FragmentMessageBinding;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.profile.activity.ProfileActivity;
import com.codder.ultimate.retrofit.Const;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MessageFragment extends BaseFragment {

    private static final String TAG = "MessageFragment";
    private FragmentMessageBinding binding;
    private ChatUserAdapter chatUserAdapter;
    private MessageViewModel viewModel;
    private MyLoader myLoader;
    private boolean firstResumeDone = false;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_message, container, false);
        myLoader = new MyLoader();
        binding.setMyLoder(myLoader);

        viewModel = new ViewModelProvider(this, new MessageViewModelFactory(requireContext())).get(MessageViewModel.class);
        chatUserAdapter = new ChatUserAdapter();

        setupUI();
        observeViewModel();

        viewModel.fetchChatUsers(false);
        return binding.getRoot();
    }

    private void setupUI() {
        binding.rvMessage.setAdapter(chatUserAdapter);

        binding.ivProfile.setOnClickListener(v -> {
            ((MainActivity) getActivity()).openProfileFragment();
        });

        chatUserAdapter.setOnClickListener((position, user) -> {
            Intent intent = new Intent(requireContext(), user.isFake()
                    ? FakeChatActivity.class : ChatActivity.class);
            intent.putExtra(Const.CHATROOM, new Gson().toJson(user));
            Log.d(TAG, "setupUI:  ---> " + new Gson().toJson(user));

            startActivity(intent);
        });

        binding.swipeRefresh.setOnRefreshListener(refresh -> {
            viewModel.isRefreshing.setValue(true);
            viewModel.fetchChatUsers(false);
        });
        binding.swipeRefresh.setOnLoadMoreListener(refresh -> {
            viewModel.isRefreshing.setValue(false);
            viewModel.fetchChatUsers(true);
        });

        binding.layDeleteChat.setOnClickListener(v -> showDeleteDialog());

        myLoader.isFirstTimeLoading.set(true);
    }

    private void observeViewModel() {
        viewModel.chatUsers.observe(getViewLifecycleOwner(), users -> {
            Log.d("MessageFragment", "User list size: " + (users != null ? users.size() : 0));

            if (users != null) {
                chatUserAdapter.submitList(new ArrayList<>(users));
                binding.layDeleteChat.setVisibility(!users.isEmpty() ? View.VISIBLE : View.GONE);
            }

            if (users != null && !users.isEmpty()) {
                myLoader.isFirstTimeLoading.set(false);
                myLoader.noData.set(false);
            } else {
                myLoader.noData.set(true);
            }
        });

        viewModel.isNoData.observe(getViewLifecycleOwner(), noData -> {
            myLoader.noData.set(noData);
            myLoader.isFirstTimeLoading.set(false);
        });

        viewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            if (loading) {
                boolean shouldShowShimmer = chatUserAdapter.getItemCount() == 0;

                myLoader.isFirstTimeLoading.set(shouldShowShimmer);

            } else {
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();

                myLoader.isFirstTimeLoading.set(false);
            }
        });

        viewModel.messageToast.observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteDialog() {
        new PopupBuilder(requireContext()).showDeleteChatPopup(R.drawable.ic_delete_popup,"Confirm Deletion",getString(R.string.delete_confirmation_text),getString(R.string.delete), getString(R.string.cancel), new PopupBuilder.OnPopupClickListener(){

            @Override
            public void onClickContinue() {
                viewModel.deleteAllChats();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && !getActivity().isFinishing()
                && sessionManager != null
                && sessionManager.getUser() != null) {
            binding.ivProfile.setHomeUserImage(
                    sessionManager.getUser().getImage(),
                    sessionManager.getUser().getAvatarFrameImage(),
                    20
            );
        }

        if (firstResumeDone && (viewModel.isLoading.getValue() == null || !viewModel.isLoading.getValue())) {
            viewModel.fetchChatUsers(false);
        }
        firstResumeDone = true;
    }
}