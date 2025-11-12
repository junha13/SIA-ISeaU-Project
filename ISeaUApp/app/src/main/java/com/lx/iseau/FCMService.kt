package com.lx.iseau

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {

    private val TAG = "ISeaU_FCM"
    private val CHANNEL_ID = "ISeaU_DANGER_CHANNEL"
    private val CHANNEL_NAME = "긴급 안전 경고"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // 1. 데이터 페이로드 확인
        remoteMessage.data.let {
            if (it.isNotEmpty()) {
                val alertBody = it["alert_message"] ?: "워치에서 위험 상황이 감지되었습니다."
                sendNotification("긴급 안전 경고", alertBody)
            }
        }

        // 2. 알림 페이로드 확인
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "긴급 안전 경고", it.body ?: "워치에서 위험 상황이 감지되었습니다.")
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // TODO: 서버에 새로운 토큰을 전송하여 업데이트합니다.
    }

    private fun sendNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(0, notificationBuilder.build())
    }
}