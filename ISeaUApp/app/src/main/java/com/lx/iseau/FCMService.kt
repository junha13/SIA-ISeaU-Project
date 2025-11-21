package com.lx.iseau

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent // ✅ 클릭 시 앱 열기를 위해 추가
import android.content.Context
import android.content.Intent // ✅ 클릭 시 앱 열기를 위해 추가
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

        // 1. 데이터 페이로드 확인 (백그라운드 처리용)
        remoteMessage.data.let {
            if (it.isNotEmpty()) {
                // 서버에서 보낸 "title"이나 "body" 키가 있으면 우선 사용
                val title = it["title"] ?: "긴급 안전 경고"
                val body = it["body"] ?: it["alert_message"] ?: "위험 상황이 감지되었습니다."
                sendNotification(title, body)
            }
        }

        // 2. 알림 페이로드 확인 (포그라운드 알림용)
        remoteMessage.notification?.let {
            sendNotification(it.title ?: "긴급 안전 경고", it.body ?: "내용 없음")
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        // 토큰 갱신은 MainActivity 진입 시 처리되므로 여기선 로그만 남김
    }

    private fun sendNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 알림 채널 생성 (오레오 이상 필수)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "위험 감지 및 거리 이탈 알림"
                enableVibration(true) // 진동 켜기
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 클릭 시 앱(MainActivity) 열기
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.iseau)
            .setContentTitle(title) // 서버에서 보낸 제목 (예: "⚠️ 그룹 이탈 알림")
            .setContentText(body)   // 서버에서 보낸 내용 (예: "김철수님이 200m 이상 멀어졌습니다.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // 클릭 이벤트 연결

        val notificationId = System.currentTimeMillis().toInt()

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}