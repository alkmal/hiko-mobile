package com.codder.ultimate.profile.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.R;
import com.codder.ultimate.RayziUtils;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.profile.adapter.LevelsAdapter;
import com.codder.ultimate.databinding.ActivityMyLevelListBinding;
import com.codder.ultimate.profile.modelclass.LevelRoot;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyLevelListActivity extends BaseActivity {

    public static final String TAG = "MyLevelListActivity";
    private ActivityMyLevelListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_level_list);

        setupUI();
        fetchLevels();
    }

    private void setupUI() {
        UserRoot.User currentUser = sessionManager.getUser();
        if (currentUser != null && currentUser.getLevel() != null) {
            Glide.with(this)
                    .load(BuildConfig.BASE_URL + currentUser.getLevel().getImage())
                    .placeholder(R.drawable.placeholder)  // optional fallback
                    .into(binding.myLevelImage);

            binding.tvMyLevel.setText(String.valueOf(currentUser.getLevel().getName()));

            String formattedCoins = RayziUtils.formatCoin(currentUser.getSpentCoin());
            String coinsText = String.format(getString(R.string.you_ve_spent_s), formattedCoins + Const.CoinName);
            binding.tvSpentCoin.setText(coinsText);

        } else {
            binding.tvMyLevel.setText(getString(R.string.no_level_available));
            binding.tvSpentCoin.setText("");
        }

        binding.rvFeed.setLayoutManager(new LinearLayoutManager(this));
    }

    private void fetchLevels() {
        showLoading(true);

        RetrofitBuilder.create().getLevels().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LevelRoot> call, @NonNull Response<LevelRoot> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LevelRoot levelRoot = response.body();

                    if (levelRoot.isStatus() && levelRoot.getLevel() != null && !levelRoot.getLevel().isEmpty()) {
                        LevelsAdapter adapter = new LevelsAdapter(levelRoot.getLevel());
                        binding.rvFeed.setAdapter(adapter);
                        binding.rvFeed.setVisibility(View.VISIBLE);
                    } else {
                        showErrorState("No levels available");
                    }
                } else {
                    showErrorState("Failed to load data");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LevelRoot> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error fetching levels", t);
                showErrorState("Something went wrong: " + t.getLocalizedMessage());
            }
        });
    }

    private void showLoading(boolean loading) {
        if (loading) {
            binding.shimmer.setVisibility(View.VISIBLE);
            binding.shimmer.startShimmer();

            binding.rvFeed.setVisibility(View.GONE);
        } else {
            binding.shimmer.stopShimmer();
            binding.shimmer.setVisibility(View.GONE);

            binding.rvFeed.setVisibility(View.VISIBLE);
        }
    }

    private void showErrorState(String message) {
        binding.shimmer.stopShimmer();
        binding.shimmer.setVisibility(View.GONE);
        binding.rvFeed.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
