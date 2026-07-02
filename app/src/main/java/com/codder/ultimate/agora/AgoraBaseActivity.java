package com.codder.ultimate.agora;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.MainApplication;
import com.codder.ultimate.R;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.activity.BaseActivity;
import com.codder.ultimate.agora.rtc.Constants;
import com.codder.ultimate.agora.rtc.EngineConfig;
import com.codder.ultimate.agora.rtc.EventHandler;
import com.codder.ultimate.agora.stats.StatsManager;
import com.codder.ultimate.popups.PopupBuilder;
import com.codder.ultimate.retrofit.Const;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;

import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.client.SocketOptionBuilder;
import io.socket.engineio.client.transports.Polling;
import io.socket.engineio.client.transports.WebSocket;

public abstract class AgoraBaseActivity extends BaseActivity implements EventHandler {
    private static final String TAG = "AgoraBaseActivity";
    SessionManager sessionManager;

    private Socket socket;
    private MediaPlayer player2;
    public AudioManager audioManager;

    public Socket getSocket() {
        return socket;
    }

    private BroadcastReceiver wiredHeadsetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.d(TAG, "Wired headset disconnected");
                        configureAudioRouting(rtcEngine()); // Re-route audio
                        break;
                    case 1:
                        Log.d(TAG, "Wired headset connected");
                        configureAudioRouting(rtcEngine()); // Re-route audio
                        break;
                    default:
                        break;
                }
            }
        }
    };
    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.d(TAG, "Bluetooth connected");
                configureAudioRouting(rtcEngine()); // Re-route audio
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.d(TAG, "Bluetooth disconnected");
                configureAudioRouting(rtcEngine()); // Re-route audio
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        registerRtcEventHandler(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//        audioManager.setBluetoothScoOn(true);
//        audioManager.startBluetoothSco();

        IntentFilter filter = new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(wiredHeadsetReceiver, filter);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter2.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothReceiver, filter2);
    }

    private boolean isBluetoothConnected() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return false;
            }
            int a2dp = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
            int headset = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
            return (a2dp == BluetoothProfile.STATE_CONNECTED || headset == BluetoothProfile.STATE_CONNECTED);
        }
        return false;
    }

    private boolean isWiredHeadsetConnected() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        return audioManager.isWiredHeadsetOn();
    }

    // Call this method after initializing the Agora engine
