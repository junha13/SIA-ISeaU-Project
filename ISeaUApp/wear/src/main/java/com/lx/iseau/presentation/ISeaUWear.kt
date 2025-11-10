package com.lx.iseau.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.lx.iseau.presentation.theme.ISeaUAppTheme
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.lifecycle.ViewModelProvider // ViewModelProvider Import 추가

class ISeaUWear : ComponentActivity() {

    private val TAG = "ISeaUWear"

    // 💡 [추가] ViewModel 인스턴스 선언
    private lateinit var healthViewModel: HealthDataViewModel

    // 💡 런타임 권한 요청 콜백 설정 (기존 로직 유지)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val grantedBodySensors = permissions[Manifest.permission.BODY_SENSORS] ?: false
            val grantedActivityRecognition = permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: false

            if (grantedBodySensors && grantedActivityRecognition) {
                Log.d(TAG, "✅ All required permissions granted.")
                startSafetyMonitoringService()
            } else {
                Log.w(TAG, "❌ Missing required permissions. Body Sensors: $grantedBodySensors, Activity Recognition: $grantedActivityRecognition")
            }
        }

    private fun startSafetyMonitoringService() {
        if (hasAllPermissions()) {
            val serviceIntent = Intent(this, SafetyMonitoringService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
            Log.d(TAG, "✅ SafetyMonitoringService started.")
        } else {
            Log.w(TAG, "❌ Cannot start service: Permissions are missing.")
        }
    }

    private fun hasAllPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionsIfNecessary() {
        val permissions = arrayOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
        )

        if (!hasAllPermissions()) {
            Log.d(TAG, "Requesting permissions...")
            requestPermissionLauncher.launch(permissions)
        } else {
            Log.d(TAG, "Permissions already granted, attempting to start service.")
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

        // 💡 [수정] Application 클래스에서 ViewModel 인스턴스를 가져와 초기화
        healthViewModel = (application as ISeaUApp).healthViewModel

        requestPermissionsIfNecessary()

        setContent {
            // 💡 [수정] ViewModel을 Composable에 전달
            WearApp(
                viewModel = healthViewModel,
                onAlertClick = { triggerManualAlert() }
            )
        }
    }
}

/**
 * 💡 [수정] ViewModel을 받아 상태에 바인딩하는 Composable
 */
@Composable
fun WearApp(viewModel: HealthDataViewModel, onAlertClick: () -> Unit) {
    ISeaUAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center // 중앙 정렬
        ) {
            TimeText()

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // 수직 중앙 정렬
            ) {
                // 1. 상태 표시 텍스트 (ViewModel 상태 연결)
                Text(
                    text = viewModel.monitoringStatus,
                    color = viewModel.statusColor, // ViewModel의 상태에 따라 색상 변경
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 2. 현재 심박수 표시 텍스트 (ViewModel 상태 연결)
                Text(
                    text = viewModel.heartRateText, // ViewModel의 상태에 따라 BPM 텍스트 변경
                    style = MaterialTheme.typography.title1,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // 3. 기존 버튼
                Button(
                    onClick = onAlertClick,
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Text(text = "서비스 시작/재시도")
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    // Preview에서는 임시 ViewModel을 사용합니다.
    WearApp(viewModel = HealthDataViewModel(), onAlertClick = {})
}