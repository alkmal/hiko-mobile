package com.codder.ultimate.live.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.databinding.ActivityLocationChooseBinding;
import com.codder.ultimate.live.adapter.LocationAdapter;
import com.codder.ultimate.live.model.SearchLocationRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationChooseActivity extends BaseActivity {

    public static final String TAG = "LocationChooseActivity";
    private static final int DEBOUNCE_DELAY = 900;
    public static final int REQ_CODE_LOCATION = 123;

    private ActivityLocationChooseBinding binding;
    private LocationAdapter locationAdapter = new LocationAdapter();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private String keyword = "Surat";
    private Call<SearchLocationRoot> call;
    private int start = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_choose);
        initializeLocationInput();

        binding.rvLocation.setAdapter(locationAdapter);
        locationAdapter.setOnLocationClickListener(selectedLocation -> {
            Intent intent = new Intent();
            intent.putExtra(Const.DATA, new Gson().toJson(selectedLocation));
            setResult(RESULT_OK, intent);
            finish();
        });

        binding.btnDone.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Const.DATA, binding.etLocation.getText().toString().trim());
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        binding.etLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    keyword = s.toString().trim();
                    if (!keyword.isEmpty()) {
                        searchLocation();
                    }
                };

                handler.postDelayed(searchRunnable, DEBOUNCE_DELAY);

                binding.btnDone.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void initializeLocationInput() {
        Intent intent = getIntent();
        String location = intent.getStringExtra(Const.DATA);

        if (location != null && !location.isEmpty()) {
            binding.etLocation.setText(location);
        } else {
            String currentCity = sessionManager.getStringValue(Const.CURRENT_CITY);
            if (!currentCity.isEmpty()) {
                binding.etLocation.setText(currentCity);
            } else {
                binding.etLocation.setText(sessionManager.getStringValue(Const.COUNTRY));
            }
        }

        keyword = binding.etLocation.getText().toString().trim();
        if (!keyword.isEmpty()) {
            searchLocation(); // Only search if something is in the input
        }
    }


    private void searchLocation() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }

        if (keyword.isEmpty()) {
            binding.layoutNoData.setVisibility(View.VISIBLE);
            return;
        }

        customDialogClass.show();
        binding.layoutNoData.setVisibility(View.GONE);

        call = RetrofitBuilder.getLocation().searchLocation(
                sessionManager.getSetting().getLocationApiKey(), keyword);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<SearchLocationRoot> call, Response<SearchLocationRoot> response) {
                customDialogClass.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    handleLocationResponse(response.body());
                } else {
                    Log.e(TAG, "Location API response unsuccessful or empty.");
                    binding.layoutNoData.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<SearchLocationRoot> call, Throwable t) {
                customDialogClass.dismiss();
                if (!call.isCanceled()) {
                    Log.e(TAG, "API failure: ", t);
                }
            }
        });
    }

    private void handleLocationResponse(SearchLocationRoot searchLocationRoot) {
        if (searchLocationRoot != null && searchLocationRoot.getData() != null && !searchLocationRoot.getData().isEmpty()) {
            locationAdapter.submitList(searchLocationRoot.getData());
        } else {
            if (start == 0) {
                binding.layoutNoData.setVisibility(View.VISIBLE);
            } else {
                binding.layoutNoData.setVisibility(View.GONE);
            }
        }
    }
}
