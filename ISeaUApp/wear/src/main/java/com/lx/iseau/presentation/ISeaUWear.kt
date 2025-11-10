package com.lx.iseau.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.lx.iseau.presentation.theme.ISeaUAppTheme

// 💡 권한 처리를 위한 필수 Import
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest // Android Manifest 클래스
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class ISeaUWear : ComponentActivity() {

    private val TAG = "ISeaUWear"

    // 💡 런타임 권한 요청 콜백 설정
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.BODY_SENSORS] == true &&
                permissions[Manifest.permission.ACTIVITY_RECOGNITION] == true) {

                Log.d(TAG, "Health permissions granted. Starting service.")
                startSafetyMonitoringService()
            } else {
                Log.e(TAG, "Required health permissions denied. Cannot monitor health data.")
            }
        }

    // 💡 Health Services 백그라운드 모니터링 서비스 시작
    private fun startSafetyMonitoringService() {
        val serviceIntent = Intent(this, SafetyMonitoringService::class.java)
        startService(serviceIntent)
        Log.d(TAG, "SafetyMonitoringService started.")
    }

    private fun requestPermissionsIfNecessary() {
        val permissions = arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
        )

        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissionLauncher.launch(permissions)
        } else {
            startSafetyMonitoringService()
        }
    }

    // 버튼 클릭 시 권한 확인/서비스 시작 재시도
    private fun triggerManualAlert() {
        Log.d(TAG, "Manual alert button clicked, attempting service start.")
        requestPermissionsIfNecessary()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        // 💡 onCreate에서 권한을 요청하고 서비스 시작을 시도합니다.
        requestPermissionsIfNecessary()

        setContent {
            WearApp(
                greetingName = "Android",
                onAlertClick = { triggerManualAlert() }
            )
        }
    }
}

@Composable
fun WearApp(greetingName: String, onAlertClick: () -> Unit) {
    ISeaUAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()

            Button(
                onClick = onAlertClick,
                modifier = Modifier.fillMaxSize(0.9f)
            ) {
                Text(text = "🚨 Health Service 시작/권한 확인")
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp("Preview Android", onAlertClick = {})
}