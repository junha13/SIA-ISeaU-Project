package com.lx.iseau

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent // ✅ 클릭 시 앱 열기를 위해 추가
import android.content.Context
import android.content.Intent // ✅ 클릭 시 앱 열기를 위해 추가
import android.media.AudioAttributes
import android.net.Uri
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




    private fun sendNotification(title: String, messageBody: String) {
        // 소리를 적용하려면 채널 ID를 변경해야 합니다. (기존 앱 설치자는 v2로 변경 시 적용됨)
        val channelId = "safety_channel_v2"
        val channelName = "긴급 재난 알림"

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // 1. 커스텀 사운드 파일 설정 (res/raw/alram.mp3)
        val soundUri = Uri.parse("android.resource://$packageName/${R.raw.alram}")

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 2. 안드로이드 8.0 (Oreo) 이상: 채널 생성 및 소리 연결
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM) // 알람 용도로 설정
                .build()

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "위험 감지 및 거리 이탈 알림"
                enableLights(true)
                enableVibration(true)
                setSound(soundUri, audioAttributes) // ★ 핵심: 소리 연결
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 3. 알림 빌더 설정
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.iseau)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(soundUri) // 오레오 미만 버전을 위한 설정
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}