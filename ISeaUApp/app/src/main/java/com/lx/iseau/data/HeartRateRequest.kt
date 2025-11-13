package com.lx.iseau.data

import kotlinx.serialization.Serializable

@Serializable
data class HeartRateRequest(
    //Int 타입을 Nullable(Int?)로 변경하여 Java Integer와 호환성 맞추기
    val userNumber: Int?,
    val heartRate: Int?,
    val occurredAt: String, // ISO 8601 형식
    val isEmergency: Boolean = false
)