package com.codder.ultimate.guestuser.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.MyLoader;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.FragmentGuestUserReelsBinding;
import com.codder.ultimate.fragments.BaseFragment;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.reels.activity.ReelsActivity;
import com.codder.ultimate.reels.adapter.ProfileVideoGridAdapter;
import com.codder.ultimate.reels.model.ReliteRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class GuestUserReelsFragment extends BaseFragment {

    private static final String TAG = "GuestUserReelsFragment";
    FragmentGuestUserReelsBinding binding;
    ProfileVideoGridAdapter profileVideoGridAdapter = new ProfileVideoGridAdapter();
    MyLoader myLoader = new MyLoader();
    private GuestProfileRoot.User userDummy;
    private int start = 0;

    public static GuestUserReelsFragment newInstance(GuestProfileRoot.User user) {
        GuestUserReelsFragment fragment = new GuestUserReelsFragment();
        Bundle args = new Bundle();
        args.putString("user_json", new Gson().toJson(user));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String userJson = getArguments().getString("user_json");
            if (userJson != null) {
                userDummy = new Gson().fromJson(userJson, GuestProfileRoot.User.class);
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guest_user_reels, container, false);
        binding.setLoader(myLoader);
        initMain();
        return binding.getRoot();
    }

    private void initMain() {

        binding.rvFeed.setAdapter(profileVideoGridAdapter);
        binding.swipeRefresh.setOnRefreshListener((refreshLayout) -> getData(false));
        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> getData(true));

        profileVideoGridAdapter.setOnVideoGridClickListener(new ProfileVideoGridAdapter.OnVideoGridClickListener() {
            @Override
            public void onVideoClick(int position) {
                startActivity(new Intent(getActivity(),
                        ReelsActivity.class).putExtra(Const.POSITION, position).putExtra(Const.DATA, new Gson().toJson(profileVideoGridAdapter.getCurrentItems())));
            }

            @Override
            public void onDeleteClick(ReliteRoot.VideoItem videoItem, int position) {
                if (videoItem.getUserId().equalsIgnoreCase(sessionManager.getUser().getId())) {

                    new PopupBuilder(requireContext()).deletePopup(getString(R.string.delete_relite_confirmation), new PopupBuilder.OnMultiButtonPopupLister() {
                        @Override
                        public void onClickContinue() {
                            deleteVideo(videoItem, position);
                        }

                        @Override
                        public void onClickCancel() {

                        }
                    });
                }
            }

        });


        getData(false);
    }

    public void deleteVideo(ReliteRoot.VideoItem postItem, int pos) {
        if (postItem == null || postItem.getId() == null) {
            Log.e(TAG, "DeleteVideo: Invalid post item or ID.");
            Toast.makeText(getActivity(), getString(R.string.invalid_video), Toast.LENGTH_SHORT).show();
            return;
        }

        Call<RestResponse> call = RetrofitBuilder.create().deleteRelite(postItem.getId());

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                if (response.body().isStatus() && response.isSuccessful()) {
                    Toast.makeText(getActivity(), getString(R.string.deleted_successfully), Toast.LENGTH_SHORT).show();
                    profileVideoGridAdapter.getCurrentItems().remove(pos);
                    profileVideoGridAdapter.notifyItemRemoved(pos);
                    Log.d(TAG, "DeleteVideo: Video deleted successfully at position " + pos);
                } else {
                    String errorMessage = (response.body() != null) ? response.body().getMessage() : "Failed to delete video";
                    Log.w(TAG, "DeleteVideo: " + errorMessage);
                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {
                Log.e(TAG, "DeleteVideo: Network failure", t);
                Toast.makeText(getActivity(), getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void getData(boolean isLoadMore) {
        if (userDummy == null) {
            Toast.makeText(getActivity(), getString(R.string.user_data_not_loaded), Toast.LENGTH_SHORT).show();
            binding.swipeRefresh.finishRefresh();
            binding.swipeRefresh.finishLoadMore();
            return;
        }

        if (isLoadMore) {
            start += Const.LIMIT;
        } else {
            myLoader.isFirstTimeLoading.set(true);
            profileVideoGridAdapter.submitList(new ArrayList<>());
            start = 0;
        }

        myLoader.noData.set(false);
        Call<ReliteRoot> call = RetrofitBuilder.create().getRelites(userDummy.getUserId(), "User", start, Const.LIMIT);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ReliteRoot> call, @NonNull Response<ReliteRoot> response) {
                myLoader.isFirstTimeLoading.set(false);
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();

                if (response.isSuccessful() && response.body() != null) {
                    ReliteRoot result = response.body();

                    if (result.isStatus() && result.getVideo() != null && !result.getVideo().isEmpty()) {
                        profileVideoGridAdapter.submitList(result.getVideo());
                        Log.d(TAG, "Loaded " + result.getVideo().size() + " videos.");
                    } else if (start == 0) {
                        myLoader.noData.set(true);
                        Log.d(TAG, "No videos found for this user.");
                    }
                } else {
                    Log.e(TAG, "getData: Error response - Code: " + response.code() + ", Message: " + response.message());
                    if (start == 0) {
                        myLoader.noData.set(true);
                    }
                }
            }

            @Override
            public void onFailure(Call<ReliteRoot> call, Throwable t) {
                myLoader.isFirstTimeLoading.set(false);
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();
                Log.e(TAG, "getData: Network failure", t);
                Toast.makeText(getActivity(), getString(R.string.failed_to_load_data_please_try_again), Toast.LENGTH_SHORT).show();

            }
        });
    }
}