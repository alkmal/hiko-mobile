package com.codder.ultimate;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.codder.ultimate.live.activity.UploadActivity;
import com.codder.ultimate.live.model.BannerRoot;
import com.codder.ultimate.live.model.GiftCategoryRoot;
import com.codder.ultimate.live.model.GiftRoot;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.modelclass.BroadcastBannerRoot;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.profile.modelclass.AdsRoot;
import com.codder.ultimate.profile.modelclass.SettingRoot;
import com.codder.ultimate.profile.modelclass.VipPlanRoot;
import com.codder.ultimate.retrofit.Const;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SessionManager {

    private static final String FAKE_LIVE_LIST = "fakeLiveList";
    private final SharedPreferences pref;

    private final SharedPreferences.Editor editor;

    private static final String ADS = "ads";

    private static final String SEARCH_HISTORY = "searchhistry";
    private static final String KEY_BLOCKED_USERS = "KEY_BLOCKED_USERS";


    @SuppressLint("CommitPrefEdits")
    public SessionManager(Context context) {
        this.pref = context.getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
        this.editor = this.pref.edit();
    }


    public static String getUserId(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        if (sessionManager.getBooleanValue(Const.IS_LOGIN)) {
            return sessionManager.getUser().getId();
        }
        return "";
    }

    public void saveStringValue(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String getStringValue(String key) {
        return pref.getString(key, "");
    }

    public boolean isNotificationOn() {
        return pref.getBoolean("isNotification", false);
    }

    public void notificationOnOff(boolean value) {
        editor.putBoolean("isNotification", value);
        editor.apply();

    }

    public void saveBooleanValue(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBooleanValue(String key) {
        return pref.getBoolean(key, false);
    }

    public void saveSetting(SettingRoot.Setting setting) {
        editor.putString(Const.SETTING, new Gson().toJson(setting));
        editor.apply();
    }

    public SettingRoot.Setting getSetting() {
        String userString = pref.getString(Const.SETTING, "");
        if (userString != null && !userString.isEmpty()) {
            return new Gson().fromJson(userString, SettingRoot.Setting.class);
        }
        return null;
    }

    public void saveUser(UserRoot.User userDummy) {
        editor.putString(Const.USER_STR, new Gson().toJson(userDummy));
        editor.apply();


    }

    public UserRoot.User getUser() {
        String userString = pref.getString(Const.USER_STR, "");
        if (userString != null && !userString.isEmpty()) {
            return new Gson().fromJson(userString, UserRoot.User.class);
        }
        return null;
    }

    public void addToSearchHistory(GuestProfileRoot.User newUser) {
        List<GuestProfileRoot.User> fav = getSearchHistory();

        if (newUser != null) {
            for (Iterator<GuestProfileRoot.User> iterator = fav.iterator(); iterator.hasNext(); ) {
                GuestProfileRoot.User user = iterator.next();
                if (user.getUserId().equals(newUser.getUserId())) {
                    iterator.remove();
                }
            }
        }

        fav.add(newUser);
        editor.putString(SEARCH_HISTORY, new Gson().toJson(fav));
        editor.apply();
    }

    public List<GuestProfileRoot.User> getSearchHistory() {
        String userString = pref.getString(SEARCH_HISTORY, "");
        if (!userString.isEmpty()) {
            List<GuestProfileRoot.User> list = new Gson().fromJson(userString, new TypeToken<ArrayList<GuestProfileRoot.User>>() {
            }.getType());
            Collections.reverse(list);
            return list;
        }
        return new ArrayList<>();
    }

    public void ClearAllData() {
        editor.clear();
        editor.apply();
    }

    public void saveAds(AdsRoot.Advertisement setting) {
        editor.putString(ADS, new Gson().toJson(setting));
        editor.apply();
    }

    public AdsRoot.Advertisement getAds() {
        String userString = pref.getString(ADS, "");
        if (userString != null && !userString.isEmpty()) {
            return new Gson().fromJson(userString, AdsRoot.Advertisement.class);
        }
        return null;
    }

    public void saveVipPlan(List<VipPlanRoot.VipPlanItem> bannerRootsList) {
        String userListJson = new Gson().toJson(bannerRootsList);
        editor.putString("vipPlanList", userListJson);
        editor.apply();
        Log.d("SessionManager", "Saving VIP Plans JSON: " + userListJson);

    }

    public List<VipPlanRoot.VipPlanItem> getVipPlan() {
        String bannerList = pref.getString("vipPlanList", null);
        if (bannerList != null) {
            Type userListType = new TypeToken<List<VipPlanRoot.VipPlanItem>>() {
            }.getType();
            return new Gson().fromJson(bannerList, userListType);
        }
        return new ArrayList<>();
    }

    public List<GiftCategoryRoot.CategoryItem> getGiftCategoriesList() {
        String giftCategories = pref.getString(Const.GIFT_CATEGORY_LIST, null);
        if (giftCategories != null) {
            Type userListType = new TypeToken<List<GiftCategoryRoot.CategoryItem>>() {
            }.getType();
            return new Gson().fromJson(giftCategories, userListType);
        }
        return new ArrayList<>();
    }

    public void clearBooleanValue(String key) {
        editor.remove(key);
        editor.apply();
    }

    public void saveGiftsList(String key, List<GiftRoot.GiftItem> giftCategoryItems) {
        String userListJson = new Gson().toJson(giftCategoryItems);

        Log.d("TAG", "saveGiftsList: ==> " +userListJson.toString());
        editor.putString(key, userListJson);
        editor.apply();
    }

    public void saveGiftCategories(List<GiftCategoryRoot.CategoryItem> giftCategoryItems) {
        String userListJson = new Gson().toJson(giftCategoryItems);

        Log.d("TAG", "saveGiftCategories: ==> " +userListJson.toString());
        editor.putString(Const.GIFT_CATEGORY_LIST, userListJson);
        editor.apply();
    }

    public List<GiftRoot.GiftItem> getGiftsList(String key) {
        String giftCategories = pref.getString(key, null);
        if (giftCategories != null) {
            Type userListType = new TypeToken<List<GiftRoot.GiftItem>>() {
            }.getType();
            return new Gson().fromJson(giftCategories, userListType);
        }
        return new ArrayList<>();
    }

    public void saveLiveUserForBackground(PkAudioLiveUserRoot.UsersItem bannerRootsList) {
        String userListJson = new Gson().toJson(bannerRootsList);
        editor.putString("liveUserForBackground", userListJson);
        editor.apply();
    }

    public PkAudioLiveUserRoot.UsersItem getLiveUserForBackground() {
        String userString = pref.getString("liveUserForBackground", "");
        if (userString != null && !userString.isEmpty()) {
            return new Gson().fromJson(userString, PkAudioLiveUserRoot.UsersItem.class);
        }
        return null;
    }

    public UploadActivity.LocalVideo getLocalVideo() {
        String userString = pref.getString("localvid", "");
        if (userString != null && !userString.isEmpty()) {
            return new Gson().fromJson(userString, UploadActivity.LocalVideo.class);
        }
        return null;
    }

    public void saveLocalVideo(UploadActivity.LocalVideo userDummy) {
        editor.putString("localvid", new Gson().toJson(userDummy));
        editor.apply();
    }

    public void setIsAudioRoomBackground(boolean isAudioRoomBackground){
        editor.putBoolean("isAudioRoomBackground", isAudioRoomBackground);
        editor.apply();
    }

    public boolean getIsAudioRoomBackground(){
        return pref.getBoolean("isAudioRoomBackground", false);
    }

    public void setIsAudioRoomExit(boolean isAudioRoomExit){
        editor.putBoolean("isAudioRoomExit", isAudioRoomExit);
        editor.apply();
    }

    public boolean getIsAudioRoomExit(){
        return pref.getBoolean("isAudioRoomExit", true);
    }

    public void setIsUserBackgroundLive(boolean isUserBackgroundLive){
        editor.putBoolean("isUserBackgroundLive", isUserBackgroundLive);
        editor.apply();
    }

    public boolean getIsUserBackgroundLive(){
        return pref.getBoolean("isUserBackgroundLive", false);
    }

    public void saveUserAudioBgModel(PkAudioLiveUserRoot.UsersItem userAudioBgModel) {
        String userListJson = new Gson().toJson(userAudioBgModel);
        editor.putString("userAudioBgModel", userListJson);
        editor.apply();
    }

    public PkAudioLiveUserRoot.UsersItem getUserAudioBgModel() {
        String userString = pref.getString("userAudioBgModel", "");
        if (userString != null && !userString.isEmpty()) {
            return new Gson().fromJson(userString, PkAudioLiveUserRoot.UsersItem.class);
        }
        return null;
    }

    public void saveBannerList(List<BannerRoot.BannerItem> bannerRootsList) {
        String userListJson = new Gson().toJson(bannerRootsList);
        editor.putString("BannerList", userListJson);
        editor.apply();
    }

    public List<BannerRoot.BannerItem> getBannerList() {
        String bannerList = pref.getString("BannerList", null);
        if (bannerList != null) {
            Type userListType = new TypeToken<List<BannerRoot.BannerItem>>() {
            }.getType();
            return new Gson().fromJson(bannerList, userListType);
        }
        return new ArrayList<>();
    }

    public void saveFakeLiveList(List<PkAudioLiveUserRoot.UsersItem> pkAudioLiveUserRoots) {
        String userListJson = new Gson().toJson(pkAudioLiveUserRoots);
        editor.putString(FAKE_LIVE_LIST, userListJson);
        editor.apply();
    }

    public List<PkAudioLiveUserRoot.UsersItem> getShuffledFakeLiveList() {
        String giftCategories = pref.getString(FAKE_LIVE_LIST, null);
        if (giftCategories != null) {
            Type userListType = new TypeToken<List<PkAudioLiveUserRoot.UsersItem>>() {}.getType();
            List<PkAudioLiveUserRoot.UsersItem> giftItemList = new Gson().fromJson(giftCategories, userListType);
            return giftItemList;
        }
        return new ArrayList<>();
    }


    private List<String> blockedUserIds = new ArrayList<>();

    public void setBlockedUserIds(List<String> blockedUserIds) {
        this.blockedUserIds = blockedUserIds;
        String json = new Gson().toJson(blockedUserIds);
        editor.putString(KEY_BLOCKED_USERS, json);
        editor.apply();
    }

    public List<String> getBlockedUserIds() {
        if (blockedUserIds == null || blockedUserIds.isEmpty()) {
            String json = pref.getString(KEY_BLOCKED_USERS, null);
            if (json != null) {
                blockedUserIds = new Gson().fromJson(json, new TypeToken<List<String>>() {}.getType());
            } else {
                blockedUserIds = new ArrayList<>();
            }
        }
        return blockedUserIds;
    }

    public void saveBroadcastBannerList(List<BroadcastBannerRoot.BroadcastBannerItem> bannerRootsList) {
        String userListJson = new Gson().toJson(bannerRootsList);
        editor.putString("BroadcastBannerList", userListJson);
        editor.apply();
    }

    public List<BroadcastBannerRoot.BroadcastBannerItem> getBroadcastBannerList() {
        String bannerList = pref.getString("BroadcastBannerList", null);
        if (bannerList != null) {
            Type userListType = new TypeToken<List<BroadcastBannerRoot.BroadcastBannerItem>>() {
            }.getType();
            return new Gson().fromJson(bannerList, userListType);
        }
        return new ArrayList<>(); // Return an empty list if no data found
    }

    public void saveGameBroadcastBannerList(List<BroadcastBannerRoot.BroadcastBannerItem> bannerRootsList) {
        String userListJson = new Gson().toJson(bannerRootsList);
        editor.putString("GameBroadcastBannerList", userListJson);
        editor.apply();
    }

    public List<BroadcastBannerRoot.BroadcastBannerItem> getGameBroadcastBannerList() {
        String bannerList = pref.getString("GameBroadcastBannerList", null);
        if (bannerList != null) {
            Type userListType = new TypeToken<List<BroadcastBannerRoot.BroadcastBannerItem>>() {
            }.getType();
            return new Gson().fromJson(bannerList, userListType);
        }
        return new ArrayList<>(); // Return an empty list if no data found
    }



}
