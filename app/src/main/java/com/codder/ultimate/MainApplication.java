package com.codder.ultimate;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;

import com.bumptech.glide.request.RequestOptions;
import com.codder.ultimate.agora.rtc.AgoraEventHandler;
import com.codder.ultimate.agora.rtc.Constants;
import com.codder.ultimate.agora.rtc.EngineConfig;
import com.codder.ultimate.agora.rtc.EventHandler;
import com.codder.ultimate.agora.stats.StatsManager;
import com.codder.ultimate.launguagetranslation.TranslationContextWrapper;
import com.codder.ultimate.musicfunction.AudioMixingController;
import com.codder.ultimate.provider.ExoPlayerProvider;
import com.codder.ultimate.provider.JacksonProvider;
import com.codder.ultimate.provider.RoomProvider;
import com.codder.ultimate.socket.MySocketManager;
import com.codder.ultimate.utils.TempUtil;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.vaibhavpandey.katora.Container;
import com.vaibhavpandey.katora.contracts.ImmutableContainer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import io.agora.rtc2.RtcEngine;


public class MainApplication extends Application {

    private static final String TAG = "MainApplication";
    private static final Container CONTAINER = new Container();
    private AgoraEventHandler mHandler = new AgoraEventHandler();
    public static boolean isAppOpen = false;

