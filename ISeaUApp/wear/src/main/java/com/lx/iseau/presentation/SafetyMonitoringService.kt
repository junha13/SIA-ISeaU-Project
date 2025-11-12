package com.lx.iseau.presentation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import androidx.health.services.client.HealthServices;
import androidx.health.services.client.MeasureClient;
import androidx.health.services.client.MeasureCallback;
import androidx.health.services.client.data.Availability;
import androidx.health.services.client.data.DataPoint;
import androidx.health.services.client.data.DataPointContainer;
import androidx.health.services.client.data.DataType;
import androidx.health.services.client.data.DeltaDataType;
import androidx.health.services.client.data.DataTypeAvailability;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// Retrofit 관련 클래스는 정의되지 않았으므로 주석 유지
// import retrofit2.Call;
// import retrofit2.Callback;
// import retrofit2.Response;

public class SafetyMonitoringService extends Service {

    private MeasureClient measureClient;
    private static final String TAG = "ISeaU_SafetyService";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "safety_monitoring_channel";

    private static final double DANGER_HR_LOW = 50.0;
    private static final double DANGER_HR_HIGH = 150.0;

    private boolean isMonitoring = false;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final MeasureCallback heartRateCallback = new MeasureCallback() {
        @Override
        public void onAvailabilityChanged(DeltaDataType<?, ?> dataType, Availability availability) {
            if (availability instanceof DataTypeAvailability) {
                Log.d(TAG, "Heart Rate availability changed: " + availability);
            }
        }

        @Override
        public void onDataReceived(DataPointContainer data) {
            // 🛠️ 수정: DataPoint<?>를 사용하여 "원시 형식" 경고를 해결합니다.
            for (DataPoint<?> dataPoint : data.getData(DataType.HEART_RATE_BPM)) {

                // 🛠️ 수정: 1.1.0-alpha05 버전에 맞춰 getAsDouble() 메서드 사용.
                double heartRate = dataPoint.getAsDouble();
                int bpm = (int) Math.round(heartRate);

                // ⚠️ ViewModel 호출 주석 유지 (Ambiguous Call 오류 방지)
                // ISeaUApp app = (ISeaUApp) getApplication();
                // app.getHealthViewModel().updateHeartRate(bpm);

                Log.d(TAG, "❤️ Heart Rate: " + bpm + " BPM (Logged in Service)");

                if (bpm > 0) {
                    checkSafetyLogic(bpm);
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        measureClient = HealthServices.getClient(this).getMeasureClient();
        Log.d(TAG, "SafetyMonitoringService created.");
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SafetyMonitoringService started.");

        // Start Foreground Service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH);
        } else {
            startForeground(NOTIFICATION_ID, createNotification());
        }

        if (hasRequiredPermissions()) {
            subscribeToHeartRate();
        } else {
            Log.e(TAG, "❌ Required permissions not granted!");
            stopSelf();
        }

        return START_STICKY;
    }

    private void subscribeToHeartRate() {
        if (isMonitoring) {
            Log.d(TAG, "Already monitoring heart rate.");
            return;
        }

        // Kotlin의 await()에 대응하는 Java 비동기 처리 로직
        ListenableFuture<Boolean> capabilityFuture = Futures.transform(
                measureClient.getCapabilitiesAsync(),
                capabilities -> capabilities.getSupportedDataTypesMeasure().contains(DataType.HEART_RATE_BPM),
                ContextCompat.getMainExecutor(this));

        capabilityFuture.addListener(() -> {
            try {
                if (capabilityFuture.get()) {
                    measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, heartRateCallback);
                    isMonitoring = true;
                    Log.d(TAG, "✅ Heart Rate monitoring started!");
                } else {
                    Log.e(TAG, "❌ Heart Rate sensor not supported!");
                    stopSelf();
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to check capabilities or start monitoring: " + e.getMessage(), e);
                stopSelf();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void checkSafetyLogic(int bpm) {
        boolean isLowHR = bpm < DANGER_HR_LOW;
        boolean isHighHR = bpm > DANGER_HR_HIGH;

        if (isLowHR || isHighHR) {
            String status = isLowHR ? "너무 낮음" : "너무 높음";
            String message = "🚨 긴급: 심박수 이상! " + bpm + " BPM (" + status + ")";
            Log.e(TAG, message);

            // sendAlertToServer(bpm);
        }
    }

    private boolean hasRequiredPermissions() {
        String[] permissions = {
                android.Manifest.permission.BODY_SENSORS,
                android.Manifest.permission.ACTIVITY_RECOGNITION
        };
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private Notification createNotification() {
        createNotificationChannel();
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ISeaU 안전 모니터링")
                .setContentText("심박수를 실시간으로 모니터링 중")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "안전 모니터링",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("백그라운드에서 심박수를 모니터링합니다");

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed. Cleaning up...");

        if (isMonitoring) {
            measureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, heartRateCallback);
            isMonitoring = false;
            Log.d(TAG, "✅ Callbacks unregistered");
        }
        scheduler.shutdown();
    }
}