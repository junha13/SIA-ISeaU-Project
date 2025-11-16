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
import androidx.compose.foundation.layout.PaddingValues


import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.text.style.TextAlign

class ISeaUWear : ComponentActivity() {

    private val TAG = "ISeaUWear"

    // 💡 ViewModel 인스턴스 선언
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
            // ✅ 서버 업로드 전용 서비스 실행 (여기서는 HeartRateUploadService가 정의되어 있다고 가정)
            HeartRateUploadService.start(this)
            Log.d(TAG, "✅ HeartRateUploadService started.")

            // 💡 서비스 시작 시 연결 상태를 '연결됨'으로 업데이트 (UI 전환 목적)
            healthViewModel.updatePhoneConnectionStatus(true)
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

        // 💡 테스트를 위해 ViewModel 인스턴스를 여기서 임시 생성
        healthViewModel = ViewModelProvider(this).get(HealthDataViewModel::class.java)
        healthViewModel.updatePhoneConnectionStatus(false) // 초기 상태 미연결

        requestPermissionsIfNecessary()

        setContent {
            ISeaUAppTheme {
                // 💡 연결 상태에 따라 다른 화면을 표시
                if (healthViewModel.isPhoneConnected) {
                    WearApp(
                        viewModel = healthViewModel,
                        onAlertClick = { triggerManualAlert() }
                    )
                } else {
                    PhoneConnectionStatus(
                        viewModel = healthViewModel,
                        onRetryClick = {
                            Log.d(TAG, "Phone connection retry clicked. Attempting to start service...")
                            // 서비스 시작/재시도 로직 호출
                            requestPermissionsIfNecessary()
                        }
                    )
                }
            }
        }
    }
}


// 🎨 ISeaU Custom Colors (SCSS 변수 참조)
private val IseuPrimary = Color(0xFF0092BA) // $iseu-primary
private val IseuSecondary = Color(0xFF0B1956) // $iseu-secondary
private val IseuSuccess = Color(0xFF7EEC85) // $iseu-success
private val IseuDanger = Color(0xFFEB725B) // $iseu-danger
private val IseuEmergency = Color(0xFFB93F67) // $iseu-emergency
private val IseuWarning = Color(0xFFFFB354) // $iseu-warning


/**
 * 💡 휴대폰 연결 상태를 표시하는 Composable
 */
@Composable
fun PhoneConnectionStatus(viewModel: HealthDataViewModel, onRetryClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IseuSecondary), // 배경색 적용
        contentAlignment = Alignment.Center
    ) {
        TimeText()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingValues(horizontal = 16.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = viewModel.iseauText,
                color = IseuPrimary, // 색상 적용
                fontSize = 30.sp, // 💡 I SEA U 글씨 크기 키움
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "휴대폰 연결 확인 중",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = "휴대폰과 연결되면 안전 모니터링이 시작됩니다.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding(bottom = 20.dp),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetryClick,
                colors = androidx.wear.compose.material.ButtonDefaults.primaryButtonColors(
                    backgroundColor = IseuPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth(0.6f) // 💡 크기 조정
                    .height(32.dp)
            ) {
                Text(text = "연결 재시도") // 💡 텍스트 변경
            }
        }
    }
}


/**
 * 💡 ViewModel을 받아 상태에 바인딩하는 Composable (ISeaU 색상 및 크기 적용)
 */
@Composable
fun WearApp(viewModel: HealthDataViewModel, onAlertClick: () -> Unit) {
    ISeaUAppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(IseuSecondary), // 💡 배경색 변경
            contentAlignment = Alignment.Center
        ) {
            TimeText()

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = viewModel.iseauText,
                    color = IseuPrimary, // 💡 색상 적용
                    fontSize = 30.sp, // 💡 I SEA U 글씨 크기 키움 (BPM과 비슷하게)
                    modifier = Modifier.padding(top= 6.dp)
                )
                // 1. 상태 표시 텍스트 (ViewModel 상태 연결)
                Text(
                    text = viewModel.monitoringStatus,
                    color = when(viewModel.statusColor) { // 💡 ViewModel 상태 색상을 ISeaU 색상으로 매핑
                        Color.Green -> IseuSuccess
                        Color.Red -> IseuEmergency
                        else -> Color.LightGray
                    },
                    fontSize = 12.sp, // 💡 심박수 모니터링 중 크기는 12sp로 유지
                    modifier = Modifier.padding(bottom = 2.dp, top= 3.dp)
                )

                // 2. 현재 심박수 표시 텍스트 (ViewModel 상태 연결)
                Text(
                    text = viewModel.heartRateText,
                    color = Color.White, // 💡 심박수 텍스트 흰색
                    style = MaterialTheme.typography.title1,
                    fontSize = 30.sp, // 💡 BPM 글씨 크기 유지
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // 3. 기존 버튼 (ISeaU 색상 적용)
                Button(
                    onClick = onAlertClick,
                    colors = androidx.wear.compose.material.ButtonDefaults.primaryButtonColors(
                        backgroundColor = IseuPrimary, // 💡 버튼 배경색 변경
                        contentColor = Color.White // 💡 버튼 내용 색상 흰색
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.6f) // 💡 크기 조정
                        .height(32.dp)
                ) {
                    Text(text = "측정") // 💡 텍스트 변경
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    val viewModel = HealthDataViewModel()
    // 💡 연결된 상태 Preview
    viewModel.updateHeartRate(75)
    viewModel.updatePhoneConnectionStatus(true)
    WearApp(viewModel = viewModel, onAlertClick = {})
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DisconnectedPreview() {
    val viewModel = HealthDataViewModel()
    // 💡 미연결 상태 Preview
    viewModel.updatePhoneConnectionStatus(false)
    PhoneConnectionStatus(viewModel = viewModel, onRetryClick = {})
}