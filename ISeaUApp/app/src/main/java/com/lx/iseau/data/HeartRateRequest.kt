package com.lx.iseau.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName // 💡 추가: SerialName 사용을 위해 필요

@Serializable
data class HeartRateRequest(
    // Int 타입을 Nullable(Int?)로 변경하여 Java Integer와 호환성 맞추기
    @SerialName("user_number") // 💡 수정: JSON 키를 서버 백엔드/DB 규칙인 snake_case로 강제
    val userNumber: Int?,
    @SerialName("heart_rate") // 💡 수정: JSON 키를 snake_case로 강제
    val heartRate: Int?,
    @SerialName("occurred_at") // 💡 수정: JSON 키를 snake_case로 강제
    val occurredAt: String, // ISO 8601 형식
    @SerialName("is_emergency") // 💡 수정: JSON 키를 snake_case로 강제
    val isEmergency: Boolean = false
)