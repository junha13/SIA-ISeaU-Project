package com.lx.iseau.presentation

import android.telephony.CarrierConfigManager.ImsEmergency
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.regex.Pattern

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

    // private const val SERVER_URL = "https://iseau.kr"
    // private const val SERVER_URL = "https://uneffusing-bowen-noninteractive.ngrok-free.dev"
    // private const val SERVER_URL = "https://ongoing-dierdre-consonantly.ngrok-free.dev"
    private const val SERVER_URL = "https://hellokiyo.ngrok.io"
    private const val ENDPOINT = SERVER_URL+"/api/controltower/heart-rate"

    private val io = Executors.newFixedThreadPool(2)

    /**
     * @param userNumber   워치 주인 유저 번호
     * @param occurredAtIso  ISO-8601(UTC) 문자열, 예: 2025-11-12T13:45:21Z
     * @param heartRateBpm   Int BPM
     * @param latitude     위도 (nullable)
     * @param longitude    경도 (nullable)
     * @param altitude     고도(Z, meter 단위, nullable)
     */

    fun sendHeartRateAsync(userNumber: Int, heartRateBpm: Int, occurredAtIso: String, latitude: Double?, longitude: Double?, altitude: Double?) {
        io.execute {
            try {
                // 1. 추가 데이터(위치/고도) JSON 조각 만들기
                // 주의: 이전 DTO(HeartRateRequest)에 맞춰 키 이름을 watchLatitude 등으로 변경함
                val sb = StringBuilder()

                if (latitude != null && longitude != null) {
                    sb.append(""", "watchLatitude": $latitude""")
                    sb.append(""", "watchLongitude": $longitude""")
                }

                if (altitude != null) {
                    sb.append(""", "watchAltitude": $altitude""")
                }
// 2. JSON 조립 (occurredAt에 따옴표 "" 추가됨 확인하세요!)
                // 구조: {"key":value, "key":"StringValue" ... }
                val json = """
                    {
                        "userNumber": $userNumber,
                        "heartRate": $heartRateBpm,
                        "occurredAt": "$occurredAtIso"
                        $sb
                    }
                """.trimIndent().replace("\n", "") // 줄바꿈 제거 (선택사항)

                Log.d(TAG, "🚀 전송 시도 JSON: $json") // 디버깅용 로그

                val url = URL(ENDPOINT)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 5000
                    readTimeout = 5000
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

                if (code in 200..299) {
                    Log.i(TAG, "✅ HR 전송 성공: HTTP $code")
                } else {
                    // 에러 내용 읽기
                    val errStream = conn.errorStream ?: conn.inputStream
                    val errBody = errStream?.bufferedReader()?.use { it.readText() }
                    Log.e(TAG, "⚠️ HR 전송 실패: HTTP $code, ServerMsg: $errBody")
                }

                conn.disconnect()
            } catch (t: Throwable) {
                Log.e(TAG, "❌ 네트워크 에러: ${t.message}", t)
            }
        }
    }
}
