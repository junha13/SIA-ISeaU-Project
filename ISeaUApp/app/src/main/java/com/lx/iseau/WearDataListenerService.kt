package com.lx.iseau

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.android.gms.wearable.*
import com.lx.iseau.data.HeartRateRequest
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import java.time.Instant

class WearDataListenerService : WearableListenerService() {

    private val TAG = "ISeaU_MobileListener"

    // 💡 [수정 완료] 사용자께서 제공해주신 실제 ngrok URL을 반영했습니다.
    private val SERVER_BASE_URL = "https://hellokiyo.ngrok.io"
    private val SERVER_HR_API_URL = "$SERVER_BASE_URL/api/watch/heart-rate"

    // 💡 TODO: 워치를 착용한 실제 사용자 번호(user_number)를 가져오는 로직 구현 필요
    // 이 값은 서버의 tb_user에 존재하는 user_number와 일치해야 합니다.
    private fun getCurrentUserNumber(): Int = 1 // 일단 1로 고정

    // Ktor HTTP 클라이언트 초기화 (JSON 직렬화 포함)
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            // kotlinx.serialization 설정
            json(
                // 서버가 요구하는 JSON 형식에 맞게 설정 (예: 스네이크 케이스 등)
            )
        }
        // 요청 타임아웃 설정을 추가하여 네트워크 실패에 대비합니다.
        engine {
            requestTimeout = 10_000 // 10초 타임아웃
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                val path = dataItem.uri.path

                // 긴급 알림 또는 실시간 업데이트 데이터를 수신
                if (path?.startsWith("/DANGER_ALERT") == true || path?.startsWith("/REALTIME_HR") == true) {

                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap

                    // 워치에서 명시적으로 추가된 데이터 사용
                    val heartRate = dataMap.getInt("heart_rate")
                    val timestamp = dataMap.getLong("timestamp")

                    // DTO에 기본값이 설정되었으므로, 워치 데이터가 없을 경우를 대비하여
                    // dataMap.getBoolean("is_emergency", false)를 사용하여 안전하게 추출합니다.
                    val isEmergency = dataMap.getBoolean("is_emergency", false)

                    if (heartRate > 0) {
                        Log.i(TAG, "Watch Data Received: HR=$heartRate, Emergency=$isEmergency, Path=$path")

                        // Instant.ofEpochMilli(timestamp)는 워치에서 보낸 long 타입의 Unix Time(ms)를
                        // ISO 8601 형식 문자열로 변환하여 서버가 요구하는 occurredAt 필드에 맞춥니다.
                        val hrData = HeartRateRequest(
                            userNumber = getCurrentUserNumber(),
                            heartRate = heartRate,
                            occurredAt = Instant.ofEpochMilli(timestamp).toString(),
                            isEmergency = isEmergency
                        )

                        // Spring Boot 서버로 데이터 전송 (REALTIME이든 DANGER든 같은 API 사용)
                        sendHeartRateToServer(hrData)
                    } else {
                        Log.e(TAG, "❌ Received invalid Heart Rate: $heartRate")
                    }
                }
            }
        }
    }

    private fun sendHeartRateToServer(data: HeartRateRequest) {
        Log.i(TAG, "Sending HR data to server: ${data.heartRate} BPM, Emergency: ${data.isEmergency}")

        serviceScope.launch {
            try {
                val response = httpClient.post(SERVER_HR_API_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(data) // DTO가 JSON 본문으로 변환되어 전송됨
                }

                if (response.status.isSuccess()) {
                    Log.d(TAG, "✅ Server acknowledged HR data successfully. Status: ${response.status}")
                } else {
                    // 서버에서 4xx나 5xx 응답이 왔을 때 상세 로그를 남깁니다.
                    val responseBody = response.bodyAsText()
                    Log.e(TAG, "❌ Server Error: ${response.status}. Body: $responseBody")
                    // 400 Bad Request가 다시 발생하면, 서버의 DTO 필드명과 안드로이드 DTO 필드명이 일치하는지 재확인해야 합니다.
                }

            } catch (e: Exception) {
                // 네트워크 연결 실패(ngrok 터널이 닫혔거나 URL이 틀렸을 때)
                Log.e(TAG, "❌ Network Error: Failed to connect to server: ${e.message}", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        httpClient.close()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed. Resources cleaned up.")
    }
}