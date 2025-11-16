// com/lx/iseau/presentation/UserConfigDataHandler.kt
package com.lx.iseau.presentation

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem

/**
 * 폰 → 워치 : userNumber 동기화 데이터 처리 로직
 */
class UserConfigDataHandler(private val context: Context) : DataClient.OnDataChangedListener {

    private val TAG = "UserConfigDataHandler"
    private val USER_INFO_PATH = "/USER_INFO"

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.i(TAG, "🔔 Data Layer Event Received via In-Line Listener!")

        dataEvents.forEach { event ->
            val dataItem = event.dataItem

            if (dataItem.uri.path == USER_INFO_PATH) {
                val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                val userNumber = dataMap.getInt("userNumber", -1)

                if (userNumber > 0) {
                    saveUserNumber(userNumber)
                    Log.d(TAG, "✅ /USER_INFO 수신, userNumber 저장: $userNumber")
                } else {
                    Log.e(TAG, "❌ /USER_INFO 수신했는데 userNumber가 이상함: $userNumber")
                }
            }
        }
    }

    private fun saveUserNumber(userNumber: Int) {
        val prefs = context.getSharedPreferences("iseau_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("userNumber", userNumber)
            .apply()
    }

    // 기존의 companion object는 UserSessionManager 등 다른 곳으로 이동하거나 유지합니다.
    // 여기서는 편의를 위해 제거합니다.
}