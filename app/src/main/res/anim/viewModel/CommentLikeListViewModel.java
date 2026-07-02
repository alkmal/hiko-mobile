package com.example.rayzi.viewModel;

import android.content.Context;
import android.util.Log;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.rayzi.SessionManager;
import com.example.rayzi.comments.CommentAdapter;
import com.example.rayzi.modelclass.PostCommentRoot;
import com.example.rayzi.modelclass.RestResponse;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.example.rayzi.user.SearchUserAdapter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

package com.example.rayzi.viewModel;

import android.content.Context;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.rayzi.SessionManager;
import com.example.rayzi.comments.CommentAdapter;
import com.example.rayzi.modelclass.PostCommentRoot;
import com.example.rayzi.modelclass.RestResponse;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentLikeListViewModel extends ViewModel {

    public static final int POST = 0;
    public static final int RELITE = 1;

    private final ObservableBoolean noData = new ObservableBoolean(false);
    private final ObservableBoolean isLoading = new ObservableBoolean(false);
    private final MutableLiveData<Integer> commentCount = new MutableLiveData<>();

    private final CommentAdapter commentAdapter;
    private SessionManager sessionManager;
    private int start = 0;

    public CommentLikeListViewModel(Context context) {
        this.sessionManager = new SessionManager(context);
        this.commentAdapter = new CommentAdapter(context);
    }

    public ObservableBoolean getNoData() {
        return noData;
    }

    public ObservableBoolean getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<Integer> getCommentCount() {
        return commentCount;
    }

    public CommentAdapter getCommentAdapter() {
        return commentAdapter;
    }

    public void fetchCommentsOrLikes(String postId, int type, boolean isComment, boolean isLoadMore) {
        if (!isLoadMore) {
            start = 0;
            commentCount.setValue(0);
        }

        Call<PostCommentRoot> call = isComment ?
                getCommentApi(type, postId) :
                getLikeApi(type, postId);

        isLoading.set(true);
        noData.set(false);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PostCommentRoot> call, Response<PostCommentRoot> response) {
                isLoading.set(false);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<PostCommentRoot.CommentsItem> data = response.body().getData();
                    if (data != null && !data.isEmpty()) {
                        commentAdapter.addComments(data);
                        start += Const.LIMIT;
                        commentCount.setValue(commentAdapter.getItemCount());
                    } else if (start == 0) {
                        noData.set(true);
                    }
                } else if (start == 0) {
                    noData.set(true);
                }
            }

            @Override
            public void onFailure(Call<PostCommentRoot> call, Throwable t) {
                isLoading.set(false);
            }
        });
    }

    public void deleteComment(PostCommentRoot.CommentsItem comment, int position) {
        if (comment == null || comment.getId() == null) return;

        isLoading.set(true);
        RetrofitBuilder.create().deleteComment(comment.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                isLoading.set(false);
                if (response.isSuccessful() && response.body().isStatus()) {
                    commentAdapter.removeComment(position);
                }
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {
                isLoading.set(false);
            }
        });
    }

    private Call<PostCommentRoot> getCommentApi(int type, String postId) {
        return type == POST ?
                RetrofitBuilder.create().getPostCommentList(sessionManager.getUserId(), postId, start, Const.LIMIT) :
                RetrofitBuilder.create().getReliteCommentList(sessionManager.getUserId(), postId, start, Const.LIMIT);
    }

    private Call<PostCommentRoot> getLikeApi(int type, String postId) {
        return type == POST ?
                RetrofitBuilder.create().getPostLikeList(sessionManager.getUserId(), postId, start, Const.LIMIT) :
                RetrofitBuilder.create().getReliteLikeList(sessionManager.getUserId(), postId, start, Const.LIMIT);
    }
}

