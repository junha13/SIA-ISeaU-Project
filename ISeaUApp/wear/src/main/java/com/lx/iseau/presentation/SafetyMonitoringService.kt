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
    import androidx.core.app.NotificationCompat
    import androidx.core.content.ContextCompat
    import androidx.health.services.client.HealthServices
    import androidx.health.services.client.MeasureClient
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
        private lateinit var measureClient: MeasureClient
        private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        private val TAG = "ISeaU_SafetyService"
        private val NOTIFICATION_ID = 1001
        private val CHANNEL_ID = "safety_monitoring_channel"

        private val DANGER_HR_LOW = 50.0
        private val DANGER_HR_HIGH = 80.0

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
                    val bpm = heartRate.roundToInt()
                    lastHeartRate = heartRate

                    // ViewModel 업데이트 (워치 UI)
                    val app = application as ISeaUApp
                    app.healthViewModel.updateHeartRate(bpm)

                    Log.d(TAG, "❤️ Heart Rate: $bpm BPM (Logged in Service, Sent to ViewModel)")

                    if (bpm > 0) {
                        // 1. 긴급 안전 로직 실행 (이상 감지 시 DANGER_ALERT 전송)
                        checkSafetyLogic()

                        // 2. 💡 [추가] 실시간 데이터를 휴대폰으로 전송
                        sendRealtimeHRToMobile(bpm)
                    } else {
                        Log.w(TAG, "⚠️ HR Monitoring Failed: Heart Rate is null or 0.0. Check wearing or sensor.")
                    }
                }
            }
        }

        override fun onCreate() {
            super.onCreate()
            dataClient = Wearable.getDataClient(this)
            measureClient = HealthServices.getClient(this).measureClient

            Log.d(TAG, "SafetyMonitoringService created.")
            createNotificationChannel()
        }

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            Log.d(TAG, "SafetyMonitoringService started.")

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                    startForeground(
                        NOTIFICATION_ID,
                        createNotification(),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                    )
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    startForeground(NOTIFICATION_ID, createNotification())
                }


                else -> {
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
                    val capabilities = measureClient.getCapabilitiesAsync().await()

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

        // 긴급 상황 시 모바일로 알림 전송 (기존 로직)
        private fun checkSafetyLogic() {
            val hr = lastHeartRate

            if (hr == null || hr == 0.0) return

            val isLowHR = hr < DANGER_HR_LOW
            val isHighHR = hr > DANGER_HR_HIGH

            if (isLowHR || isHighHR) {
                val hrDisplay = hr.roundToInt()
                val status = if (isLowHR) "너무 낮음" else "너무 높음"

                val message = "🚨 긴급: 심박수 이상! ${hrDisplay} BPM ($status)"
                Log.e(TAG, message)

                // DANGER_ALERT 경로로 전송 (isEmergency=true)
                sendDataToMobile("/DANGER_ALERT", hrDisplay, message, isEmergency = true)
            }
        }

        /**
         * 💡 [추가] 실시간 심박수 데이터를 모바일로 전송합니다.
         */
        private fun sendRealtimeHRToMobile(bpm: Int) {
            // REALTIME_HR 경로로 전송 (isEmergency=false)
            sendDataToMobile("/REALTIME_HR", bpm, "Heart Rate Update", isEmergency = false)
        }

        /**
         * 워치에서 휴대폰으로 데이터를 전송하는 통합 함수.
         */
        private fun sendDataToMobile(path: String, heartRate: Int, message: String, isEmergency: Boolean) {
            val putDataReq = PutDataMapRequest.create(path).apply {
                dataMap.putString("alert_message", message)
                dataMap.putInt("heart_rate", heartRate) // 💡 심박수 값 명시적 추가
                dataMap.putBoolean("is_emergency", isEmergency) // 💡 긴급 여부 명시적 추가
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }

            val request = putDataReq.asPutDataRequest()

            dataClient.putDataItem(request)
                .addOnSuccessListener {
                    Log.d(TAG, "✅ Data sent to mobile ($path): HR=$heartRate, Emergency=$isEmergency")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "❌ Failed to send data ($path): ${e.message}", e)
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