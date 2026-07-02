package com.codder.ultimate.profile.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.MyLoader;
import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityVideoListGridBinding;
import com.codder.ultimate.dialog.CustomDialogClass;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.reels.activity.ReelsActivity;
import com.codder.ultimate.reels.adapter.ProfileVideoGridAdapter;
import com.codder.ultimate.reels.model.ReliteRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyRelitesActivity extends BaseActivity {

    public static final String TAG = "MyRelitesActivity";
    private ActivityVideoListGridBinding binding;
    private final ProfileVideoGridAdapter adapter = new ProfileVideoGridAdapter();
    private String userId;
    private int start = 0;
    private final MyLoader myLoader = new MyLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupUI();
        initialize();
        handleIntent();
        setupListeners();
    }

    private void setupUI() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_list_grid);
        binding.setLoader(myLoader);
    }

    private void initialize() {
        sessionManager = new SessionManager(this);
        customDialogClass = new CustomDialogClass(this, R.style.customStyle);
        customDialogClass.setCancelable(false);
        customDialogClass.setCanceledOnTouchOutside(false);

        binding.rvFeed.setAdapter(adapter);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        userId = intent != null ? intent.getStringExtra(Const.DATA) : null;

        if (userId != null && !userId.trim().isEmpty()) {
            getData(false);
        }

    }

    private void setupListeners() {
        binding.swipeRefresh.setOnRefreshListener(refreshLayout -> getData(false));
        binding.swipeRefresh.setOnLoadMoreListener(refreshLayout -> getData(true));

        adapter.setOnVideoGridClickListener(new ProfileVideoGridAdapter.OnVideoGridClickListener() {
            @Override
            public void onVideoClick(int position) {
                startActivity(new Intent(MyRelitesActivity.this, ReelsActivity.class)
                        .putExtra(Const.POSITION, position)
                        .putExtra(Const.DATA, new Gson().toJson(adapter.getCurrentItems())));
            }

            @Override
            public void onDeleteClick(@NonNull ReliteRoot.VideoItem postItem, int position) {
                showDeleteConfirmation(postItem, position);
            }
        });
    }

    private void showDeleteConfirmation(@NonNull ReliteRoot.VideoItem videoItem, int position) {
        if (videoItem.getId() == null) return;

        new PopupBuilder(MyRelitesActivity.this).deletePopup(getString(R.string.delete_relite_confirmation), new PopupBuilder.OnMultiButtonPopupLister() {
            @Override
            public void onClickContinue() {
                deleteVideo(videoItem, position);
            }

            @Override
            public void onClickCancel() {

            }
        });
    }

    private void deleteVideo(@NonNull ReliteRoot.VideoItem postItem, int pos) {
        if (postItem == null || postItem.getId() == null) {
            Log.e(TAG, "deleteVideo: Invalid video item or ID.");
            Toast.makeText(MyRelitesActivity.this, getString(R.string.invalid_video), Toast.LENGTH_SHORT).show();
            return;
        }

        customDialogClass.show();

        Call<RestResponse> call = RetrofitBuilder.create().deleteRelite(postItem.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<RestResponse> call, @NonNull Response<RestResponse> response) {
                customDialogClass.dismiss();
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(MyRelitesActivity.this, getString(R.string.deleted_successfully), Toast.LENGTH_SHORT).show();

                    List<ReliteRoot.VideoItem> updatedList = new ArrayList<>(adapter.getCurrentItems());
                    if (pos >= 0 && pos < updatedList.size()) {
                        updatedList.remove(pos);
                        adapter.submitList(updatedList);
                        Log.d(TAG, "deleteVideo: Video deleted successfully at position " + pos);
                    } else {
                        Log.w(TAG, "deleteVideo: Invalid position or list out of bounds.");
                    }
                } else {
                    Toast.makeText(MyRelitesActivity.this, getString(R.string.failed_to_delete_video), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "deleteVideo: API call failed or no status.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<RestResponse> call, @NonNull Throwable t) {
                customDialogClass.dismiss();
                Toast.makeText(MyRelitesActivity.this, getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "deleteVideo: Network failure", t);
            }
        });
    }

    private void getData(boolean isLoadMore) {
        if (isLoadMore) {
            start += Const.LIMIT;
        } else {
            start = 0;
            myLoader.isFirstTimeLoading.set(true);
            adapter.submitList(new ArrayList<>());
        }

        myLoader.noData.set(false);

        Call<ReliteRoot> call = RetrofitBuilder.create().getRelites(SessionManager.getUserId(this), "User", start, Const.LIMIT);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ReliteRoot> call, @NonNull Response<ReliteRoot> response) {
                stopLoadingIndicators();

                if (response.isSuccessful() && response.body() != null) {
                    ReliteRoot result = response.body();

                    if (result.isStatus()) {
                        if (result.getVideo() != null && !result.getVideo().isEmpty()) {
                            List<ReliteRoot.VideoItem> newList = result.getVideo();
                            adapter.submitList(newList);
                            Log.d(TAG, "Loaded " + newList.size() + " videos.");
                        } else if (start == 0) {
                            myLoader.noData.set(true);
                            Log.d(TAG, "No videos available.");
                        }
                    } else {
                        Log.w(TAG, "Failed to load relites: " + (result.getMessage() != null ? result.getMessage() : "Unknown error"));
                        Toast.makeText(MyRelitesActivity.this, getString(R.string.failed_to_load_videos), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Error response: Code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(MyRelitesActivity.this, getString(R.string.failed_to_load_videos), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReliteRoot> call, @NonNull Throwable t) {
                stopLoadingIndicators();
                Log.e(TAG, "Error loading relites: " + t.getMessage(), t);
                Toast.makeText(MyRelitesActivity.this, getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void stopLoadingIndicators() {
        myLoader.isFirstTimeLoading.set(false);
        binding.swipeRefresh.finishRefresh();
        binding.swipeRefresh.finishLoadMore();
    }
}
