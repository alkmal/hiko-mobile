package com.codder.ultimate.agora.rtc;

import java.util.ArrayList;

import io.agora.rtc2.IRtcEngineEventHandler;


public class AgoraEventHandler extends IRtcEngineEventHandler {
    private ArrayList<EventHandler> mHandler = new ArrayList<>();

    public void addHandler(EventHandler handler) {
        mHandler.add(handler);
    }

    public void removeHandler(EventHandler handler) {
        mHandler.remove(handler);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        for (EventHandler handler : mHandler) {
            handler.onJoinChannelSuccess(channel, uid, elapsed);
        }
    }

    @Override
    public void onRequestToken() {
        super.onRequestToken();
        for (EventHandler handler : mHandler) {
            handler.onRequestToken();
        }
    }

    @Override
    public void onTokenPrivilegeWillExpire(String token) {
        super.onTokenPrivilegeWillExpire(token);
        for (EventHandler handler : mHandler) {
            handler.onTokenPrivilegeWillExpire(token);
        }
    }

    @Override
    public void onChannelMediaRelayStateChanged(int state, int code) {
        for (EventHandler handler : mHandler) {
            handler.onChannelMediaRelayStateChanged(state, code);
        }
    }

    @Override
    public void onLeaveChannel(RtcStats stats) {
        for (EventHandler handler : mHandler) {
            handler.onLeaveChannel(stats);
        }
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        for (EventHandler handler : mHandler) {
            handler.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
        }
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        for (EventHandler handler : mHandler) {
            handler.onUserJoined(uid, elapsed);
        }
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        for (EventHandler handler : mHandler) {
            handler.onUserOffline(uid, reason);
        }
    }

    @Override
    public void onRtcStats(RtcStats stats) {
        for (EventHandler handler : mHandler) {
            handler.onRtcStats(stats);
        }
    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        for (EventHandler handler : mHandler) {
            handler.onNetworkQuality(uid, txQuality, rxQuality);
        }
    }

    @Override
    public void onAudioRouteChanged(int routing) {
        for (EventHandler handler : mHandler) {
            handler.onAudioRouteChanged(routing);
        }
    }

    @Override
    public void onRemoteVideoStats(RemoteVideoStats stats) {
        for (EventHandler handler : mHandler) {
            handler.onRemoteVideoStats(stats);
        }
    }

    @Override
    public void onRemoteAudioStats(RemoteAudioStats stats) {
        for (EventHandler handler : mHandler) {
            handler.onRemoteAudioStats(stats);
        }
    }

    @Override
    public void onLastmileQuality(int quality) {
        for (EventHandler handler : mHandler) {
            handler.onLastmileQuality(quality);
        }
    }

    @Override
    public void onLastmileProbeResult(LastmileProbeResult result) {
        for (EventHandler handler : mHandler) {
            handler.onLastmileProbeResult(result);
        }
    }

    @Override
    public void onError(int err) {
        for (EventHandler handler : mHandler) {
            handler.onErr(err);
        }

    }

    @Override
    public void onConnectionLost() {
        for (EventHandler handler : mHandler) {
            handler.onConnectionLost();
        }

    }

    @Override
    public void onVideoStopped() {
        for (EventHandler handler : mHandler) {
            handler.onVideoStopped();
        }

    }

    @Override
    public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
        super.onAudioVolumeIndication(speakers, totalVolume);
        for (EventHandler handler : mHandler) {
            handler.onAudioVolumeIndication( speakers, totalVolume);
        }
    }

}
