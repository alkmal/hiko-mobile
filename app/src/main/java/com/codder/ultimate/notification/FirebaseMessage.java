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

import com.codder.ultimate.BuildConfig;
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
import com.codder.ultimate.modelclass.RestResponse;
import com.codder.ultimate.reels.activity.ReelsActivity;
import com.codder.ultimate.retrofit.Const;
import com.codder.ultimate.retrofit.RetrofitBuilder;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        SessionManager currentSession = sessionManager != null ? sessionManager : new SessionManager(this);
        if (currentSession.getUser() == null
                || currentSession.getUser().getId() == null
                || currentSession.getUser().getIdentity() == null
                || currentSession.getUser().getIdentity().isEmpty()) {
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("userId", currentSession.getUser().getId());
        payload.addProperty("identity", currentSession.getUser().getIdentity());
        payload.addProperty("fcmToken", token);
        RetrofitBuilder.create().updateFcmToken(payload).enqueue(new Callback<RestResponse>() {
            @Override
            public void onResponse(Call<RestResponse> call, Response<RestResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isStatus()) {
                    Log.w(TAG, "Failed to sync refreshed FCM token.");
                }
            }

            @Override
            public void onFailure(Call<RestResponse> call, Throwable throwable) {
                Log.w(TAG, "Failed to sync refreshed FCM token.", throwable);
            }
        });
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

        JSONObject payload = buildPayload(messageData, data);
        String intentData = isChatType(type) ? buildChatRoomPayload(payload) : data;
        Intent intent = resolveIntentByType(type, intentData);
        if (intent == null) {
            Log.w(TAG, "Intent is null. Aborting notification handling.");
            return;
        }

        if ((isChatType(type) || isChatGiftType(type)) && isCurrentOpenChat(payload)) {
            Log.d(TAG, "User already has this chat open. Skipping notification.");
            return;
        }

        if (isChatType(type)) {
            final String senderId = getChatSenderId(payload);
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

                            if (!senderId.isEmpty() && blockedUserIds.contains(senderId)) {
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
            Intent defaultIntent = new Intent(this, SplashActivity.class);
            showDataNotification(remoteMessage, defaultIntent);
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
                case "CHAT":
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

    private boolean isChatType(String type) {
        return "MESSAGE".equals(type) || "CHAT".equals(type);
    }

    private boolean isChatGiftType(String type) {
        return "GIFT".equals(type) || "CHAT_GIFT".equals(type) || Const.CHAT_GIFT.equals(type);
    }

    private JSONObject buildPayload(Map<String, String> messageData, String data) {
        JSONObject payload = new JSONObject();
        if (data != null && !data.isEmpty()) {
            try {
                payload = new JSONObject(data);
            } catch (JSONException e) {
                Log.d(TAG, "Notification data is not a JSON object.");
            }
        }

        try {
            for (Map.Entry<String, String> entry : messageData.entrySet()) {
                if (entry.getValue() != null && !"data".equals(entry.getKey())) {
                    payload.put(entry.getKey(), entry.getValue());
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error merging notification payload.", e);
        }
        return payload;
    }

    private String buildChatRoomPayload(JSONObject payload) {
        JSONObject chatRoom = new JSONObject();
        try {
            String topic = firstNonEmpty(payload, "topic", "chatTopicId");
            String userId = getChatSenderId(payload);
            chatRoom.put("userId", userId);
            chatRoom.put("topic", topic);
            chatRoom.put("chatTopicId", topic);
            chatRoom.put("name", firstNonEmpty(payload, "name", "userName", "senderName"));
            chatRoom.put("image", firstNonEmpty(payload, "image", "userImage", "senderImage"));
            chatRoom.put("avatarFrameImage", firstNonEmpty(payload, "avatarFrameImage", "userAvatarFrameImage", "senderAvatarFrameImage"));
            chatRoom.put("isOnline", firstBoolean(payload, false, "isOnline"));
            chatRoom.put("isFake", firstBoolean(payload, false, "isFake", "isFakeSender"));
        } catch (JSONException e) {
            Log.e(TAG, "Error building chat notification payload.", e);
        }
        return chatRoom.toString();
    }

    private boolean isCurrentOpenChat(JSONObject payload) {
        if (!ChatActivity.isOPEN) return false;

        String incomingTopic = firstNonEmpty(payload, "chatTopicId", "topic");
        if (!incomingTopic.isEmpty() && incomingTopic.equals(ChatActivity.topicId)) {
            return true;
        }

        String senderId = getChatSenderId(payload);
        return !senderId.isEmpty() && senderId.equals(ChatActivity.otherUserId);
    }

    private String getChatSenderId(JSONObject payload) {
        return firstNonEmpty(payload, "senderId", "userId", "fromUserId");
    }

    private String firstNonEmpty(JSONObject payload, String... keys) {
        for (String key : keys) {
            String value = payload.optString(key, "");
            if (value != null && !value.trim().isEmpty() && !"null".equalsIgnoreCase(value.trim())) {
                return value;
            }
        }
        return "";
    }

    private boolean firstBoolean(JSONObject payload, boolean defaultValue, String... keys) {
        for (String key : keys) {
            if (!payload.has(key)) continue;
            Object value = payload.opt(key);
            if (value instanceof Boolean) return (boolean) value;
            if (value instanceof String) return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    private void showFirebaseNotification(RemoteMessage message, RemoteMessage.Notification notification, Intent intent) {
        if (intent == null) return;
        if (notification == null) {
            showDataNotification(message, intent);
            return;
        }

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

    private void showDataNotification(RemoteMessage message, Intent intent) {
        if (message == null || intent == null) return;

        Map<String, String> data = message.getData();
        String title = data.get("title");
        String body = data.get("body");
        if (body == null || body.isEmpty()) body = data.get("message");
        if (title == null || title.isEmpty()) title = getString(R.string.app_name);
        if (body == null) body = "";

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.app_logo)
                .setColor(getResources().getColor(R.color.logo_color))
                .setLights(Color.MAGENTA, 1000, 300)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setDefaults(Notification.DEFAULT_VIBRATE);

        String imageUrl = normalizeImageUrl(data.get("image"));
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Bitmap bitmap = getBitmapFromUrl(imageUrl);
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

    private String normalizeImageUrl(String image) {
        if (image == null || image.isEmpty()) return "";
        if (image.startsWith("http://") || image.startsWith("https://")) return image;
        return BuildConfig.BASE_URL + image.replaceFirst("^/+", "");
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
