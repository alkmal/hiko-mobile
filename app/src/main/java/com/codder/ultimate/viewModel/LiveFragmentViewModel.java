package com.codder.ultimate.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LiveFragmentViewModel extends ViewModel {

    private final MutableLiveData<List<PkAudioLiveUserRoot.UsersItem>> liveUsers =
            new MutableLiveData<>();


    public LiveData<List<PkAudioLiveUserRoot.UsersItem>> getLiveUsers() {
        return liveUsers;
    }

    public void refreshLiveUsers(
            String userId,
            String countryCode,
            List<PkAudioLiveUserRoot.UsersItem> fakeUsers
    ) {

        RetrofitBuilder.create()
                .getLiveUsersList(userId, "AudioLive", "", 0, Const.LIMIT, countryCode)
                .enqueue(new Callback<PkAudioLiveUserRoot>() {

                    @Override
                    public void onResponse(Call<PkAudioLiveUserRoot> call,
                                           Response<PkAudioLiveUserRoot> response) {

                        List<PkAudioLiveUserRoot.UsersItem> finalList = new ArrayList<>();
                        List<PkAudioLiveUserRoot.UsersItem> mergedUsers = new ArrayList<>();

                        // 🔹 STATIC: My Room
                        PkAudioLiveUserRoot.UsersItem myRoom = new PkAudioLiveUserRoot.UsersItem();
                        myRoom.setId("STATIC_MY_ROOM");
                        myRoom.setItemType(PkAudioLiveUserRoot.UsersItem.TYPE_MY_ROOM);
                        finalList.add(myRoom);

                        // 🔹 STATIC: Quick Join
                        PkAudioLiveUserRoot.UsersItem quickJoin = new PkAudioLiveUserRoot.UsersItem();
                        quickJoin.setId("STATIC_QUICK_JOIN");
                        quickJoin.setItemType(PkAudioLiveUserRoot.UsersItem.TYPE_QUICK_JOIN);
                        finalList.add(quickJoin);

                        // 🔹 REAL USERS
                        if (response.isSuccessful() && response.body() != null) {
                            for (PkAudioLiveUserRoot.UsersItem user : response.body().getUsers()) {
                                user.setItemType(PkAudioLiveUserRoot.UsersItem.TYPE_USER);
                                user.setUniqueKey("REAL_" + user.getId());
                                mergedUsers.add(user);
                            }
                        }

                        // 🔹 FAKE USERS
                        if (fakeUsers != null && !fakeUsers.isEmpty()) {
                            for (PkAudioLiveUserRoot.UsersItem fake : fakeUsers) {
                                fake.setItemType(PkAudioLiveUserRoot.UsersItem.TYPE_USER);
                                fake.setUniqueKey("FAKE_" + fake.getRoomName());
                                mergedUsers.add(fake);
                            }
                        }

                        // 🔥 ODD INDEX ONLY (STRICT)
                        List<PkAudioLiveUserRoot.UsersItem> filteredUsers = new ArrayList<>();
                        for (int i = 0; i < mergedUsers.size(); i++) {
                            if (i % 2 != 0) {
                                filteredUsers.add(mergedUsers.get(i));
                            }
                        }

                        finalList.addAll(filteredUsers);
                        liveUsers.postValue(finalList);
                    }

                    @Override
                    public void onFailure(Call<PkAudioLiveUserRoot> call, Throwable t) {
                        liveUsers.postValue(fakeUsers == null
                                ? new ArrayList<>()
                                : fakeUsers);
                    }
                });
    }
}
