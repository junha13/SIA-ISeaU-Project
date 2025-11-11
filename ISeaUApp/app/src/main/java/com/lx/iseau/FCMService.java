package com.lx.iseau;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FCMService extends FirebaseMessagingService {

    private static final String TAG = "ISeaU_FCM_Java";
    private static final String CHANNEL_ID = "ISeaU_DANGER_CHANNEL";
    private static final String CHANNEL_NAME = "긴급 안전 경고";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // 1. 데이터 페이로드 확인
        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0) {
            String alertBody = data.get("alert_message");
            if (alertBody == null) {
                alertBody = "워치에서 위험 상황이 감지되었습니다.";
            }
            sendNotification("긴급 안전 경고", alertBody);
        }

        // 2. 알림 페이로드 확인
        if (remoteMessage.getNotification() != null) {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            String title = notification.getTitle() != null ? notification.getTitle() : "긴급 안전 경고";
            String body = notification.getBody() != null ? notification.getBody() : "워치에서 위험 상황이 감지되었습니다.";
            sendNotification(title, body);
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        // TODO: 서버에 새로운 토큰을 전송하여 업데이트합니다.
    }

    private void sendNotification(String title, String body) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8.0 (Oreo) 이상에서는 Notification Channel 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(0, notificationBuilder.build());
    }
}