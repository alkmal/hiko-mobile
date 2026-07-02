package com.codder.ultimate.post.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.databinding.FragmentFeedListBinding;
import com.codder.ultimate.databinding.ItemFeedBinding;
import com.codder.ultimate.databinding.ItemSearchUsersBinding;
import com.codder.ultimate.fragments.BaseFragment;
import com.codder.ultimate.guestuser.activity.GuestActivity;
import com.codder.ultimate.guestuser.activity.SearchActivity;
import com.codder.ultimate.guestuser.adapter.SearchUserAdapter;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.post.BottomSheetCommentLikeList;
import com.codder.ultimate.post.adapter.FeedAdapter;
import com.codder.ultimate.post.model.PostRoot;
import com.codder.ultimate.retrofit.CommentApiCalling;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.UserApiCall;
import com.codder.ultimate.viewModel.FeedListViewModel;
import com.codder.ultimate.viewModel.ViewModelFactory;
import com.google.gson.Gson;

public class FeedListFragment extends BaseFragment implements FeedAdapter.OnPostClickListener {

    private static final String TAG = "FeedListFragment";
    private static final String ARG_TYPE = "arg_type";
    private FragmentFeedListBinding binding;
    private FeedListViewModel viewModel;
    private CommentApiCalling commentApiCalling;

    protected UserApiCall userApiCall;
    private String type = "Popular";
    private boolean isBottomSheetOpen = false;

    public static FeedListFragment newInstance(String type) {
        FeedListFragment fragment = new FeedListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_feed_list, container, false);

        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE, "Popular");
        }

        commentApiCalling = new CommentApiCalling(requireActivity());
        userApiCall = new UserApiCall(requireActivity());
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new FeedListViewModel()).createFor()).get(FeedListViewModel.class);
        viewModel.init(requireActivity(), type);
        binding.setViewModel(viewModel);
        viewModel.feedAdapter.setOnPostClickListener(this);
        viewModel.getPostData(false, sessionManager.getUser().getId());

        initListener();
        return binding.getRoot();
    }

    private void initListener() {
        binding.swipeRefresh.setOnRefreshListener(refreshLayout -> viewModel.getPostData(false, sessionManager.getUser().getId()));
        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> {
            viewModel.getPostData(true, sessionManager.getUser().getId());
        });
        viewModel.isLoadCompleted.observe(requireActivity(), aBoolean -> {
            binding.swipeRefresh.finishRefresh();
            binding.swipeRefresh.finishLoadMore();
            viewModel.isFirstTimeLoading.set(false);
            viewModel.isLoadMoreLoading.set(false);
        });


      viewModel.feedAdapter.setOnUserClickListener(new FeedAdapter.OnFollowClickListener() {
          @Override
          public void onFollowClick(@NonNull PostRoot.PostItem user, @NonNull ItemFeedBinding binding, int position) {
              if (user.getUserId() == null) return;

              binding.pd.setVisibility(View.VISIBLE);
              binding.tvFollow.setVisibility(View.GONE);

              userApiCall.followUnfollowUser(!user.isFollow(), user.getUserId(), "", new UserApiCall.OnFollowUnfollowListener() {
                  @Override
                  public void onFollowSuccess() {
                      user.setFollow(true);
                      viewModel.feedAdapter.notifyItemChanged(position, user);
                      binding.tvFollow.setVisibility(View.VISIBLE);
                      Toast.makeText(requireActivity(), "Followed Successfully!!", Toast.LENGTH_SHORT).show();
                  }

                  @Override
                  public void onUnfollowSuccess() {
                      user.setFollow(false);
                      viewModel.feedAdapter.notifyItemChanged(position, user);
                      binding.tvFollow.setVisibility(View.VISIBLE);
                  }

                  @Override
                  public void onFail() {
                      toggleFollowUI(binding);
                  }
              });
          }
      });

    }

    private void toggleFollowUI(ItemFeedBinding binding) {
        binding.tvFollow.setVisibility(View.VISIBLE);
        binding.pd.setVisibility(View.GONE);
    }

    @Override
    public void onLikeClick(PostRoot.PostItem postDummy, int position, ItemFeedBinding binding) {
        if (postDummy.isLikeUpdating()) return;

        postDummy.setLikeUpdating(true);

        boolean currentLikeStatus = postDummy.isUserLiked();
        int currentLikeCount = postDummy.getLikeCount();

        boolean newLikeStatus = !currentLikeStatus;
        int newLikeCount = currentLikeStatus ? currentLikeCount - 1 : currentLikeCount + 1;

        postDummy.setUserLiked(newLikeStatus);
        postDummy.setLikeCount(newLikeCount);
        binding.tvLikes.setText(String.valueOf(newLikeCount));
        viewModel.feedAdapter.notifyItemChanged(position, postDummy);

        commentApiCalling.toggleLikePost(postDummy.getId(), isLikedFromServer -> {
            // Fix: compare server response with original state, not optimistic
            postDummy.setUserLiked(isLikedFromServer);

            // Adjust like count properly
            int adjustedCount = currentLikeCount + (isLikedFromServer ? 1 : -1);
            postDummy.setLikeCount(adjustedCount);
            binding.tvLikes.setText(String.valueOf(adjustedCount));

            postDummy.setLikeUpdating(false);
            viewModel.feedAdapter.notifyItemChanged(position, postDummy);
        });
    }

    @Override
    public void onCommentListClick(PostRoot.PostItem postDummy) {
        BottomSheetCommentLikeList.newInstance(
                new Gson().toJson(postDummy),
                null,
                BottomSheetCommentLikeList.COMMENTS,
                "Feed"
        ).show(requireActivity().getSupportFragmentManager(), "PostCommentBottomSheet");
    }

    @Override
    public void onLikeListClick(PostRoot.PostItem postDummy) {
        BottomSheetCommentLikeList.newInstance(
                new Gson().toJson(postDummy),
                null,
                BottomSheetCommentLikeList.LIKES,
                "Feed"
        ).show(requireActivity().getSupportFragmentManager(), "PostLikeBottomSheet");
    }

    @Override
    public void onShareClick(PostRoot.PostItem postDummy) {

        if (isBottomSheetOpen) return;
        isBottomSheetOpen = true;


        String deepLink = BuildConfig.BASE_URL + "open?type=POST&userId=" + postDummy.getUserId() + "&postId=" + postDummy.getId();

        Log.d(TAG, "onShareClick: ==" + deepLink);

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, deepLink);
            getActivity().startActivity(Intent.createChooser(shareIntent, "Share"));
        } catch (Exception e) {
            Log.e(TAG, "Share error: " + e.getMessage());
        }

        isBottomSheetOpen = false;

    }

    @Override
    public void onMentionClick(String userName) {
        startActivity(new Intent(getActivity(), GuestActivity.class).putExtra(Const.USERNAME, userName));
    }

}
