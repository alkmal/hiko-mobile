package com.codder.ultimate.guestuser.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.lifecycle.ViewModelProvider;

import com.codder.ultimate.R;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.adapter.SearchHistoryAdapter;
import com.codder.ultimate.databinding.ActivitySearchBinding;
import com.codder.ultimate.databinding.ItemSearchHistoryBinding;
import com.codder.ultimate.databinding.ItemSearchUsersBinding;
import com.codder.ultimate.guestuser.adapter.SearchUserAdapter;
import com.codder.ultimate.guestuser.model.SearchViewModel;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.modelclass.SearchHistoryRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.codder.ultimate.retrofit.RetrofitService;
import com.codder.ultimate.retrofit.UserApiCall;
import com.codder.ultimate.viewModel.ViewModelFactory;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends BaseActivity {

    private ActivitySearchBinding binding;
    private SearchViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        viewModel = new ViewModelProvider(this, new ViewModelFactory(new SearchViewModel(sessionManager))).get(SearchViewModel.class);
        binding.setViewModel(viewModel);
        binding.rvMessage.setAdapter(viewModel.searchUserAdapter);
        binding.rvSearch.setAdapter(viewModel.searchHistoryAdapter);

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.historyList.observe(this, list -> {
            if (list == null || list.isEmpty()) {
                recentSearch(false);
            } else {
                recentSearch(true);
            }
        });

        showSearchHistory();  // default normal state
        viewModel.getHistory(false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        doTransition(Const.UP_TO_BOTTOM);
    }

    private void setupListeners() {

        binding.swipeRefresh.setOnRefreshListener(refresh -> viewModel.getUsers(false));
        binding.swipeRefresh.setOnLoadMoreListener(load -> viewModel.getUsers(true));

        binding.swipeRefresh2.setOnRefreshListener(refresh -> viewModel.getHistory(false));
        binding.swipeRefresh2.setOnLoadMoreListener(load -> viewModel.getHistory(true));

        binding.tvClearAll.setOnClickListener(view -> {
            Call<RestResponse> call = RetrofitBuilder.create().deleteHistory(sessionManager.getUser().getId(),"");
            call.enqueue(new Callback<RestResponse>() {
                @Override
                public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                    Log.d(TAG, "onResponse: ==== delete all");
                    viewModel.searchHistoryAdapter.submitList(new ArrayList<>());
                    // show no data UI (if used)
                    viewModel.noHistoryData.set(true);
                    recentSearch(false);
                }

                @Override
                public void onFailure(Call<RestResponse> call, Throwable throwable) {
                    Log.d(TAG, "onFailure: =====" + throwable.getLocalizedMessage());
                }
            });
        });


        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.onKeywordChanged(s.toString());
                viewModel.keyword = s.toString();
            }
        });

        viewModel.isLoadCompleted.observe(this, done -> {
            if (done) {
                binding.swipeRefresh.finishRefresh();
                binding.swipeRefresh.finishLoadMore();

                binding.swipeRefresh2.finishRefresh();
                binding.swipeRefresh2.finishLoadMore();
            }
        });


        viewModel.showUserShimmer.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(androidx.databinding.Observable sender, int propertyId) {
                updateUserShimmerState();
            }
        });

        viewModel.showHistoryShimmer.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(androidx.databinding.Observable sender, int propertyId) {
                updateHistoryShimmerState();
            }
        });

        viewModel.noUserData.addOnPropertyChangedCallback(
                new Observable.OnPropertyChangedCallback() {
                    @Override
                    public void onPropertyChanged(androidx.databinding.Observable sender, int propertyId) {

                        // show NoData ONLY when searching
                        if (viewModel.noUserData.get()
                                && viewModel.searchState.getValue() == SearchViewModel.SearchState.SHOW_RESULTS) {

                            binding.layoutNoData.setVisibility(VISIBLE);

                        } else {
                            binding.layoutNoData.setVisibility(GONE);
                        }
                    }
                }
        );



        viewModel.searchState.observe(this, state -> {
            switch (state) {
                case SHOW_HISTORY:
                    binding.rvSearch.setVisibility(View.VISIBLE);
                    binding.rvMessage.setVisibility(View.GONE);

                    binding.swipeRefresh2.setVisibility(View.VISIBLE);
                    binding.swipeRefresh.setVisibility(View.GONE);
                    recentSearch(true);
                    showSearchHistory();
                    break;

                case SHOW_RESULTS:
                    binding.rvSearch.setVisibility(View.GONE);
                    binding.rvMessage.setVisibility(View.VISIBLE);

                    binding.swipeRefresh2.setVisibility(View.GONE);
                    binding.swipeRefresh.setVisibility(View.VISIBLE);
                    binding.layoutNoData.setVisibility(GONE);
                    recentSearch(false);
                    showSearchResults();
                    break;
            }
        });


        viewModel.searchUserAdapter.setOnUserClickListener(new SearchUserAdapter.OnUserClickListener() {
            @Override
            public void onFollowClick(@NonNull GuestProfileRoot.User user, @NonNull ItemSearchUsersBinding itemBinding, int position) {
                if (user.getUserId() == null) return;

                itemBinding.pd.setVisibility(VISIBLE);

                itemBinding.tvFollow.setVisibility(GONE);

                userApiCall.followUnfollowUser(!user.isFollow(), user.getUserId(), "", new UserApiCall.OnFollowUnfollowListener() {
                    @Override
                    public void onFollowSuccess() {
                        user.setFollow(true);
                        viewModel.searchUserAdapter.notifyItemChanged(position, user);
                        itemBinding.tvFollow.setVisibility(VISIBLE);
                    }

                    @Override
                    public void onUnfollowSuccess() {
                        user.setFollow(false);
                        viewModel.searchUserAdapter.notifyItemChanged(position, user);
                        itemBinding.tvFollow.setVisibility(VISIBLE);
                    }

                    @Override
                    public void onFail() {
                        toggleFollowUI(itemBinding);
                    }
                });
            }

            @Override
            public void onUserClick(@NonNull GuestProfileRoot.User user, @NonNull ItemSearchUsersBinding binding, int position) {
                if (user.getUserId() != null) {
                    sessionManager.addToSearchHistory(user);
                    startActivity(new Intent(SearchActivity.this, GuestActivity.class).putExtra(Const.USERID, user.getUserId()));
                }

                Call<RestResponse> call = RetrofitBuilder.create().CreateSearchHistory(sessionManager.getUser().getId(),user.getUserId(), viewModel.keyword);
                call.enqueue(new Callback<RestResponse>() {
                    @Override
                    public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                        Log.d(TAG, "onResponse: ======history Add successfully!!");
                    }

                    @Override
                    public void onFailure(Call<RestResponse> call, Throwable throwable) {
                        Log.d(TAG, "onFailure: ====" + throwable.getLocalizedMessage());
                    }
                });

            }
        });

        viewModel.searchHistoryAdapter.setOnUserClickListener(new SearchHistoryAdapter.OnUserClickListener() {
            @Override
            public void onDeleteClick(@NonNull SearchHistoryRoot.DataItem user, @NonNull ItemSearchHistoryBinding binding, int position) {
                Call<RestResponse> call = RetrofitBuilder.create().deleteHistory(user.getSearchedBy(),user.getId());
                call.enqueue(new Callback<RestResponse>() {
                    @Override
                    public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                        Log.d(TAG, "onResponse: ====delete successfully!!");


                        List<SearchHistoryRoot.DataItem> current = new ArrayList<>(viewModel.searchHistoryAdapter.getCurrentList());
                        current.remove(position);
                        viewModel.searchHistoryAdapter.submitList(current);

                        if (current.isEmpty()){
                            viewModel.noHistoryData.set(true);
                            recentSearch(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<RestResponse> call, Throwable throwable) {
                        Log.d(TAG, "onFailure: ======" + throwable.getLocalizedMessage());
                    }
                });
            }

            @Override
            public void onUserClick(@NonNull SearchHistoryRoot.DataItem user, @NonNull ItemSearchHistoryBinding binding, int position) {
                if (user.getSearchedUser().getId() != null) {
                    startActivity(new Intent(SearchActivity.this, GuestActivity.class).putExtra(Const.USERID, user.getSearchedUser().getId()));
                }

                Call<RestResponse> call = RetrofitBuilder.create().CreateSearchHistory(sessionManager.getUser().getId(),user.getSearchedUser().getId(), viewModel.keyword);
                 call.enqueue(new Callback<RestResponse>() {
                     @Override
                     public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                         Log.d(TAG, "onResponse: ======history add successfully!!");
                     }

                     @Override
                     public void onFailure(Call<RestResponse> call, Throwable throwable) {
                         Log.d(TAG, "onFailure: ====" + throwable.getLocalizedMessage());
                     }
                 });
            }
        });
    }



    private void updateUserShimmerState() {
        if (viewModel.showUserShimmer.get()) {
            binding.shimmer.setVisibility(View.VISIBLE);
            binding.shimmer.startShimmer();

            // hide both recyclers
            binding.rvMessage.setVisibility(View.GONE);
            binding.rvSearch.setVisibility(View.GONE);

            binding.swipeRefresh.setVisibility(View.GONE);
            binding.swipeRefresh2.setVisibility(View.GONE);

        } else {
            binding.shimmer.stopShimmer();
            binding.shimmer.setVisibility(View.GONE);

            // show only user recycler
            binding.rvMessage.setVisibility(View.VISIBLE);
            binding.swipeRefresh.setVisibility(View.VISIBLE);
        }
    }

    private void updateHistoryShimmerState() {
        if (viewModel.showHistoryShimmer.get()) {
            binding.shimmer.setVisibility(View.VISIBLE);
            binding.shimmer.startShimmer();

            // hide both recyclers
            binding.rvMessage.setVisibility(View.GONE);
            binding.rvSearch.setVisibility(View.GONE);

            binding.swipeRefresh.setVisibility(View.GONE);
            binding.swipeRefresh2.setVisibility(View.GONE);

        } else {
            binding.shimmer.stopShimmer();
            binding.shimmer.setVisibility(View.GONE);

            // show only history recycler
            binding.rvSearch.setVisibility(View.VISIBLE);
            binding.swipeRefresh2.setVisibility(View.VISIBLE);
        }
    }



    private void showSearchHistory() {
        binding.rvSearch.setVisibility(VISIBLE);
        binding.rvMessage.setVisibility(GONE);

        binding.swipeRefresh2.setVisibility(VISIBLE);
        binding.swipeRefresh.setVisibility(GONE);
    }

    private void showSearchResults() {
        binding.rvSearch.setVisibility(GONE);
        binding.rvMessage.setVisibility(VISIBLE);

        binding.swipeRefresh2.setVisibility(GONE);
        binding.swipeRefresh.setVisibility(VISIBLE);
    }


    private void toggleFollowUI(ItemSearchUsersBinding binding) {
        binding.tvFollow.setVisibility(VISIBLE);
        binding.pd.setVisibility(GONE);
    }

    private void recentSearch(boolean isvisible){
        if (isvisible){
            binding.layRecentSearches.setVisibility(VISIBLE);
        }else {
            binding.layRecentSearches.setVisibility(GONE);
        }
    }
}

