package com.lx.iseau.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel

/**
 * 심박수 및 모니터링 상태를 UI에 노출하는 ViewModel입니다.
 */
class HealthDataViewModel : ViewModel() {

    // 실시간 심박수 표시 텍스트 (UI에 바인딩됨)
    var heartRateText by mutableStateOf("-- BPM")
        private set

    // 모니터링 상태 텍스트 (UI에 바인딩됨)
    var monitoringStatus by mutableStateOf("모니터링 대기 중...")
        private set

    // 상태에 따른 텍스트 색상 (UI에 바인딩됨)
    var statusColor by mutableStateOf(Color.Gray)
        private set

    /**
     * 서비스에서 호출하여 UI 상태를 업데이트합니다.
     */
    fun updateHeartRate(bpm: Int) {
        if (bpm > 0) {
            heartRateText = "$bpm BPM"
            monitoringStatus = "심박수 모니터링 중"
            statusColor = Color.Green
        } else {
            heartRateText = "-- BPM"
            monitoringStatus = "⚠️ 센서 데이터 미수신"
            statusColor = Color.Red
        }
    }
}