    private RtcEngine mRtcEngine;
    public static RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.bg_placeholder_defult).error(R.drawable.bg_placeholder_defult);
    public static RequestOptions requestOptionsLive = new RequestOptions().placeholder(R.drawable.placeholder_live).error(R.drawable.placeholder_live);

    public static RequestOptions requestOptionsFeed = new RequestOptions().placeholder(R.drawable.bg_placeholder_feed).error(R.drawable.bg_placeholder_feed);
    private StatsManager mStatsManager = new StatsManager();
    private EngineConfig mGlobalConfig = new EngineConfig();

    private SessionManager sessionManager;


    public static ImmutableContainer getContainer() {
        return CONTAINER;
    }

    public static LeastRecentlyUsedCacheEvictor leastRecentlyUsedCacheEvictor = null;
    public static ExoDatabaseProvider exoDatabaseProvider = null;

    public static Long exoPlayerCacheSize = (long) (900 * 1024 * 1024);

    private static Context context;

    public static SimpleCache simpleCache = null;

    public static Context getAppContext() {
        return MainApplication.context;
    }

    AppLifecycleObserver appLifecycleObserver = new AppLifecycleObserver();

    public static final String CHANNEL_ID = "01";

    private void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel ch = new NotificationChannel(
                        CHANNEL_ID,
                        "General Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                ch.setDescription("Channel for all app notifications");
                nm.createNotificationChannel(ch);
            }
        }
    }


    @Override
    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(new TranslationContextWrapper(base));
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressWarnings("SameParameterValue")
    private void createChannel(String id, String name, int visibility, int importance) {
        NotificationChannel channel = new NotificationChannel(id, name, importance);
        channel.enableLights(true);

        Uri ringUri = Settings.System.DEFAULT_NOTIFICATION_URI;
        channel.setSound(ringUri, new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT).build());

        channel.setLightColor(ContextCompat.getColor(this, R.color.tintColor));
        channel.setLockscreenVisibility(visibility);
        if (importance == NotificationManager.IMPORTANCE_LOW) {
            channel.setShowBadge(false);
        }

        NotificationManager nm =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(channel);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MainApplication.context = getApplicationContext();

        Lifecycle lifecycle = ProcessLifecycleOwner.get().getLifecycle();
        lifecycle.removeObserver(appLifecycleObserver);
        lifecycle.addObserver(appLifecycleObserver);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel(
                    getString(R.string.notification_channel_id),
                    getString(R.string.notification_channel_name),
                    Notification.VISIBILITY_PUBLIC,
                    NotificationManager.IMPORTANCE_HIGH
            );
        }

        sessionManager = new SessionManager(getAppContext());

        CONTAINER.install(new ExoPlayerProvider(this));
        CONTAINER.install(new JacksonProvider());
        CONTAINER.install(new RoomProvider(this));

        if (leastRecentlyUsedCacheEvictor == null) {
            leastRecentlyUsedCacheEvictor = new LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize);
        }

        if (exoDatabaseProvider == null) {
            exoDatabaseProvider = new ExoDatabaseProvider(this);
            Log.i(TAG, "ExoDatabaseProvider initialized.");
        }

        if (simpleCache == null) {
            File exoCacheDir = new File(getCacheDir(), "exo_cache");
            simpleCache = new SimpleCache(exoCacheDir, leastRecentlyUsedCacheEvictor, exoDatabaseProvider);
            Log.i(TAG, "SimpleCache initialized at: ===================== " + getCacheDir().getAbsolutePath());
            Log.i(TAG, "Initial cache size: " + simpleCache.getCacheSpace() + " bytes");

            if (simpleCache.getCacheSpace() >= 400_207_768) {
                Log.w(TAG, "Cache is large (>=400MB), consider clearing.");
                 clearExoCache(); // if you have a manual clear option
            }
        }

        initAgora(this);

        ensureChannel(context);
    }

    public static void clearExoCache() {
        try {
            if (simpleCache != null) {
                simpleCache.release();
                simpleCache = null;
            }

            File cacheDir = new File(getAppContext().getCacheDir(), "exo_cache");
            if (cacheDir.exists()) {
                for (File file : cacheDir.listFiles()) {
                    file.delete();
                }
                cacheDir.delete();
            }

            Log.i(TAG, "ExoPlayer cache cleared successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear ExoPlayer cache: " + e.getMessage(), e);
        }
    }


    public void initAgora(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        try {
            mRtcEngine = RtcEngine.create(getApplicationContext(), sessionManager.getSetting().getAgoraKey(), mHandler);
            mRtcEngine.setLogFile(TempUtil.initializeLogFile(this));
            mRtcEngine.setDefaultAudioRoutetoSpeakerphone(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "initAgora: initializing ===== ");
        SharedPreferences pref = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        mGlobalConfig.setVideoDimenIndex(pref.getInt(
                Constants.PREF_RESOLUTION_IDX, Constants.DEFAULT_PROFILE_IDX));

        boolean showStats = pref.getBoolean(Constants.PREF_ENABLE_STATS, false);
        mGlobalConfig.setIfShowVideoStats(showStats);
        mStatsManager.enableStats(showStats);

        mGlobalConfig.setMirrorLocalIndex(pref.getInt(Constants.PREF_MIRROR_LOCAL, 0));
        mGlobalConfig.setMirrorRemoteIndex(pref.getInt(Constants.PREF_MIRROR_REMOTE, 0));
        mGlobalConfig.setMirrorEncodeIndex(pref.getInt(Constants.PREF_MIRROR_ENCODE, 0));

        AudioMixingController.getInstance().init(context,mRtcEngine);
    }

    public StatsManager statsManager() {
        return mStatsManager;
    }

    public RtcEngine rtcEngine() {
        return mRtcEngine;
    }

    public void registerEventHandler(EventHandler handler) {
        mHandler.addHandler(handler);
    }

    public void removeEventHandler(EventHandler handler) {
        mHandler.removeHandler(handler);
    }

    public EngineConfig engineConfig() {
        return mGlobalConfig;
    }

    public void initGlobalSocket() {
        Log.d(TAG, "initGlobalSocket: 106");

        if (MySocketManager.getInstance().globalConnecting) {
            Log.d(TAG, "initGlobalSocket: already connecting... global socket .........");
            return;
        }
        MySocketManager.getInstance().createGlobal(getApplicationContext());
    }

    public class AppLifecycleObserver implements DefaultLifecycleObserver {
        @Override
        public void onResume(@NonNull LifecycleOwner owner) {
            //Log.e(TAG, "onResume: GLOBAL SOCKET "+socket.isActive());
            try {
                if (sessionManager.getUser() == null) {
                    Log.d(TAG, "onResume: not logged yet");
                    return;
                }

                if (MySocketManager.getInstance().getSocket() == null || !MySocketManager.getInstance().getSocket().connected()) {
                    MySocketManager.getInstance().createGlobal(getApplicationContext());
                    MySocketManager.getInstance().globalConnecting = true;

                    Intent intent = new Intent();
                    intent.setAction("com.ttyo.ONLINE");
                    intent.putExtra("from", "mainapp");
                    sendBroadcast(intent);
                }
                Log.d(TAG, "onResume: ");
            } catch (Exception e) {
                Log.d(TAG, "onResume: err " + e.toString());
            }

        }

        @Override
        public void onPause(@NonNull LifecycleOwner owner) {
            Log.e(TAG, "onPause: ");
        }

        @Override
        public void onDestroy(@NonNull LifecycleOwner owner) {
            DefaultLifecycleObserver.super.onDestroy(owner);
            if (MySocketManager.getInstance().getSocket() != null) {
                Log.d(TAG, "onDestroy: ");

                String userId = "";

                if (sessionManager.getUser().getId() != null) {
                    userId = sessionManager.getUser().getId();
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("userId", userId);
                    Log.d(TAG, "onPause: manual1");
                    MySocketManager.getInstance().getSocket().emit("manualDisconnect", jsonObject);

                    Intent intent1 = new Intent();
                    intent1.setAction("com.ttyo.OFFLINE");
                    intent1.putExtra("from", "mainapp");
                    sendBroadcast(intent1);

                    Log.d(TAG, "onPause: manual ");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                MySocketManager.getInstance().getSocket().disconnect();
            }

        }
    }
}





