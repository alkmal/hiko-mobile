package com.codder.ultimate.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.post.activity.FeedListActivity;
import com.codder.ultimate.activity.HostRequestActivity;
import com.codder.ultimate.activity.MainActivity;
import com.codder.ultimate.activity.SplashActivity;
import com.codder.ultimate.chat.activity.ChatActivity;
import com.codder.ultimate.guestuser.activity.GuestActivity;
import com.codder.ultimate.modelclass.BlockedUserListRoot;
import com.codder.ultimate.reels.activity.ReelsActivity;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FirebaseMessage extends FirebaseMessagingService {

    static final String TAG = "FirebaseMessage";
    private static final String CHANNEL_ID = "01";
    private SessionManager sessionManager;
    private final List<String> blockedUserIds = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        sessionManager = new SessionManager(this);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "FCM Received Message: " + new Gson().toJson(remoteMessage));
        if (remoteMessage.getNotification() != null) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            Log.d(TAG, "Notification Title: " + notification.getTitle());
            Log.d(TAG, "Notification Body: " + notification.getBody());
            Log.d(TAG, "Notification Image URL: " + notification.getImageUrl());
        }
        if (!remoteMessage.getData().isEmpty()) {
            Log.d(TAG, "Data Payload: " + new Gson().toJson(remoteMessage.getData()));
        }

        Map<String, String> messageData = remoteMessage.getData();
        String type = messageData.get("type");
        String data = messageData.get("data");

        try {
            JSONObject json = new JSONObject(messageData);
            Log.d(TAG, "Formatted Data Payload:\n" + json.toString(4)); // Pretty print
        } catch (JSONException e) {
            Log.e(TAG, "Failed to format data payload.", e);
        }


        if (type == null || type.isEmpty()) {
            handleDefaultNotification(remoteMessage);
            return;
        }

        if ("CALL".equals(type)) {
            handleCallNotification(remoteMessage, data, messageData);
            return;
        }

        Intent intent = resolveIntentByType(type, data);
        if (intent == null) {
            Log.w(TAG, "Intent is null. Aborting notification handling.");
            return;
        }

        if ("MESSAGE".equals(type) && isUserInChat(data)) {
            Log.d(TAG, "User already chatting. Skipping notification.");
            return;
        }

        if ("GIFT".equals(type) && isUserInChatGift(data)) {
            Log.d(TAG, "User already chatting. Skipping notification.");
            return;
        }

        if ("MESSAGE".equals(type)) {
            try {
                JSONObject jsonData = new JSONObject(data);
                final String senderId = jsonData.optString("userId", "");
                final String currentUserId = sessionManager.getUser() != null ? sessionManager.getUser().getId() : null;

                Log.d(TAG, "Checking blocked status for senderId: " + senderId);

                if (currentUserId == null) {
                    Log.w(TAG, "Current user ID is null. Cannot fetch block list.");
                    return;
                }

                RetrofitBuilder.create().getBlockUser(currentUserId).enqueue(new Callback<BlockedUserListRoot>() {
                    @Override
                    public void onResponse(Call<BlockedUserListRoot> call, Response<BlockedUserListRoot> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<BlockedUserListRoot.BlockedUsersItem> blockedUsers = response.body().getBlockedUsers();
                            blockedUserIds.clear();

                            if (blockedUsers != null) {
                                for (BlockedUserListRoot.BlockedUsersItem blockedUser : blockedUsers) {
                                    if (blockedUser != null && blockedUser.getToUserId() != null) {
                                        blockedUserIds.add(blockedUser.getToUserId().getId());
                                    }
                                }
                            }

                            Log.d(TAG, "Blocked user IDs updated: " + blockedUserIds);

                            if (blockedUserIds.contains(senderId)) {
                                Log.w(TAG, "Notification blocked — sender is in blocked list: " + senderId);
                                return; // ⛔ Don't show notification
                            }

                            // ✅ Show notification only if not blocked
                            if (sessionManager.isNotificationOn()) {
                                showFirebaseNotification(remoteMessage, remoteMessage.getNotification(), intent);
                            }
                        } else {
                            Log.w(TAG, "Failed to fetch blocked users. Code: " + response.code());
                            // Optional fallback
                            if (sessionManager.isNotificationOn()) {
                                showFirebaseNotification(remoteMessage, remoteMessage.getNotification(), intent);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<BlockedUserListRoot> call, Throwable t) {
                        Log.e(TAG, "Error fetching blocked users", t);
                        // Optional fallback
                        if (sessionManager.isNotificationOn()) {
                            showFirebaseNotification(remoteMessage, remoteMessage.getNotification(), intent);
                        }
                    }
                });

            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse message data", e);
            }

            return; // ⛔ Important! Prevents further notification showing
        }

        // ✅ For other types (not MESSAGE), show notification directly
        if (sessionManager.isNotificationOn()) {
            showFirebaseNotification(remoteMessage, remoteMessage.getNotification(), intent);
        }
    }

    private void handleDefaultNotification(RemoteMessage remoteMessage) {
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification == null) {
            Log.w(TAG, "No notification payload found.");
            return;
        }

        if (sessionManager.isNotificationOn()) {
            Intent defaultIntent = new Intent(this, SplashActivity.class);
            showFirebaseNotification(remoteMessage, notification, defaultIntent);
        }
    }

    private void handleCallNotification(RemoteMessage message, String data, Map<String, String> messageData) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P || MainApplication.isAppOpen) {
            Log.d(TAG, "Skipping call notification due to app state or version.");
            return;
        }

        RemoteMessage.Notification notification = message.getNotification();
        if (notification == null) {
            Log.w(TAG, "Call notification missing title/body.");
            return;
        }

        Intent serviceIntent = new Intent(this, HeadsUpNotificationService.class);
        Bundle bundle = new Bundle();
        bundle.putString(HeadsUpNotificationService.TITLE, notification.getTitle());
        bundle.putString(HeadsUpNotificationService.DES, notification.getBody());
        bundle.putString(HeadsUpNotificationService.TYPE, "CALL");
        bundle.putString(HeadsUpNotificationService.CALL_FROM, messageData.get("callFrom"));
        bundle.putString(HeadsUpNotificationService.DATA, data);
        serviceIntent.putExtras(bundle);

        ContextCompat.startForegroundService(this, serviceIntent);
        Log.d(TAG, "Foreground service for CALL notification started.");
    }

    private Intent resolveIntentByType(String type, String data) {
        try {
            switch (type) {
                case "MESSAGE":
                    Intent chatIntent = new Intent(this, ChatActivity.class);
                    chatIntent.putExtra(Const.CHATROOM, data);
                    return chatIntent;

                case "USER":
                    Intent userIntent = new Intent(this, GuestActivity.class);
                    userIntent.putExtra(Const.USERID, data);
                    return userIntent;

                case "POST":
                    Intent postIntent = new Intent(this, FeedListActivity.class);
                    postIntent.putExtra(Const.DATA, data);
                    return postIntent;

                case "RELITE":
                    Intent reelIntent = new Intent(this, ReelsActivity.class);
                    reelIntent.putExtra(Const.DATA, data);
                    return reelIntent;

                case "LIVE":

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(Const.isNotification, true);
                    return intent;

                case "HOSTREQUEST":
                    Intent hostRequest = new Intent(this, HostRequestActivity.class);
                    hostRequest.putExtra(Const.DATA, data);
                    return hostRequest;

                default:
                    return new Intent(this, SplashActivity.class);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resolving intent by type: " + type, e);
            return null;
        }
    }

    private boolean isUserInChat(String data) {
        try {
            JSONObject json = new JSONObject(data);
            return ChatActivity.isOPEN && ChatActivity.otherUserId.equals(json.getString("userId"));
        } catch (JSONException e) {
            Log.e(TAG, "Error checking chat state.", e);
            return false;
        }
    }

    private boolean isUserInChatGift(String data) {
        try {
            JSONObject json = new JSONObject(data);
            String incomingTopic = json.optString("chatTopicId",
                   json.optString("topic", "")); // tolerate either key
            return ChatActivity.isOPEN && ChatActivity.topicId.equals(incomingTopic);
        } catch (JSONException e) {
            Log.e(TAG, "Error checking chat state.", e);
            return false;
        }
    }

    private void showFirebaseNotification(RemoteMessage message, RemoteMessage.Notification notification, Intent intent) {
        if (notification == null || intent == null) return;

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.app_logo)
                .setColor(getResources().getColor(R.color.logo_color))
                .setLights(Color.MAGENTA, 1000, 300)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setDefaults(Notification.DEFAULT_VIBRATE);

        if (notification.getImageUrl() != null) {
            Bitmap bitmap = getBitmapFromUrl(notification.getImageUrl().toString());
            if (bitmap != null) {
                builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
                builder.setLargeIcon(bitmap);
            }
        }

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "General Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for all app notifications");
            manager.createNotificationChannel(channel);
        }

        manager.notify(0, builder.build());
    }

    private Bitmap getBitmapFromUrl(String imageUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
            connection.setDoInput(true);
            connection.connect();
            try (InputStream input = connection.getInputStream()) {
                return BitmapFactory.decodeStream(input);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image from URL: " + imageUrl, e);
            return null;
        }
    }
}
