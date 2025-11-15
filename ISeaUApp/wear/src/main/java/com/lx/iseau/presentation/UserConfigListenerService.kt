package com.lx.iseau.presentation

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

/**
 * ✅ 폰 → 워치 : userNumber 동기화용 리스너
 * - 폰에서 /USER_INFO 경로로 내려보낸 user_number를 받는다.
 * - SharedPreferences 에 저장해두고, 워치 → 서버 전송할 때 이 값을 사용.
 */
class UserConfigListenerService : WearableListenerService() {

    private val TAG = "UserConfigListener"

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        Log.i(TAG, "🔔 Data Layer Event Received!") // ✅ 이 로그가 뜨는지 확인

        dataEvents.forEach { event ->
            if (event.type != DataEvent.TYPE_CHANGED) return@forEach

            val item = event.dataItem
            val path = item.uri.path

            if (path == "/USER_INFO") {
                val dataMap = DataMapItem.fromDataItem(item).dataMap
                val userNumber = dataMap.getInt("user_number", -1)

                if (userNumber > 0) {
                    saveUserNumber(userNumber)
                    Log.d(TAG, "✅ /USER_INFO 수신, userNumber 저장: $userNumber")
                } else {
                    Log.e(TAG, "❌ /USER_INFO 수신했는데 user_number가 이상함: $userNumber")
                }
            }
        }
    }

    private fun saveUserNumber(userNumber: Int) {
        val prefs = getSharedPreferences("iseau_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("user_number", userNumber)
            .apply()
    }

    companion object {
        /**
         * 워치 어디서나 userNumber가 필요할 때 호출하는 헬퍼
         */
        fun getSavedUserNumber(context: Context): Int {
            val prefs = context.getSharedPreferences("iseau_prefs", Context.MODE_PRIVATE)
            return prefs.getInt("user_number", -1)
        }
    }
}
