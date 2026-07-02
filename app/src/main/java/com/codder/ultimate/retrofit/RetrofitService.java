package com.codder.ultimate.retrofit;

import com.codder.ultimate.chat.modelclass.CallRequestRoot;
import com.codder.ultimate.chat.modelclass.ChatListRoot;
import com.codder.ultimate.chat.modelclass.ChatTopicRoot;
import com.codder.ultimate.chat.modelclass.ChatUserListRoot;
import com.codder.ultimate.chat.modelclass.UploadImageRoot;
import com.codder.ultimate.guestuser.model.FollowUnfollowResponse;
import com.codder.ultimate.launguagetranslation.modelclass.ActiveLanguageRoot;
import com.codder.ultimate.launguagetranslation.modelclass.LatestCodeofLanguage;
import com.codder.ultimate.launguagetranslation.modelclass.SingleLanguageTranslation;
import com.codder.ultimate.leaderboard.LeaderboardDataRoot;
import com.codder.ultimate.live.model.BannerRoot;
import com.codder.ultimate.live.model.BlockedUserResponse;
import com.codder.ultimate.live.model.GiftCategoryRoot;
import com.codder.ultimate.live.model.GiftRoot;
import com.codder.ultimate.live.model.HashtagsRoot;
import com.codder.ultimate.live.model.LiveStreamRoot;
import com.codder.ultimate.live.model.LiveSummaryRoot;
import com.codder.ultimate.live.model.PkAudioLiveUserRoot;
import com.codder.ultimate.live.model.ReactionRoot;
import com.codder.ultimate.live.model.SearchLocationRoot;
import com.codder.ultimate.live.model.SongRoot;
import com.codder.ultimate.live.model.StickerRoot;
import com.codder.ultimate.live.model.ThemeRoot;
import com.codder.ultimate.modelclass.BlockUnblockRoot;
import com.codder.ultimate.modelclass.BlockedUserListRoot;
import com.codder.ultimate.chat.modelclass.ChatSuggestion;
import com.codder.ultimate.modelclass.BroadcastBannerRoot;
import com.codder.ultimate.modelclass.ComplainRoot;
import com.codder.ultimate.modelclass.GuestProfileRoot;
import com.codder.ultimate.modelclass.GuestUsersListRoot;
import com.codder.ultimate.modelclass.IpAddressRoot_e;
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.modelclass.SearchHistoryRoot;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.post.model.PostCommentRoot;
import com.codder.ultimate.post.model.PostRoot;
import com.codder.ultimate.profile.modelclass.AdsRoot;
import com.codder.ultimate.profile.modelclass.CoinRecordRoot;
import com.codder.ultimate.profile.modelclass.CoinSellerDataRoot;
import com.codder.ultimate.profile.modelclass.CoinSellerHistoryRoot;
import com.codder.ultimate.profile.modelclass.CoinSellerRoot;
import com.codder.ultimate.profile.modelclass.CreateUserStripe;
import com.codder.ultimate.profile.modelclass.DiamondPlanRoot;
import com.codder.ultimate.profile.modelclass.HistoryListRoot;
import com.codder.ultimate.profile.modelclass.LevelRoot;
import com.codder.ultimate.profile.modelclass.ReedemListRoot;
import com.codder.ultimate.profile.modelclass.SettingRoot;
import com.codder.ultimate.profile.modelclass.SvgaListRoot;
import com.codder.ultimate.profile.modelclass.VipPlanRoot;
import com.codder.ultimate.reels.model.ReliteRoot;
import com.google.gson.JsonObject;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitService {

    @GET("json")
    Call<IpAddressRoot_e> getIp();

    @GET("/setting")
    Call<SettingRoot> getSettings();

    @POST("/user/loginSignup")
    Call<UserRoot> createUser(@Body JsonObject jsonObject);

    @GET("/user/profile")
    Call<UserRoot> getUser(@Query("userId") String type);

    @Multipart
    @POST("/user/update")
    Call<UserRoot> updateUser(@PartMap Map<String, RequestBody> partMap,
                              @Part MultipartBody.Part requestBody, @Part MultipartBody.Part coverImage);

    @GET("/block/getBlockedUsers")
    Call<BlockedUserListRoot> getBlockUser(@Query("userId") String userId);

    @POST("block/blockOrUnblockUser")
    Call<BlockUnblockRoot> BlockUser(@Query("userId") String userId,
                                     @Query("toUserId") String toUserId);

    @POST("/user/user/search")
    Call<GuestUsersListRoot> searchUser(@Body JsonObject jsonObject);

    @GET("/coinSeller/getCoinSellerUser")
    Call<CoinSellerDataRoot> getMyCoinSellerData(@Query("userId") String userId);

    @PATCH("/coinSeller/coinByCoinSeller")
    Call<CoinSellerDataRoot> sendCoinToUser(@Body JsonObject jsonObject);

    @GET("/coinSellerHistory/historyOfCoinSellerToUser")
    Call<CoinSellerHistoryRoot> getTopUpHistory(@Query("userId") String key);

    @GET("/post/user")
    Call<PostRoot> getUserPostList(@Query("userId") String uId,
                                   @Query("start") int start, @Query("limit") int limit);

    @DELETE("/post/deletePost")
    Call<RestResponse> deletePost(@Query("postId") String postId);

    @POST("/report")
    Call<RestResponse> reportThisUser(@Body JsonObject jsonObject);

    @GET("/favorite/likeUnlike")
    Call<RestResponse> toggleLikePost(@Query("userId") String userId, @Query("postId") String postId);

    @GET("/favorite/likeUnlike")
    Call<RestResponse> toggleLikeReel(@Query("userId") String userId, @Query("videoId") String videoId);

    @GET("/post/getFollowingPost")
    Call<PostRoot> getFollowingPost(@Query("userId") String uId, @Query("type") String type,
                                    @Query("start") int start, @Query("limit") int limit);

    @GET("/post/getPopularLatestPost")
    Call<PostRoot> getPostList(@Query("userId") String uId, @Query("type") String type,
                               @Query("start") int start, @Query("limit") int limit);

    @GET("/chatTopic/chatList")
    Call<ChatUserListRoot> getChatUserList(@Query("userId") String userId,
                                           @Query("start") int start, @Query("limit") int limit);

    @DELETE("chatTopic/deleteAllChatsAndTopics")
    Call<RestResponse> deleteAllChat(@Query("userId") String userId);

    @GET("/history/sendGiftFakeHost")
    Call<UserRoot> sendGiftFakeHost(@Query("senderUserId") String senderUserId, @Query("coin") double coin, @Query("receiverUserId") String receiverUserId, @Query("type") String type);

    @DELETE("/chat/deleteMessage")
    Call<RestResponse> deleteChat(@Query("chatId") String chatId);

    @GET("/chat/getOldChat")
    Call<ChatListRoot> getOldChats(@Query("topicId") String chatRoomId,
                                   @Query("userId") String userId,
                                   @Query("start") int start, @Query("limit") int limit);

    @Multipart
    @POST("/chat/uploadImage")
    Call<UploadImageRoot> uploadChatImage(
            @PartMap Map<String, RequestBody> partMap,
            @Part MultipartBody.Part requestBody);

    @POST("/chatTopic/createRoom")
    Call<ChatTopicRoot> createChatRoom(@Body JsonObject jsonObject);

    @POST("/history/call")
    Call<CallRequestRoot> makeCallRequest(@Body JsonObject jsonObject);

    @POST("/user/getUser")
    Call<GuestProfileRoot> getGuestUser(@Body JsonObject jsonObject);

    @POST("/follower/followUnfollow")
    Call<FollowUnfollowResponse> toggleFollowUnfollow(@Body JsonObject jsonObject);

    @POST("/follower/followingList")
    Call<GuestUsersListRoot> getFollowingList(@Body JsonObject jsonObject);

    @POST("/follower/followerList")
    Call<GuestUsersListRoot> getFollowersList(@Body JsonObject jsonObject);

    @GET("/video/getRelite")
    Call<ReliteRoot> getRelites(@Query("userId") String uId, @Query("type") String type,
                                @Query("start") int start, @Query("limit") int limit);

    @GET("/comment")
    Call<PostCommentRoot> getPostCommentList(@Query("userId") String uId, @Query("postId") String postId,
                                             @Query("start") int start, @Query("limit") int limit);

    @GET("/comment")
    Call<PostCommentRoot> getReliteCommentList(@Query("userId") String uId, @Query("videoId") String postId,
                                               @Query("start") int start, @Query("limit") int limit);

    @GET("/favorite")
    Call<PostCommentRoot> getPostLikeList(@Query("userId") String uId, @Query("postId") String postId,
                                          @Query("start") int start, @Query("limit") int limit);

    @GET("/favorite")
    Call<PostCommentRoot> getReliteLikeList(@Query("userId") String uId, @Query("videoId") String postId,
                                            @Query("start") int start, @Query("limit") int limit);

    @DELETE("/comment")
    Call<RestResponse> deleteComment(@Query("commentId") String chatId);

    @POST("/comment")
    Call<RestResponse> addComment(@Body JsonObject jsonObject);

    @DELETE("/video/deleteRelite")
    Call<RestResponse> deleteRelite(@Query("videoId") String videoId);

    @Multipart
    @POST("/complain")
    Call<RestResponse> addSupport(
            @PartMap Map<String, RequestBody> partMap,
            @Part MultipartBody.Part requestBody);

    @GET("/complain/userList")
    Call<ComplainRoot> getComplains(@Query("userId") String userid);

    @Multipart
    @POST("/hostRequest/createRequest")
    Call<RestResponse> addHostRequest(@PartMap Map<String, RequestBody> partMap, @Part MultipartBody.Part requestBody);

    @GET("/svga/get")
    Call<SvgaListRoot> getSvgaList(@Query("userId") String userId, @Query("type") String type, @Query("start") int start, @Query("limit") int limit);

    @POST("svga/purchase")
    Call<UserRoot> purchaseSvga(@Query("type") String type, @Body JsonObject jsonObject);

    @POST("svga/select")
    Call<UserRoot> selectSvga(@Body JsonObject jsonObject);

    @POST("/user/addReferralCode")
    Call<UserRoot> redeemReferralCode(@Body JsonObject jsonObject);

    @POST("/history/income/seeAd")
    Call<UserRoot> addDiamondFromAds(@Body JsonObject jsonObject);

    @POST("/vipPlan/purchase/googlePlay")
    Call<UserRoot> callPurchaseApiGooglePayVip(@Body JsonObject jsonObject);

    @POST("/coinPlan/stripe/createCustomer")
    Call<CreateUserStripe> getStripeCustomer(@Body JsonObject jsonObject);

    @POST("/vipPlan/purchase/stripe")
    Call<UserRoot> purchasePlanStripeVip(@Body JsonObject jsonObject);

    @GET("/vipPlan")
    Call<VipPlanRoot> getVipPlan();

    @GET("/level")
    Call<LevelRoot> getLevels();

    @GET("/history/diamondRcoinTotal")
    Call<CoinRecordRoot> getCoinRecord(@Query("userId") String userId,
                                       @Query("startDate") String startDate,
                                       @Query("endDate") String endDate);

    @GET("/history/diamondRcoinHistory")
    Call<HistoryListRoot> getCoinHistory(@Query("userId") String userId,
                                         @Query("startDate") String startDate,
                                         @Query("endDate") String endDate,
                                         @Query("type") String type,
                                         @Query("start") int start, @Query("limit") int limit);

    @GET("/coinPlan")
    Call<DiamondPlanRoot> getDiamondsPlan();

    @POST("/coinPlan/purchase/googlePlay")
    Call<UserRoot> callPurchaseApiGooglePayDiamond(@Body JsonObject jsonObject);

    @POST("/coinPlan/purchase/stripe")
    Call<UserRoot> purchasePlanStripeDiamonds(@Body JsonObject jsonObject);

    @POST("/history/convertRcoinToDiamond")
    Call<UserRoot> convertRcoinToDiamond(@Body JsonObject jsonObject);

    @GET("/coinSeller")
    Call<CoinSellerRoot> getCoinSellerList(@Query("userId") String userId);

    @POST("/redeem")
    Call<RestResponse> cashOutDiamonds(@Body JsonObject jsonObject);

    @GET("/redeem/user")
    Call<ReedemListRoot> getRedeemHistory(@Query("userId") String userid);

    @GET("/advertisement")
    Call<AdsRoot> getAds();

    @Multipart
    @PATCH("liveUser/live")
    Call<LiveStreamRoot> makeLiveUser(@PartMap Map<String, RequestBody> partMap,
                                      @Part MultipartBody.Part requestBody);

    @GET("hostLiveHistory/hostLive")
    Call<RestResponse> getHostApi(@Query("hostId") String hostId,
                                  @Query("liveType") String liveType,
                                  @Query("date") String date);

    @GET("/liveUser")
    Call<PkAudioLiveUserRoot> getLiveUsersList(@Query("userId") String uId, @Query("type") String type, @Query("keyword") String keyword, @Query("start") int start, @Query("limit") int limit,@Query("country") String country);

    @GET("/song")
    Call<SongRoot> getSongs();

    @Multipart
    @POST("/post/uploadPost")
    Call<RestResponse> uploadPost(@PartMap Map<String, RequestBody> partMap,
                                  @Part MultipartBody.Part requestBody
    );

    @GET("/hashtag")
    Call<HashtagsRoot> searchHashtag(@Query("value") String keyword);

    @Multipart
    @POST("/video/uploadRelite")
    Call<RestResponse> uploadRelite(@PartMap Map<String, RequestBody> partMap,
                                    @Part MultipartBody.Part requestBody1,
                                    @Part MultipartBody.Part requestBody2,
                                    @Part MultipartBody.Part requestBody3
    );

    @GET("/reaction/getReaction")
    Call<ReactionRoot> getReactions();

    @DELETE("/liveUser/terminateAudioSession")
    Call<RestResponse> deleteRoom(@Query("userId") String userId);

    @PATCH("/liveuser/broadcastAlertSound")
    Call<RestResponse> getNotification(@Query("userId") String userId);

    @Multipart
    @PATCH("/liveUser/updateRoomImage")
    Call<RestResponse> updateRoomImage(@PartMap Map<String, RequestBody> partMap,
                                       @Part MultipartBody.Part requestBody);

    @GET("/theme")
    Call<ThemeRoot> getTheme();

    @PATCH("/liveUser/updatePrivateCode")
    Call<RestResponse> updatePasscode(@Query("privateCode") String privateCode,
                                      @Query("liveUserId") String liveUserId);

    @GET("/getStreamingSummary")
    Call<LiveSummaryRoot> getLiveSummary(@Query("liveStreamingId") String liveStreamingId);

    @GET("liveUser/checkLive")
    Call<LiveStreamRoot> checkUserLiveOrNot(@Query("userId") String userId);

    @GET("/banner")
    Call<BannerRoot> getBanner(@Query("type") String type);

    @GET("/gift/{cId}")
    Call<GiftRoot> getGiftsByCategory(@Path("cId") String categoryId);

    @GET("/giftCategory")
    Call<GiftCategoryRoot> getGiftCategory();

    @GET("/liveUser/fakeLiveUser")
    Call<PkAudioLiveUserRoot> getFakeLiveList(@Query("start") int start, @Query("limit") int limit);

    @POST("/user/online")
    Call<RestResponse> makeOnlineUser(@Body JsonObject jsonObject);

    @GET("/v1/forward")
    Call<SearchLocationRoot> searchLocation(@Query("access_key") String key, @Query("query") String value);

    @GET("/user/checkPlan")
    Call<RestResponse> checkUserPlan(@Query("userId") String userId);

    @GET("/sticker")
    Call<StickerRoot> getStickers();

    @GET("/block/getUsersWhoBlockedMe")
    Call<BlockedUserResponse> getUsersWhoBlockedMe(@Query("userId") String userId);

    @GET("/suggestedMessage/getAllSuggestedMessages")
    Call<ChatSuggestion> getAllSuggestedMessages();

    @GET("/liveUser/fetchAgencyReceivingRankings")
    Call<LeaderboardDataRoot> getRichAgency(@Query("userId") String userId, @Query("type") String type);

    @GET("/liveUser/fetchUserSpendingRankings")
    Call<LeaderboardDataRoot> getUserRanking(@Query("userId") String userId, @Query("type") String type);

    @GET("/liveUser/fetchHostReceivingRankings")
    Call<LeaderboardDataRoot> getRichHost(@Query("userId") String userId, @Query("type") String type);

    @GET("brodcastbanner/fetchBanners")
    Call<BroadcastBannerRoot> getBroadcastBanner(@Query("bannerType") int bannerType);

    @POST("/searchHistory/create")
    Call<RestResponse> CreateSearchHistory(@Query("searchedBy") String searchedBy,@Query("searchedUser") String searchedUser,@Query("searchText") String searchText);

    @GET("searchHistory/get")
    Call<SearchHistoryRoot> getSearchHistory(@Query("userId") String userId, @Query("start") int start, @Query("limit") int limit);

    @DELETE("searchHistory/delete")
    Call<RestResponse> deleteHistory(@Query("userId") String userId , @Query("historyId") String historyId);

    @GET("translation/getActiveLanguage")
    Call<ActiveLanguageRoot> fetchLanguage(@Query("start") int start , @Query("limit") int limit);

    @GET("translation/getLanguageTranslations")
    Call<SingleLanguageTranslation> fetchTranslation(@Query("languageCode") String languageCode , @Query("module") String module);

    @GET("translation/version/latest")
    Call<LatestCodeofLanguage> fetchGlobalVersion();
}
