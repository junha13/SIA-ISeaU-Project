package com.lx.iseau.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

/**
 * ✅ 워치에서 현재 위치(lat, lon)를 한 번 가져오는 헬퍼
 * - 내부에서 FusedLocationProviderClient 사용
 * - 권한은 Activity(ISeaUWear)에서 이미 요청했다고 가정
 */
class LocationProvider(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * @param callback (latitude, longitude, altitude) 전달. 실패하거나 권한 없으면 (null, null, null)
     */
    @SuppressLint("MissingPermission") // 권한 체크는 Activity 쪽에서 함
    fun getCurrentLocation(callback: (Double?, Double?, Double?) -> Unit) {
        try {
            val cts = CancellationTokenSource()

            fusedClient
                .getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                )
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        // ✅ 고도(Z). hasAltitude() 가 false면 신뢰 어려우니 null 처리
                        val altitude: Double? =
                            if (location.hasAltitude()) location.altitude else null

                        Log.d(TAG, "📍 위치 획득: ${latitude}, ${longitude}, ${altitude ?: "null"}")
                        callback(latitude, longitude, altitude)
                    } else {
                        Log.w(TAG, "⚠️ location == null (위치 사용 불가)")
                        callback(null, null, null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "❌ 위치 가져오기 실패: ${e.message}", e)
                    callback(null, null, null)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ 위치 권한 없음: ${e.message}", e)
            callback(null, null, null)
        } catch (t: Throwable) {
            Log.e(TAG, "❌ 알 수 없는 위치 에러: ${t.message}", t)
            callback(null, null, null)
        }
    }

    companion object {
        private const val TAG = "LocationProvider"
    }
}
