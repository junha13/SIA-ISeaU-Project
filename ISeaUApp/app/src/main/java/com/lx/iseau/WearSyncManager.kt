// app 모듈에 새 파일 하나 만들기: WearSyncManager.kt
package com.lx.iseau

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

object WearSyncManager {

    private const val TAG = "WearSyncManager"

    /**
     * ✅ 로그인한 userNumber를 워치로 보내는 함수 (폰 ➜ 워치)
     */
    fun sendUserNumberToWatch(context: Context, userNumber: Int) {
        if (userNumber <= 0) {
            Log.e(TAG, "❌ 잘못된 userNumber: $userNumber")
            return
        }

        val dataClient = Wearable.getDataClient(context)

        // 워치에서 "/USER_INFO" path를 리슨하도록 만들 예정
        val request = PutDataMapRequest.create("/USER_INFO").apply {
            dataMap.putInt("user_number", userNumber)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()

        Log.d(TAG, "📡 워치로 userNumber 전송 시도: $userNumber")

        dataClient.putDataItem(request)
            .addOnSuccessListener {
                Log.d(TAG, "✅ 워치로 userNumber 전송 성공: $userNumber")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ 워치로 userNumber 전송 실패: ${e.message}", e)
            }
    }
}
