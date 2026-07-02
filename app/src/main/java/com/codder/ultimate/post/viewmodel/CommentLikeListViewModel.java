package com.codder.ultimate.post.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.SessionManager;
import com.codder.ultimate.dialog.CustomDialogClass;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.post.adapter.CommentAdapter;
import com.codder.ultimate.post.model.PostCommentRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentLikeListViewModel extends ViewModel {

    public static final int POST = 0;
    public static final int RELITE = 1;
    private static final String TAG = "CommentLikeListViewModel";

    private final Context context;
    private final SessionManager sessionManager;

    private final CommentAdapter commentAdapter = new CommentAdapter();
    private final MutableLiveData<Boolean> noData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> listCountFinal = new MutableLiveData<>(0);

    public CommentLikeListViewModel(Context context) {
        this.context = context.getApplicationContext();
        this.sessionManager = new SessionManager(this.context);
    }

    public CommentAdapter getCommentAdapter() {
        return commentAdapter;
    }

    public LiveData<Boolean> getNoData() {
        return noData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<Integer> getListCountFinal() {
        return listCountFinal;
    }

    public void setNoData(boolean value) {
        noData.setValue(value);
    }


    public void fetchCommentsOrLikes(String id, int contentType, boolean isComment, boolean isRefresh) {
        if (!isRefresh) isLoading.setValue(true);
        noData.setValue(false);

        Call<PostCommentRoot> call = isComment
                ? (contentType == POST
                ? RetrofitBuilder.create().getPostCommentList(sessionManager.getUser().getId(), id, 0, Const.LIMIT)
                : RetrofitBuilder.create().getReliteCommentList(sessionManager.getUser().getId(), id, 0, Const.LIMIT))
                : (contentType == POST
                ? RetrofitBuilder.create().getPostLikeList(sessionManager.getUser().getId(), id, 0, Const.LIMIT)
                : RetrofitBuilder.create().getReliteLikeList(sessionManager.getUser().getId(), id, 0, Const.LIMIT));

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<PostCommentRoot> call, Response<PostCommentRoot> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<PostCommentRoot.CommentsItem> items = response.body().getData();
                    commentAdapter.submitList(new ArrayList<>(items));
                    listCountFinal.postValue(items.size());
                    noData.postValue(items.isEmpty());

                    Log.d(TAG, "Fetched " + items.size() + " " + (isComment ? Const.COMMENTS : Const.LIKES));

                } else {
                    noData.postValue(true);
                    Log.w(TAG, "Failed to fetch " + (isComment ? Const.COMMENTS : Const.LIKES) + ": " + (response.body() != null ? response.body().getMessage() : "No data"));
                }
            }

            @Override
            public void onFailure(Call<PostCommentRoot> call, Throwable t) {
                isLoading.setValue(false);
                noData.postValue(true);
                Log.e(TAG, "Failed to fetch " + (isComment ? Const.COMMENTS : Const.LIKES), t);
            }
        });
    }

    public void addCommentLocally(PostCommentRoot.CommentsItem comment) {
        List<PostCommentRoot.CommentsItem> current = new ArrayList<>(commentAdapter.getCurrentList());
        current.add(0, comment);
        commentAdapter.submitList(current);
        listCountFinal.postValue(current.size());
        noData.postValue(false);
    }

    public void deleteComment(PostCommentRoot.CommentsItem comment, int position, CustomDialogClass customDialogClass) {
        if (comment == null || comment.getId() == null) {
            Log.e(TAG, "deleteComment: Invalid comment data.");
            return;
        }
        customDialogClass.show();
        Call<RestResponse> call = RetrofitBuilder.create().deleteComment(comment.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                customDialogClass.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<PostCommentRoot.CommentsItem> current = new ArrayList<>(commentAdapter.getCurrentList());
                    current.remove(position);
                    commentAdapter.submitList(current);
                    listCountFinal.postValue(current.size());
                    noData.postValue(current.isEmpty());
                    Log.d(TAG, "deleteComment: Comment deleted successfully.");
                } else {
                    Log.w(TAG, "deleteComment: Failed to delete comment.");
                }
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable t) {
                customDialogClass.dismiss();
                Log.e(TAG, "deleteComment: Network failure - " + t.getLocalizedMessage(), t);

            }
        });
    }
}
