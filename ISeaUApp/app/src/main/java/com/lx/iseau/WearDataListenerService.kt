// WearDataListenerService.kt
package com.lx.iseau

import android.util.Log
import com.google.android.gms.wearable.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.cancel
import org.json.JSONObject

class WearDataListenerService : WearableListenerService() {

    private val TAG = "ISeaU_MobileListener"

    // Ktor HTTP 클라이언트 초기화 (앱 시작 시 한 번만)
    private val httpClient = HttpClient(CIO) {
        // 이 부분에 JSON 직렬화 설정 등을 추가할 수 있습니다.
    }

    // 코루틴 스코프 정의 (네트워크 요청은 IO 스레드에서 처리)
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private val SERVER_ALERT_URL = "http://localhost:8080/api/alert/wear"

    // 🚨 [필수 설정] 경고를 보낸 사용자 ID (WearOS 착용자)로 변경하세요.
    private val SENDER_USER_ID = "WATCH_USER"

    // 🚨 [필수 설정] 경고를 받을 보호자 ID로 변경하세요.
    // 이 ID는 서버 DB에 토큰이 저장된 사용자여야 합니다.
    private val TARGET_PROTECTOR_ID = "GUEST_USER"


    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                val path = dataItem.uri.path

                if (path == "/DANGER_ALERT") {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val alertMessage = dataMap.getString("alert_message")
                    val timestamp = dataMap.getLong("timestamp")

                    Log.e(TAG, "!!! DANGER ALERT RECEIVED from Watch: $alertMessage")

                    // FCM 발송 요청 (서버 통신)
                    requestFCMAlertToServer(alertMessage, timestamp)
                }
            }
        }
    }

    private fun requestFCMAlertToServer(message: String?, timestamp: Long) {
        if (message == null) return

        Log.i(TAG, "Requesting server to send FCM for: $message")

        serviceScope.launch {
            try {
                // 1. 요청 본문(JSON) 구성 (Spring Boot의 WearAlertRequest DTO와 일치)
                val requestBody = JSONObject().apply {
                    put("targetUserId", TARGET_PROTECTOR_ID)
                    put("senderUserId", SENDER_USER_ID)
                    put("alertMessage", message)
                    put("timestamp", timestamp)
                }.toString()

                // 2. Ktor Client를 사용하여 Spring Boot 서버로 POST 요청
                val response = httpClient.post(SERVER_ALERT_URL) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

                // 3. 응답 처리
                if (response.status.isSuccess()) {
                    Log.d(TAG, "✅ Server acknowledged alert. FCM should be sent.")
                } else {
                    Log.e(TAG, "❌ Server Error: ${response.status}. Body: ${response.bodyAsText()}")
                }

            } catch (e: Exception) {
                // 서버 연결 실패, 네트워크 오류 등
                Log.e(TAG, "❌ Failed to connect to server: ${e.message}", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        httpClient.close() // 앱 종료 시 리소스 정리
        serviceScope.cancel()
    }
}