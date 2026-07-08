package com.codder.ultimate.socket;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.codder.ultimate.BuildConfig;
import com.codder.ultimate.SessionManager;
import com.codder.ultimate.live.utils.LiveHandler;
import com.codder.ultimate.retrofit.Const;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.WebSocket;

public class MySocketManager {
    private static final String TAG = "SocketManager";
    public static int listenerCount = 0;
    private SessionManager sessionManager;
    Handler handler = new Handler();
    private Socket socket;
    List<LiveHandler> liveHandlerList = new ArrayList<>();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (socket != null) {
                Log.d(TAG, "run: SOCKET CONNECT = " + socket.connected());
            }
            handler.postDelayed(this, 3000);
        }
    };
    private String userId;
    List<SocketConnectHandler> socketConnectHandlerList = new ArrayList<>();
    List<ChatHandler> chatHandlerList = new ArrayList<>();
    public boolean lastCallCancelled = false;
    public boolean globalConnecting = false;
    public boolean globalConnected = false;
    List<CallHandler> callHandlerList = new ArrayList<>();
    List<AudioRoomHandler> audioRoomHandlerList = new ArrayList<>();

    public MySocketManager() {

    }

    public static MySocketManager getInstance() {
        return Holder.INSTANCE;
    }

    public void createGlobal(Context applicationContext) {

        if (MySocketManager.getInstance().getSocket() != null) {
            if (MySocketManager.getInstance().getSocket().connected()) {
                return;
            }
        }

        Log.d(TAG, "createGlobal: ");

        sessionManager = new SessionManager(applicationContext);

        if (sessionManager.getUser() == null) {
            Log.d(TAG, "createGlobal: not Logged yet");
            return;
        }

        userId = sessionManager.getUser().getId();

        Log.d(TAG, "initGlobalSocket: init " + userId);
        IO.Options options = IO.Options.builder()
                .setForceNew(false)
                .setMultiplex(true)
                .setTransports(new String[]{WebSocket.NAME})
                .setUpgrade(false)
                .setRememberUpgrade(false)
                .setPath("/socket.io/")
                .setQuery("globalRoom=" + "globalRoom:" + userId)
                .setExtraHeaders(null)
                .setReconnection(true)
                .setReconnectionAttempts(Integer.MAX_VALUE)
                .setReconnectionDelay(1000)
                .setReconnectionDelayMax(5000)
                .setRandomizationFactor(0.5)
                .setAuth(null)
                .build();

        URI uri = URI.create(BuildConfig.BASE_URL);
        socket = IO.socket(uri, options);
        socket.connect();

        Log.d(TAG, "createGlobal: SSS97  " + socket.connected());

        socket.io().on("reconnect", args1 -> {
            for (SocketConnectHandler connectHandler : socketConnectHandlerList) {
                connectHandler.onReconnected(args1);
            }
            Log.d(TAG, "reconnected: 111   listener count>> " + listenerCount);
            Intent intent = new Intent();
            intent.setAction("com.ttyo.ONLINE");
            intent.putExtra("from", "socketmanager_reconnnect");
            applicationContext.sendBroadcast(intent);

        });
        socket.io().on("reconnection_attempt", args -> {
            Log.d(TAG, "reconnection_attempt :111 ");
            for (SocketConnectHandler connectHandler : socketConnectHandlerList) {
                connectHandler.onReconnecting();
            }
        });
        socket.io().on("reconnected", args1 -> {
            for (SocketConnectHandler connectHandler : socketConnectHandlerList) {
                connectHandler.onReconnected(args1);
            }
            Log.d(TAG, "reconnected: 1111  listener count>> " + listenerCount);
        });

        socket.once(Socket.EVENT_CONNECT, args -> {
            Log.d(TAG, "connected: globalSocket");
            globalConnected = true;
            lastCallCancelled = false;

            for (SocketConnectHandler connectHandler : socketConnectHandlerList) {
                connectHandler.onConnect();
                Log.d(TAG, "createGlobal: onConnect");
            }

            Intent intent = new Intent();
            intent.setAction("com.ttyo.ONLINE");
            intent.putExtra("from", "socketmanager");
            applicationContext.sendBroadcast(intent);

            socket.io().on("reconnect", args1 -> {
                Log.d(TAG, "reconnect: 222   ");
                applicationContext.sendBroadcast(intent);

            });

            socket.io().on("reconnected", args1 -> {
                Log.d(TAG, "reconnected: 222  listener count>> " + listenerCount);
            });

            socket.io().on("reconnection_attempt", args1 -> {
                Log.d(TAG, "reconnection_attempt:222 ");
            });

            socket.on("ping", args1 -> {
                Log.d("ping===", "createGlobal: ping=========  listener count>> " + listenerCount);
                socket.emit("pong", true);

            });

            socket.on(Socket.EVENT_DISCONNECT, args1 -> {
                Log.d(TAG, "createGlobal: event disconnect " + args1[0].toString());
                Log.d(TAG, "createGlobal: event disconnect length>" + args1.length);
                globalConnected = false;
                globalConnecting = false;

                for (SocketConnectHandler connectHandler : socketConnectHandlerList) {
                    connectHandler.onDisconnect();
                    Log.d(TAG, "createGlobal: onDisconnected");
                }

                Intent intent1 = new Intent();
                intent1.setAction("com.ttyo.OFFLINE");
                intent1.putExtra("from", "socketmanager");
                applicationContext.sendBroadcast(intent1);
            });

            socket.on(Const.EVENT_CALL_CONFIRMED, args1 -> {
                List<CallHandler> snapshot = new ArrayList<>(callHandlerList);
                for (CallHandler callHandler : snapshot) {
                    callHandler.onCallConfirm(args1);
                }
            });

            socket.on(Const.EVENT_CALL_ANSWER, args1 -> {
                List<CallHandler> snapshot = new ArrayList<>(callHandlerList);
                for (CallHandler callHandler : snapshot) {
                    callHandler.onCallAnswer(args1);
                }
            });

            socket.on(Const.EVENT_CALL_CANCEL, args1 -> {
                List<CallHandler> snapshot = new ArrayList<>(callHandlerList);
                for (CallHandler callHandler : snapshot) {
                    callHandler.onCallCancel(args1);
                }
            });

            socket.on(Const.EVENT_CHAT_OR_CALL_GIFT_SENT, args1 -> {
                List<CallHandler> snapshot = new ArrayList<>(callHandlerList);
                for (CallHandler callHandler : snapshot) {
                    callHandler.chatOrCallGiftSent(args1);
                }
            });

            socket.on(Const.EVENT_CALL_RECEIVE, args1 -> {
                List<CallHandler> snapshot = new ArrayList<>(callHandlerList);
                for (CallHandler callHandler : snapshot) {
                    callHandler.onCallReceive(args1);
                }
            });

            socket.on(Const.EVENT_CALL_REQUEST, args1 -> {
                Log.d(TAG, "createGlobal: call request");
                List<CallHandler> handlersSnapshot = new ArrayList<>(callHandlerList);
                for (CallHandler callHandler : handlersSnapshot) {
                    callHandler.onCallRequest(args1);
                }
            });

            socket.on(Const.EVENT_CHAT, args1 -> {
                Log.d(TAG, "createGlobal: chat event");
                List<ChatHandler> snapshot = new ArrayList<>(chatHandlerList);
                for (ChatHandler chatHandler : snapshot) {
                    chatHandler.onChat(args1);
                }
            });

            socket.on(Const.EVENT_CHAT_OR_CALL_GIFT_SENT, args1 -> {
                List<ChatHandler> snapshot = new ArrayList<>(chatHandlerList);
                for (ChatHandler chatHandler : snapshot) {
                    chatHandler.chatOrCallGiftSent (args1);
                    Log.d(TAG, "createGlobal: chatOrCallGiftSent ==> " + args1[0].toString());
                }
            });

            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(timerRunnable, 3000);
        });

        socket.on(Const.HOST_DETAILS_FOR_AUDIENCE, args1 -> {
            Log.d(TAG, "createGlobal: HOST_DETAILS_FOR_AUDIENCE  " + args1[0].toString());
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onHostDetailsForAudience(args1);
            }
        });

        socket.on(Const.EVENT_ROOM_WELCOME, args1 -> {
            Log.d(TAG, "createGlobal: HOST_DETAILS_FOR_AUDIENCE  " + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onRoomWelcome(args1);
            }
        });

        socket.on(Const.EVENT_TOTAL_ROOM_COINS, args1 -> {
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onTotalRoomCoins(args1);
            }
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onTotalRoomCoins(args1);
            }
        });

        socket.on(Const.AUDIO_LIVE_HOST_REMOVE, args1 -> {
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onAudioLiveHostRemove(args1);
            }
        });

        socket.on(Const.EVENT_VIEW, args1 -> {
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onView(args1);
            }
            Log.d(TAG, "createGlobal: event view " + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onView(args1);
            }
        });

        socket.on(Const.EVENT_GIFT, args1 -> {
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onGift(args1);
            }
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onGift(args1);
            }
        });

        socket.on(Const.EVENT_CHANGE_THEME, args1 -> {
            Log.d(TAG, "createGlobal: EVENT_CHANGE_THEME");
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onChangeTheme(args1);
            }
        });

        socket.on(Const.EVENT_MUTE_SEAT, args1 -> {
            Log.d(TAG, "createGlobal: EVENT_MUTE_SEAT " + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onMuteSeat(args1);
            }
        });

        socket.on(Const.EVENT_LESS_PARTICIPATED, args1 -> {
            Log.d(TAG, "createGlobal: EVENT_LESS_PARTICIPATED");
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onLessParticipants(args1);
            }
        });

        socket.on(Const.EVENT_ADD_REQUESTED, args1 -> {
            Log.d(TAG, "createGlobal: EVENT_ADD_REQUESTED");
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onAddRequested(args1);
            }
        });

        socket.on(Const.EVENT_ADD_PARTICIPATED, args1 -> {
            Log.d(TAG, "createGlobal: EVENT_ADD_PARTICIPATED");
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onAddParticipants(args1);
            }
        });

        socket.on(Const.EVENT_HOST_JOIN_AUDIO_ROOM, args1 -> {
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onHostEnter(args1);
            }
        });

        socket.on(Const.EVENT_SEND_REACTION, args1 -> {
            Log.d(TAG, "createGlobal: roomImage  " + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onReactionReceived(args1);
            }
        });

        socket.on(Const.USER_COIN_UPDATE, args1 -> {
            Log.d(TAG, "createGlobal: USER_COIN_UPDATE  " + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onUserCoinUpdate(args1);
            }
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onUserCoinUpdate(args1);
            }
        });

        socket.on(Const.EVENT_GET_USER, args1 -> {
            Log.d(TAG, "createGlobal: EVENT_SEAT get user");
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onGetUser(args1);
            }

        });
        socket.on(Const.EVENT_GET_USER_2, args1 -> {
            Log.d(TAG, "createGlobal: EVENT_SEAT 2 get user");
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onGetUser2(args1);
            }
        });

        socket.on(Const.EVENT_LOCK_SEAT, args1 -> {
            Log.d(TAG, "createGlobal: EVENT_LOCK_SEAT");
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onLockSeat(args1);
            }
        });

        socket.on(Const.HOST_LIVE_END, args1 -> {
            Log.d(TAG, "createGlobal: HOST_LIVE_END  " + args1[0].toString());
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onHostLiveEnd(args1);
            }
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onLiveEnd(args1);
            }
        });

        socket.on(Const.LIVE_END_BY_END, args1 -> {
            Log.d(TAG, "createGlobal: live end by end " + args1[0].toString());
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onLiveEndByEnd(args1);
            }
            Log.d(TAG, "createGlobal: chat event");
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onLiveEndByEnd(args1);
            }
        });

        socket.on(Const.EVENT_PK_START, args1 -> {
            Log.d(TAG, "createGlobal: EVENT_PK_START  " + args1[0].toString());
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onPkRequestAnswer(args1);
            }
        });

        socket.on(Const.EVENT_PK_END, args1 -> {
            Log.d(TAG, "createGlobal: EVENT_PK_END  " + args1[0].toString());
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onPkEnd(args1);
            }
        });

        socket.on(Const.EVENT_UPDATE_BLOCKED_LIST, args1 -> {
            Log.d(TAG, "createGlobal: event banned " + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onBanned(args1);
            }

            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onBanned(args1);
            }

        });

        socket.on(Const.EVENT_BLOCKED_LIST_UPDATED, args1 -> {

            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onBannedUserList(args1);
            }

            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onBannedUserList(args1);
            }
        });


        socket.on(Const.EVENT_COMMENT, args1 -> {
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onComment(args1);
            }
            Log.d(TAG, "createGlobal: event comment " + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onComment(args1);
            }
        });

        socket.on(Const.EVENT_SEAT, args1 -> {
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onSeat(args1);
            }
        });

        socket.on(Const.EVENT_INVITE, args1 -> {
            Log.d(TAG, "createGlobal: EVENT_SEAT");
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onInvite(args1);
            }
        });

        socket.on(Const.EVENT_ROOM_NAME, args1 -> {
            Log.d(TAG, "createGlobal: EVENT_ROOM_NAME  " + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onRoomNameChange(args1);
            }
        });

        socket.on("roomImage", args1 -> {
            Log.d(TAG, "createGlobal: EVENT_ROOM_WELCOME  " + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onRoomImageChange(args1);
            }
        });

        socket.on(Const.EVENT_BLOCK_USER_ALERT, args1 -> {
            Log.d(TAG, "createGlobal: EVENT_BLOCK_USER_ALERT " + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onBlockUserAlert(args1);
            }

            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onBlockUserAlert(args1);
            }
        });

        socket.on(Const.EVENT_BLOCK, args1 -> {
            Log.d(TAG, "createGlobal: event block " + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onBlock(args1);
            }
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onBlock(args1);
            }
        });

        socket.on(Const.EVENT_ANIM_FILTER, args1 -> {
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onAnimationFilter(args1);
            }
            Log.d(TAG, "createGlobal: event filter " + args1[0].toString());
        });

        socket.on(Const.EVENT_SIMPLE_FILTER, args1 -> {
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onSimpleFilter(args1);
            }
            Log.d(TAG, "createGlobal: simple filter " + args1[0].toString());
        });

        socket.on(Const.EVENT_GIF, args1 -> {
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onGif(args1);
            }
            Log.d(TAG, "createGlobal: event gif  " + args1[0].toString());
        });

        socket.on(Const.EVENT_PK_REQUEST, args1 -> {
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onPkRequest(args1);
            }
        });

        socket.on("dummy", args1 -> {
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onSingleLiveUser(args1);
            }
        });

        socket.on("data", args1 -> {
            Log.d(TAG, "createGlobal: EVENT_GET_USER  == dummy listener");
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onGetUser(args1);
            }
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onGetUser(args1);
            }
        });

        socket.on(Const.EVENT_PK_REQUEST_ANSWER, args1 -> {
            Log.d(TAG, "createGlobal: EVENT_PK_REQUEST_ANSWER  " + args1[0].toString());
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onPkRequestAnswer(args1);
            }
        });

        socket.on(Const.EVENT_PK_CONTINUE_PK, args1 -> {
            Log.d(TAG, "createGlobal: EVENT_PK_CONTINUE_PK  " + args1[0].toString());
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onPkRequestAnswer(args1);
            }
        });

        socket.on(Const.EVENT_HOST_JOIN_AUDIO_ROOM, args1 -> {
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onHostEnter(args1);
            }
        });


        socket.on(Const.HIGH_VALUE_GIFT_RECEIVE, args1 -> {
            Log.d(TAG, "createGlobal: HIGH_VALUE_GIFT_RECEIVE" + args1[0].toString());
            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onBroadcastNotification(args1);
            }
            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onBroadcastNotification(args1);
            }
        });

        socket.on("highBit", args1 -> {

            Log.d(TAG, "highBit event listen..." + args1[0].toString());

            for (LiveHandler liveHandler : liveHandlerList) {
                liveHandler.onGame(args1);
            }

            for (AudioRoomHandler audioRoomHandler : audioRoomHandlerList) {
                audioRoomHandler.onGame(args1);
            }

        });


    }

    public Socket getSocket() {
        return socket;
    }

    public void addSocketConnectHandler(SocketConnectHandler socketConnectHandler) {
        socketConnectHandlerList.add(socketConnectHandler);
    }

    public void removeSocketConnectHandler(SocketConnectHandler socketConnectHandler) {
        socketConnectHandlerList.remove(socketConnectHandler);
    }

    public void addLiveHandler(LiveHandler liveHandler) {
        liveHandlerList.add(liveHandler);
    }

    public void removeLiveHandler(LiveHandler liveHandler) {
        liveHandlerList.remove(liveHandler);
    }

    public void addCallHandler(CallHandler callHandler) {
        callHandlerList.add(callHandler);
    }

    public void addChatHandler(ChatHandler chatHandler) {
        chatHandlerList.add(chatHandler);
    }

    public void removeChatHandler(ChatHandler chatHandler) {
        chatHandlerList.remove(chatHandler);
    }

    public void removeCallHandler(CallHandler callHandler) {
        callHandlerList.remove(callHandler);
    }

    public void addAudioRoomHandler(AudioRoomHandler audioRoomHandler) {
        audioRoomHandlerList.add(audioRoomHandler);
    }

    public void removeAudioRoomHandler(AudioRoomHandler audioRoomHandler) {
        audioRoomHandlerList.remove(audioRoomHandler);
    }

    private static final class Holder {
        private static final MySocketManager INSTANCE = new MySocketManager();
    }
}
