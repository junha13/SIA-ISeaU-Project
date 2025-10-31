package com.lx.iseau

import android.util.Log
import com.google.android.gms.wearable.*
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject

class WearDataListenerService : WearableListenerService() {

    private val TAG = "ISeaU_MobileListener"

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                val path = dataItem.uri.path

                if (path == "/DANGER_ALERT") {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val alertMessage = dataMap.getString("alert_message")
                    val timestamp = dataMap.getLong("timestamp")

                    Log.e(TAG, "!!! DANGER ALERT RECEIVED from Watch: $alertMessage")

                    // FCM 발송 요청 (서버 통신 필요)
                    requestFCMAlertToServer(alertMessage, timestamp)
                }
            }
        }
    }

    private fun requestFCMAlertToServer(message: String?, timestamp: Long) {
        Log.i(TAG, "Requesting server to send FCM for: $message")

        // 🚨 주의: 이 부분은 서버 API에 FCM 발송을 요청하는 실제 코드로 대체해야 합니다.
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "Device FCM Token: $token")

                val apiCallData = JSONObject().apply {
                    put("target_fcm_token", token)
                    put("alert_message", message)
                    put("timestamp", timestamp)
                }

                // TODO: MyServerApi.sendAlert(apiCallData) 와 같은 실제 서버 API 호출 코드를 구현
            }
        }
    }
}