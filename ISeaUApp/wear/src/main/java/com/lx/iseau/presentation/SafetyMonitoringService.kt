package com.lx.iseau.presentation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DeltaDataType
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.guava.await
import kotlin.math.roundToInt

class SafetyMonitoringService : Service() {

    private lateinit var dataClient: DataClient
    private lateinit var measureClient: androidx.health.services.client.MeasureClient
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val TAG = "ISeaU_SafetyService"
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "safety_monitoring_channel"

    private val DANGER_HR_LOW = 50.0
    private val DANGER_HR_HIGH = 150.0

    private var lastHeartRate: Double? = null
    private var isMonitoring = false

    private val heartRateCallback = object : MeasureCallback {
        override fun onAvailabilityChanged(
            dataType: DeltaDataType<*, *>,
            availability: Availability
        ) {
            Log.d(TAG, "Heart Rate availability: $availability")
        }

        override fun onDataReceived(data: DataPointContainer) {
            val heartRateList = data.getData(DataType.HEART_RATE_BPM)

            heartRateList.forEach { dataPoint ->
                val heartRate = dataPoint.value
                lastHeartRate = heartRate

                Log.d(TAG, "❤️ Heart Rate: ${heartRate.roundToInt()} BPM")
                checkSafetyLogic()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        dataClient = Wearable.getDataClient(this)
        measureClient = HealthServices.getClient(this).measureClient

        Log.d(TAG, "SafetyMonitoringService created.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "SafetyMonitoringService started.")

        // ✅ API 레벨에 따라 분기 처리
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                // API 34+: FOREGROUND_SERVICE_TYPE_HEALTH 사용
                startForeground(
                    NOTIFICATION_ID,
                    createNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // API 29-33: foregroundServiceType 없이 시작
                startForeground(NOTIFICATION_ID, createNotification())
            }
            else -> {
                // API 28 이하
                startForeground(NOTIFICATION_ID, createNotification())
            }
        }

        if (hasRequiredPermissions()) {
            subscribeToHeartRate()
        } else {
            Log.e(TAG, "❌ Required permissions not granted!")
            stopSelf()
        }

        return START_STICKY
    }

    private fun hasRequiredPermissions(): Boolean {
        val permissions = arrayOf(
            android.Manifest.permission.BODY_SENSORS,
            android.Manifest.permission.ACTIVITY_RECOGNITION
        )

        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun subscribeToHeartRate() {
        if (isMonitoring) {
            Log.d(TAG, "Already monitoring heart rate.")
            return
        }

        serviceScope.launch {
            try {
                // ✅ guava의 await() 사용
                val capabilities = measureClient.getCapabilitiesAsync().await()

                // ✅ 올바른 속성 이름 사용
                if (DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure) {
                    measureClient.registerMeasureCallback(
                        DataType.HEART_RATE_BPM,
                        heartRateCallback
                    )

                    isMonitoring = true
                    Log.d(TAG, "✅ Heart Rate monitoring started!")

                } else {
                    Log.e(TAG, "❌ Heart Rate sensor not supported!")
                    stopSelf()
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to start: ${e.message}", e)
                stopSelf()
            }
        }
    }

    private fun checkSafetyLogic() {
        val hr = lastHeartRate ?: return

        val isLowHR = hr < DANGER_HR_LOW
        val isHighHR = hr > DANGER_HR_HIGH

        if (isLowHR || isHighHR) {
            val hrDisplay = hr.roundToInt()
            val status = if (isLowHR) "너무 낮음" else "너무 높음"

            val message = "🚨 긴급: 심박수 이상! ${hrDisplay} BPM ($status)"
            Log.e(TAG, message)

            sendAlertToMobile("DANGER_ALERT", message)
        }
    }

    private fun sendAlertToMobile(path: String, message: String) {
        val putDataReq = PutDataMapRequest.create("/$path").apply {
            dataMap.putString("alert_message", message)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }

        val request = putDataReq.asPutDataRequest()

        dataClient.putDataItem(request)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Alert sent to mobile: $message")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to send alert: ${e.message}", e)
            }
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ISeaU 안전 모니터링")
            .setContentText("심박수를 실시간으로 모니터링 중")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "안전 모니터링",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "백그라운드에서 심박수를 모니터링합니다"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed. Cleaning up...")

        if (isMonitoring) {
            serviceScope.launch {
                try {
                    // ✅ unregisterMeasureCallbackAsync 사용
                    measureClient.unregisterMeasureCallbackAsync(
                        DataType.HEART_RATE_BPM,
                        heartRateCallback
                    ).await()

                    isMonitoring = false
                    Log.d(TAG, "✅ Callbacks unregistered")
                } catch (e: Exception) {
                    Log.e(TAG, "Error unregistering callback: ${e.message}")
                }
            }
        }

        serviceScope.cancel()
    }
}