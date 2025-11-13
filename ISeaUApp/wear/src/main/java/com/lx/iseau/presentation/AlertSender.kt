package com.lx.iseau.presentation

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

/**
 * ✅ 서버 전송 전담 모듈 (워치 → 데이터 서버)
 * - 의존성 없이 HttpURLConnection 사용
 * - sendHeartRateAsync(...) : 내부 스레드 풀에서 비동기 전송
 * - 서버는 JSON { "occurred_at": "...", "heart_rate": 88 } 를 받는다고 가정
 *
 * ⚠️ 반드시 ENDPOINT 를 너희 데이터 서버 주소로 교체할 것.
 */
object AlertSender {
    private const val TAG = "HrAlertSender"

    private const val ENDPOINT = "https://hellokiyo.ngrok.io/api/controltower/heart-rate"

    private val io = Executors.newFixedThreadPool(2)

    // 🔧 목업 값 (로그인/연동 전 임시)
    private const val MOCK_USER_NUMBER = 2            // 임시 유저 번호
    private const val MOCK_EMERGENCY_THRESHOLD = 100   // 이하면 긴급(true)

    /**
     * 서버로 심박/발생시각 전송 (비동기)
     * @param occurredAtIso  ISO-8601(UTC) 문자열, 예: 2025-11-12T13:45:21Z
     * @param heartRateBpm   Int BPM
     * userNumber / isEmergency 는 내부 목업으로 자동 세팅:
     *  - userNumber = MOCK_USER_NUMBER
     *  - isEmergency = (heartRateBpm <= MOCK_EMERGENCY_THRESHOLD)
     */

    fun sendHeartRateAsync(occurredAtIso: String, heartRateBpm: Int) {
        val userNumber = MOCK_USER_NUMBER
        val isEmergency = false

        io.execute {
            try {
                val json = """{"occurred_at":"$occurredAtIso","heart_rate":$heartRateBpm,"userNumber":$userNumber,"isEmergency":$isEmergency}"""
                val url = URL(ENDPOINT)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 7000
                    readTimeout = 7000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                }

                conn.outputStream.use { os ->
                    val bytes = json.toByteArray(StandardCharsets.UTF_8)
                    os.write(bytes, 0, bytes.size)
                    os.flush()
                }

                val code = conn.responseCode
                val errBody = try {
                    val es = conn.errorStream ?: conn.inputStream
                    es?.bufferedReader()?.use { it.readText() }
                } catch (_: Throwable) { null }

                if (code in 200..299) {
                    Log.i(TAG, "✅ HR 전송 성공: $code, $json")
                } else {
                    Log.w(TAG, "⚠️ HR 전송 실패: HTTP $code, $json${if (errBody!=null) ", server=$errBody" else ""}")
                }
                conn.disconnect()
            } catch (t: Throwable) {
                Log.e(TAG, "❌ HR 전송 에러: ${t.message}", t)
            }
        }
    }

}
