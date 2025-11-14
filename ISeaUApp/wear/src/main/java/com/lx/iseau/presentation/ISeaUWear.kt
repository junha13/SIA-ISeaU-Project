package com.lx.iseau.presentation

import android.content.Intent
import android.os.Bundle
import android.os.Build
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
                Log.d(TAG, "✅ Foreground permissions granted.")
                requestBackgroundPermissionIfNecessary()
            } else {
                Log.w(TAG, "❌ Missing required permissions. Body Sensors: $grantedBodySensors, Activity Recognition: $grantedActivityRecognition")
            }
        }

    // ✅ BACKGROUND 권한 체크
    private fun hasBackgroundPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BODY_SENSORS_BACKGROUND
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // 13 미만에서는 별도 BACKGROUND 개념이 없어서 true 취급
            true
        }
    }

    // ✅ BACKGROUND 권한이 없으면 요청, 있으면 바로 서비스 시작
    private fun requestBackgroundPermissionIfNecessary() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasBackgroundPermission()) {
            Log.d(TAG, "Requesting BODY_SENSORS_BACKGROUND permission...")
            bgPermissionLauncher.launch(Manifest.permission.BODY_SENSORS_BACKGROUND)
        } else {
            // 이미 허용되어 있으면 바로 서비스 시작
            startSafetyMonitoringService()
        }
    }

    // ✅ BACKGROUND 센서 권한 런처 (BODY_SENSORS_BACKGROUND)
    private val bgPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                Log.d(TAG, "✅ BODY_SENSORS_BACKGROUND granted.")
            } else {
                Log.w(TAG, "⚠️ BODY_SENSORS_BACKGROUND 미허용. 화면 꺼지면 센서가 끊길 수 있음.")
            }
            // 허용하든 말든, 일단 현재 상태에서 가능한 범위로 서비스는 시작
            startSafetyMonitoringService()
        }

    private fun startSafetyMonitoringService() {
        if (hasAllPermissions()) {
            // ✅ 서버 업로드 전용 서비스 실행
            HeartRateUploadService.start(this)
            Log.d(TAG, "✅ HeartRateUploadService started.")
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
            Log.d(TAG, "Requesting FOREGROUND sensor permissions...")
            requestPermissionLauncher.launch(permissions)
        } else {
            Log.d(TAG, "Foreground permissions already granted, checking BACKGROUND...")
            // ⬇️ 바로 서비스 시작 말고, BACKGROUND 권한 체크/요청을 거쳐서 시작
            requestBackgroundPermissionIfNecessary()
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
        // (AndroidManifest.xml에 android:name="com.lx.iseau.presentation.ISeaUApp" 등록 필수)
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