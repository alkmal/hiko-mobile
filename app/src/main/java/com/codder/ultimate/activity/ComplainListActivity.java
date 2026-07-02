package com.codder.ultimate.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.MyLoader;
import com.codder.ultimate.R;
import com.codder.ultimate.adapter.TicketAdapter;
import com.codder.ultimate.databinding.ActivityComplainListBinding;
import com.codder.ultimate.modelclass.ComplainRoot;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComplainListActivity extends BaseActivity {

    public static final String TAG = "ComplainListActivity";
    ActivityComplainListBinding binding;
    MyLoader myLoader = new MyLoader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_complain_list);
        binding.setLoader(myLoader);

        getData();
        initListener();
    }

    private void initListener() {
        binding.swipeRefresh.setOnRefreshListener(refreshLayout -> {
            myLoader.isFirstTimeLoading.set(false);
            binding.shimmer.stopShimmer();
            binding.shimmer.setVisibility(View.GONE);

            getData();
        });
    }

    private void getData() {
        binding.layoutNoData.setVisibility(View.GONE);

        if (!binding.swipeRefresh.isRefreshing()) {

            myLoader.isFirstTimeLoading.set(true);
            binding.shimmer.setVisibility(View.VISIBLE);
            binding.shimmer.startShimmer();
        }

        Call<ComplainRoot> call = RetrofitBuilder.create().getComplains(sessionManager.getUser().getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ComplainRoot> call, Response<ComplainRoot> response) {
                myLoader.isFirstTimeLoading.set(false);
                binding.shimmer.stopShimmer();
                binding.shimmer.setVisibility(View.GONE);

                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.finishRefresh();
                }

                if (response.isSuccessful() && response.body() != null) {
                    ComplainRoot result = response.body();

                    if (result.isStatus() && result.getComplain() != null && !result.getComplain().isEmpty()) {
                        binding.rvMyTickets.setAdapter(new TicketAdapter(result.getComplain()));
                        binding.layoutNoData.setVisibility(View.GONE);
                    } else {
                        binding.layoutNoData.setVisibility(View.VISIBLE);
                        Log.w(TAG, "No complaints available or empty response.");
                    }
                } else {
                    binding.layoutNoData.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Failed to fetch complaints. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ComplainRoot> call, Throwable t) {
                myLoader.isFirstTimeLoading.set(false);
                binding.shimmer.stopShimmer();
                binding.shimmer.setVisibility(View.GONE);

                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.finishRefresh();
                }

                binding.layoutNoData.setVisibility(View.VISIBLE);
                Log.e(TAG, "Failed to fetch complaints due to network error: " + t.getMessage(), t);
                Toast.makeText(ComplainListActivity.this, getString(R.string.something_went_wrong_text), Toast.LENGTH_SHORT).show();

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (myLoader.isFirstTimeLoading.get()) {
            binding.shimmer.startShimmer();
        }
    }

    @Override
    protected void onPause() {
        binding.shimmer.stopShimmer();
        super.onPause();
    }

}