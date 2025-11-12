// Android app/src/main/java/.../data/HeartRateRequest.kt
package com.lx.iseau.data

import kotlinx.serialization.Serializable

@Serializable
data class HeartRateRequest(
    val userNumber: Int,
    val heartRate: Int,
    val occurredAt: String, // ISO 8601 형식
    val isEmergency: Boolean = false
)