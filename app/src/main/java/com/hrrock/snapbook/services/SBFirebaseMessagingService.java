package com.hrrock.snapbook.services;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hrrock.snapbook.activities.NotificationActivity;
import com.hrrock.snapbook.utils.NotificationUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class SBFirebaseMessagingService extends FirebaseMessagingService {
    private NotificationUtils notificationUtils;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0) {
            try {
                JSONObject jsonObject = new JSONObject(remoteMessage.getData().toString());
                handleNotification(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleNotification(JSONObject jsonObject) {
        JSONObject data = jsonObject.optJSONObject("data");
        String title = data.optString("title");
        String message = data.optString("message");
        String imageUrl = data.optString("image");
        String timestamp = data.optString("timestamp");
        JSONObject payload = data.optJSONObject("payload");

        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent("notification");
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        } else {
            // app is in background, show the notification in notification tray
            Intent resultIntent = new Intent(getApplicationContext(), NotificationActivity.class);
            resultIntent.putExtra("message", message);

            showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }
}
