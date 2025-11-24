// app 모듈의 WearSyncManager.kt
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

        // 연결된 웨어 노드가 실제로 있는지 로그로 확인 (유저넘버 워치에서 못받아서 확인용)
        Wearable.getNodeClient(context).connectedNodes
            .addOnSuccessListener { nodes ->
                Log.d(TAG, "📡 연결된 웨어 노드 수: ${nodes.size}")
                nodes.forEach { node ->
                    Log.d(TAG, "📡 nodeId=${node.id}, displayName=${node.displayName}, isNearby=${node.isNearby}")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ connectedNodes 조회 실패: ${e.message}", e)
            }

        // 워치에서 "/USER_INFO" path를 리슨
        val request = PutDataMapRequest.create("/USER_INFO").apply {
            dataMap.putInt("userNumber", userNumber)
            dataMap.putLong("timestamp", System.currentTimeMillis()) // 데이터 변경 보장
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