//    public void configureAudioRouting(RtcEngine rtcEngine) {
//        if (isBluetoothConnected()) {
//            Log.d(TAG, "Bluetooth headset connected, routing audio to Bluetooth");
//            // Let Agora handle Bluetooth routing automatically
//            // No need to change audio route manually, Bluetooth takes precedence
//
//        } else if (isWiredHeadsetConnected()) {
//            Log.d(TAG, "Wired headset connected, routing audio to wired headset");
//            // Wired headsets automatically take precedence
//        } else {
//            // No external device connected, decide based on preference
//            Log.d(TAG, "No external device connected, routing to speaker");
//            // Route audio to speakerphone
//            if (rtcEngine() != null) {
//                rtcEngine().setDefaultAudioRoutetoSpeakerphone(true);
//                rtcEngine().setEnableSpeakerphone(true);
//            }
//        }
//    }

    public void configureAudioRouting(RtcEngine rtcEngine) {
        if (isBluetoothConnected()) {
            audioManager.setBluetoothScoOn(true);
            audioManager.startBluetoothSco();

            if (rtcEngine != null) {
                rtcEngine.setEnableSpeakerphone(false);
            }

        } else if (isWiredHeadsetConnected()) {
            audioManager.setSpeakerphoneOn(false);

            if (rtcEngine != null) {
                rtcEngine.setEnableSpeakerphone(false);
            }

        } else {
            audioManager.setSpeakerphoneOn(true);

            if (rtcEngine != null) {
                rtcEngine.setEnableSpeakerphone(true);
            }
        }
    }

    public void initSoketIo(String tkn, boolean ishost) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("liveRoom", tkn);
            if (ishost) {
                jsonObject.put("liveHostRoom", sessionManager.getUser().getId());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "initSoketIo: live rid " + tkn);
        SocketOptionBuilder optionsBuilder = IO.Options.builder()
                // IO factory options
                .setForceNew(false)
                .setMultiplex(true)

                // low-level engine options
                .setTransports(new String[]{Polling.NAME, WebSocket.NAME})
                .setUpgrade(true)
                .setRememberUpgrade(false)
                .setPath("/socket.io/")
                .setQuery("obj=" + jsonObject.toString() + "")
                .setExtraHeaders(null)
                // Manager options
                .setReconnection(true)
                .setReconnectionAttempts(Integer.MAX_VALUE)
                .setReconnectionDelay(1_000)
                .setReconnectionDelayMax(5_000)
                .setRandomizationFactor(0.5)
                .setTimeout(20_000)
                // Socket options
                .setAuth(null);

        Log.d(TAG, "initSoketIo: ");
       /* if (ishost) {
            optionsBuilder.setQuery("liveHostRoom=" + tkn + "");
        }*/
        //   optionsBuilder.setQuery("liveRoom=" + tkn + "");
        IO.Options options = optionsBuilder.build();

        URI uri = URI.create(BuildConfig.BASE_URL);
        socket = IO.socket(uri, options);
        Log.d("TAG", "onCreate: " + socket.id());
        socket.connect();
        Log.d("TAG", "soket: " + socket.connected());
        socket.on("connection", args -> {
            Log.d("TAG", "onCreate: socket ");
        });
        socket.on(Socket.EVENT_CONNECT, args -> {
            Log.d("TAG", "initSoketIo: connect ");
            socket.on(Const.EVENT_PK_END, args1 -> {
                Log.d(TAG, "initSoketIo: pk end listener");
            });
        });
    }

    public void makeSound() {
        if (player2 != null) {
            player2.release();
            player2 = null;
        }
        try {
            player2 = new MediaPlayer();
            try {
                AssetFileDescriptor afd2 = getAssets().openFd("pop.mp3");
                player2.setDataSource(afd2.getFileDescriptor(), afd2.getStartOffset(), afd2.getLength());
                player2.prepare();
                player2.start();
            } catch (IOException e) {
                Log.d(TAG, "initUI: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "initUI: errrr " + e.getMessage());
        }
    }


    protected MainApplication application() {
        return (MainApplication) getApplication();
    }

    protected StatsManager statsManager() {
        return application().statsManager();
    }

    protected RtcEngine rtcEngine() {
        return application().rtcEngine();
    }

    public void configVideo() {
        VideoEncoderConfiguration configuration = new VideoEncoderConfiguration(
                Constants.VIDEO_DIMENSIONS[config().getVideoDimenIndex()],
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        );
        configuration.mirrorMode = VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_ENABLED;
        if (rtcEngine() != null) {
            rtcEngine().setVideoEncoderConfiguration(configuration);
        }
    }


    protected EngineConfig config() {
        return application().engineConfig();
    }

    protected void removeRtcVideo(int uid, boolean local) {
        if (rtcEngine() != null) {
            if (local) {
                rtcEngine().setupLocalVideo(null);
            } else {
                rtcEngine().setupRemoteVideo(new VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, uid));
            }
        }

    }

    public boolean checkOverlayDisplayPermission() {
        return Settings.canDrawOverlays(this);
    }

    public void requestOverlayDisplayPermission() {

        new PopupBuilder(this).showReliteDiscardPopup(R.drawable.vector_info, getString(R.string.screen_overlay_permission_needed), getString(R.string.enable_display_over_other_apps_from_system_settings), "Open Settings", "cancel", new PopupBuilder.OnPopupClickListener() {
            @Override
            public void onClickContinue() {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, RESULT_OK);

//                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                intent.setData(Uri.parse("package:" + getPackageName()));
//                startActivity(intent);

//                Intent intent = new Intent(
//                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                        Uri.parse("package:" + getPackageName())
//                );
//                startActivity(intent);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(wiredHeadsetReceiver);
        unregisterReceiver(bluetoothReceiver);

        removeRtcEventHandler(this);
    }

}
