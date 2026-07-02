package com.codder.ultimate.notification;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.codder.ultimate.chat.activity.CallIncomeActivity;
import com.codder.ultimate.retrofit.Const;

public class CallNotificationActionReceiver extends BroadcastReceiver {

    private static final String TAG = "CallNotificationReceiver";
    private static final String ACTION_RECEIVE_CALL = "RECEIVE_CALL";
    private static final String ACTION_DIALOG_CALL = "DIALOG_CALL";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getExtras() == null) {
            Log.w(TAG, "Intent or extras is null. Aborting call action handling.");
            return;
        }

        String action = intent.getStringExtra("ACTION_TYPE");
        if (action == null || action.isEmpty()) {
            Log.w(TAG, "Action type is null or empty.");
            return;
        }

        performClickAction(context, action, intent);

        // Dismiss system dialogs and stop service
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        try {
            context.stopService(new Intent(context, HeadsUpNotificationService.class));
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop HeadsUpNotificationService", e);
        }
    }

    private void performClickAction(Context context, String action, Intent intent) {
        String callData = intent.getStringExtra(Const.DATA);
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", 0);

        try {
            if (ACTION_RECEIVE_CALL.equalsIgnoreCase(action)) {
                if (hasNecessaryPermissions(context)) {
                    launchCallActivity(context, callData, true);
                } else {
                    Log.w(TAG, "Missing required permissions. Cannot accept call.");
                }

            } else if (ACTION_DIALOG_CALL.equalsIgnoreCase(action)) {
                launchCallActivity(context, callData, false);

            } else {
                Log.d(TAG, "Unhandled action: " + action);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling click action", e);
        } finally {
            clearNotifications(context, notificationId);
        }
    }

    private void launchCallActivity(Context context, String data, boolean isAcceptClick) {
        Intent callIntent = new Intent(context.getApplicationContext(), CallIncomeActivity.class);
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        callIntent.putExtra(Const.DATA, data);
        if (isAcceptClick) {
            callIntent.putExtra(Const.IS_ACCEPT_CLICK, true);
        }
        context.getApplicationContext().startActivity(callIntent);
        Log.d(TAG, "Call activity launched: AcceptClick=" + isAcceptClick);
    }

    private void clearNotifications(Context context, int notificationId) {
        try {
            NotificationManager manager = (NotificationManager) context.getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                if (notificationId != 0) {
                    manager.cancel(notificationId);
                }
                manager.cancelAll();
            } else {
                Log.w(TAG, "NotificationManager is null.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling notifications", e);
        }
    }

    private boolean hasNecessaryPermissions(Context context) {
        return hasPermission(context, Manifest.permission.CAMERA)
                && hasPermission(context, Manifest.permission.RECORD_AUDIO);
    }

    private boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
