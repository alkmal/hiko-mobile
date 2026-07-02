package com.codder.ultimate.modelclass;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.codder.ultimate.live.adapter.LiveStramCommentAdapter;
import com.codder.ultimate.live.adapter.LiveViewAdapter;
import com.codder.ultimate.live.adapter.LiveViewUserAdapter;

import org.json.JSONObject;

public class WatchLiveViewModel extends ViewModel {

    public boolean isMuted = false;

    public LiveViewAdapter liveViewAdapter = new LiveViewAdapter();


    public LiveViewUserAdapter liveViewUserAdapter = new LiveViewUserAdapter();
    public LiveStramCommentAdapter liveStramCommentAdapter = new LiveStramCommentAdapter();
    public MutableLiveData<UserRoot.User> clickedComment = new MutableLiveData<>();
    public MutableLiveData<JSONObject> clickedUser = new MutableLiveData<>();

    public void initLister() {
        liveStramCommentAdapter.setOnCommentClickListener((UserRoot.User userDummy) -> {
            clickedComment.setValue(userDummy);
        });
        liveViewUserAdapter.setOnLiveUserAdapterClickListener((JSONObject userDummy) -> clickedUser.setValue(userDummy));

        liveViewAdapter.setOnLiveUserAdapterClickListener(userDummy -> {
            clickedUser.setValue(userDummy);
        });


    }

}
