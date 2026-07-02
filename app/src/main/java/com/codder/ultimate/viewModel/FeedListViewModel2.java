package com.codder.ultimate.viewModel;

import android.content.Context;
import android.util.Log;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.post.adapter.FeedAdapter;
import com.codder.ultimate.post.model.PostRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedListViewModel2 extends ViewModel {
    public int start = 0;
    private Context context;
    public FeedAdapter feedAdapter;
    private String type = Const.TYPE_POPULAR;
    public final ObservableBoolean isFirstTimeLoading = new ObservableBoolean(true);
    public final ObservableBoolean isLoadMoreLoading = new ObservableBoolean(false);
    public final ObservableBoolean noData = new ObservableBoolean(false);
    public final MutableLiveData<Boolean> isLoadCompleted = new MutableLiveData<>();

    public void init(Context context, String type) {
        this.context = context.getApplicationContext();

        this.context = context.getApplicationContext();
        if (type != null && !type.isEmpty()) {
            this.type = type;
        }

        if (feedAdapter == null) {
            feedAdapter = new FeedAdapter(context);
        }
    }

    public void getPostData(boolean isLoadMore, String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.e("FeedListViewModel", "User ID is null or empty");
            isLoadCompleted.postValue(true);
            return;
        }

        if (isLoadMore) {
            start += Const.LIMIT;
            isLoadMoreLoading.set(true);
        } else {
            start = 0;
            feedAdapter.submitList(null);
            isFirstTimeLoading.set(true);
        }

        noData.set(false);

        Call<PostRoot> call;
        switch (type) {
            case Const.TYPE_FOLLOWING:
                call = RetrofitBuilder.create().getFollowingPost(userId, type, start, Const.LIMIT);
                break;
            case Const.USER:
                call = RetrofitBuilder.create().getUserPostList(userId, start, Const.LIMIT);
                break;
            default:
                call = RetrofitBuilder.create().getPostList(userId, type, start, Const.LIMIT);
                break;
        }

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PostRoot> call, Response<PostRoot> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<PostRoot.PostItem> posts = response.body().getPost();
                    if (posts != null && !posts.isEmpty()) {
                        if (isLoadMore) {
                            List<PostRoot.PostItem> currentList = new ArrayList<>(feedAdapter.getCurrentList());

                            HashSet<String> seenIds = new HashSet<>();
                            for (PostRoot.PostItem item : currentList) {
                                seenIds.add(item.getId());
                            }

                            for (PostRoot.PostItem item : posts) {
                                if (!seenIds.contains(item.getId())) {
                                    currentList.add(item);
                                    seenIds.add(item.getId());
                                }
                            }

                            feedAdapter.submitList(currentList);
                        }
                        else {
                            feedAdapter.submitList(new ArrayList<>(posts));
                        }
                    } else if (start == 0) {
                        noData.set(true);
                        feedAdapter.submitList(new ArrayList<>());
                    }
                } else if (start == 0) {
                    noData.set(true);
                    feedAdapter.submitList(new ArrayList<>());
                }

                isLoadMoreLoading.set(false);
                isFirstTimeLoading.set(false);
                isLoadCompleted.postValue(true);
            }

            @Override
            public void onFailure(Call<PostRoot> call, Throwable t) {
                Log.e("FeedListViewModel", "Failed to load posts", t);
                isLoadMoreLoading.set(false);
                isFirstTimeLoading.set(false);
                isLoadCompleted.postValue(true);
            }
        });
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        feedAdapter.submitList(null);
    }
}
