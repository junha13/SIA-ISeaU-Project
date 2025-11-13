package com.lx.iseau.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.MeasureClient
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.SampleDataPoint
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


/**
 * ✅ 워치에서 실시간 심박(heart_rate) 수집 → 서버로 {occurred_at, heart_rate} 전송하는 Foreground 서비스
 * - Health Services API (MeasureClient) 사용
 * - 발생시각은 우선 수신 시각의 UTC ISO-8601 사용 (정밀 타임스탬프 동기화는 추후 확장)
 */
class HeartRateUploadService : Service() {

    companion object {
        private const val TAG = "HeartRateUploadService"
        private const val NOTI_CHANNEL_ID = "hr_upload_channel"
        private const val NOTI_ID = 212

        fun start(context: Context) {
            val intent = Intent(context, HeartRateUploadService::class.java)
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent)
            else context.startService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, HeartRateUploadService::class.java))
        }
    }

    private lateinit var measureClient: MeasureClient
    private var registered = false

    private val isoFormatter: DateTimeFormatter =
        DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC)

    private val hrCallback = object : MeasureCallback {
        override fun onAvailabilityChanged(
            dataType: DeltaDataType<*, *>,
            availability: Availability
        ) {
            Log.d(TAG, "Availability $dataType = $availability")
        }

        override fun onDataReceived(data: DataPointContainer) {
            val points = data.getData(DataType.HEART_RATE_BPM)
            for (p in points) {
                val value = (p as? SampleDataPoint<Float>)?.value ?: continue
                val bpm = value.roundToInt()
                val occurredAt = isoFormatter.format(Instant.now())

                // 워차 화면에도 심박 표시
                (application as? ISeaUApp)?.healthViewModel?.updateHeartRate(bpm)

                // ✅ 서버 전송
                AlertSender.sendHeartRateAsync(occurredAt, bpm)
                Log.i(TAG, "HR=$bpm at $occurredAt → sent")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        measureClient = HealthServices.getClient(this).measureClient
        createNotificationChannel()
        startAsForeground()
        register()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregister()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                NOTI_CHANNEL_ID,
                "Heart Rate Upload",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "실시간 심박 업로드용 채널"
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(ch)
        }
    }

    private fun startAsForeground() {
        val noti: Notification = NotificationCompat.Builder(this, NOTI_CHANNEL_ID)
            .setContentTitle("I Sea U — 심박 업로드")
            .setContentText("워치에서 심박을 수집하여 서버로 전송합니다.")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTI_ID, noti, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            startForeground(NOTI_ID, noti)
        }
    }

    private fun register() {
        if (registered) return
        registered = true
        measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, hrCallback)
    }

    private fun unregister() {
        if (!registered) return
        registered = false
        try {
            val future = measureClient.unregisterMeasureCallbackAsync(
                DataType.HEART_RATE_BPM,
                hrCallback   // 등록 때 쓴 같은 인스턴스여야 함
            )
            // 완료 로그(선택)
            future.addListener({
                Log.i(TAG, "HR callback unregistered")
            }, ContextCompat.getMainExecutor(this))
        } catch (_: Throwable) {
        }
    }
}