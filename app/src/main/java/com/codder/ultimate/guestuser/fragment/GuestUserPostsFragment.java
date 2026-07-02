package com.codder.ultimate.guestuser.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.MyLoader;
import com.codder.ultimate.R;
import com.codder.ultimate.post.activity.FeedListActivity;
import com.codder.ultimate.adapter.FeedGridAdapter;
import com.codder.ultimate.databinding.FragmentGuestUserPostsBinding;
import com.codder.ultimate.fragments.BaseFragment;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.post.model.PostRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class GuestUserPostsFragment extends BaseFragment {


    private static final String TAG = "GuestUserPostsFragment";
    FragmentGuestUserPostsBinding binding;
    FeedGridAdapter feedGridAdapter;
    private GuestProfileRoot.User user;
    private int start = 0;
    MyLoader myLoader = new MyLoader();

    public static GuestUserPostsFragment newInstance(GuestProfileRoot.User user) {
        GuestUserPostsFragment fragment = new GuestUserPostsFragment();
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
                user = new Gson().fromJson(userJson, GuestProfileRoot.User.class);
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_guest_user_posts, container, false);
        binding.setLoader(myLoader);
        initMain();
        getData(false);
        return binding.getRoot();
    }

    private void getData(boolean isLoadMore) {
        if (user == null || user.getUserId() == null) {
            Log.e(TAG, "User or userId is null. Skipping data load.");
            return;
        }

        if (isLoadMore) {
            start += Const.LIMIT;
        } else {
            start = 0;
            myLoader.isFirstTimeLoading.set(true);
            feedGridAdapter.submitList(new ArrayList<>());
        }

        myLoader.noData.set(false);
        Call<PostRoot> call = RetrofitBuilder.create().getUserPostList(user.getUserId(), start, Const.LIMIT);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PostRoot> call, Response<PostRoot> response) {
                myLoader.isFirstTimeLoading.set(false);
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();
                if (response.isSuccessful() && response.body() != null) {
                    PostRoot result = response.body();

                    if (result.isStatus() && result.getPost() != null && !result.getPost().isEmpty()) {
                        List<PostRoot.PostItem> postList = result.getPost();
                        feedGridAdapter.submitList(postList);
                        Log.d(TAG, "Posts loaded: " + postList.size());
                    } else {
                        if (start == 0) {
                            myLoader.noData.set(true);
                            Log.i(TAG, "No posts available for this user.");
                            feedGridAdapter.submitList(new ArrayList<>());
                        }
                    }
                } else {
                    Log.e(TAG, "Response error: HTTP " + response.code() + " - " + response.message());
                    if (start == 0) {
                        myLoader.noData.set(true);
                        feedGridAdapter.submitList(new ArrayList<>());
                    }
                }
            }

            @Override
            public void onFailure(Call<PostRoot> call, Throwable t) {
                myLoader.isFirstTimeLoading.set(false);
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();

                Log.e(TAG, "Network failure: " + t.getLocalizedMessage(), t);
                if (start == 0) {
                    myLoader.noData.set(true);
                    feedGridAdapter.submitList(new ArrayList<>());
                }
            }
        });
    }

    private void initMain() {
        feedGridAdapter = new FeedGridAdapter(requireContext());

        binding.rvFeed.setAdapter(feedGridAdapter);

        binding.swipeRefresh.setOnRefreshListener((refreshLayout) -> getData(false));
        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> getData(true));


        feedGridAdapter.setOnFeedGridAdapterClickListener(new FeedGridAdapter.OnFeedGridAdapterClickListener() {
            @Override
            public void onFeedClick(int position) {
                startActivity(new Intent(getActivity(), FeedListActivity.class)
                        .putExtra(Const.USERID, user.getId())
                        .putExtra(Const.POSITION, position).putExtra(Const.DATA, new Gson().toJson(feedGridAdapter.getCurrentList())));
            }

            @Override
            public void onDeleteClick(PostRoot.PostItem postItem, int position) {
                if (postItem.getUserId().equalsIgnoreCase(sessionManager.getUser().getId())) {
                    new PopupBuilder(requireContext()).deletePopup(getString(R.string.delete_post_confirmation), new PopupBuilder.OnMultiButtonPopupLister() {
                        @Override
                        public void onClickContinue() {
                            deletePost(postItem, position);
                        }

                        @Override
                        public void onClickCancel() {

                        }
                    });
                }
            }
        });
    }

    public void deletePost(PostRoot.PostItem postItem, int pos) {
        if (postItem == null || postItem.getId() == null) {
            Log.e(TAG, "Post item or ID is null. Cannot delete.");
            return;
        }

        Call<RestResponse> call = RetrofitBuilder.create().deletePost(postItem.getId());

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Context context = getContext();
                    if (context != null) {
                        Toast.makeText(context, getString(R.string.deleted_successfully), Toast.LENGTH_SHORT).show();
                    }

                    List<PostRoot.PostItem> currentList = new ArrayList<>(feedGridAdapter.getCurrentList());
                    if (pos >= 0 && pos < currentList.size()) {
                        currentList.remove(pos);
                        feedGridAdapter.submitList(currentList);
                    } else {
                        Log.w(TAG, "Invalid post position: " + pos);
                    }
                } else {
                    Log.e(TAG, getString(R.string.delete_failed) + (response.body() != null ? response.body().getMessage() : getString(R.string.unknown_error)));
                    Toast.makeText(getContext(), getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {
                Log.e(TAG, "Delete post API failed: " + t.getLocalizedMessage(), t);
                Toast.makeText(getContext(), getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
            }
        });
    }

}