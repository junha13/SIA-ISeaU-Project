package com.lx.iseau.presentation

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService

/**
 * ✅ 폰 → 워치 : userNumber 동기화용 리스너
 * - 폰에서 /USER_INFO 경로로 내려보낸 userNumber를 받는다.
 * - SharedPreferences 에 저장해두고, 워치 → 서버 전송할 때 이 값을 사용.
 */
import com.google.android.gms.wearable.CapabilityClient // 이 import가 이미 최상단에 있어야 함.

class UserConfigListenerService : WearableListenerService(),
    com.google.android.gms.wearable.CapabilityClient.OnCapabilityChangedListener {

    private val TAG = "UserConfigListener"

    private val healthDataViewModel by lazy { HealthDataViewModel() }

    private val MOBILE_APP_CAPABILITY = "mobile_app_capability"
    override fun onCreate() {
        super.onCreate()
        // ⭐ 서비스 생성 시 Capability 리스너 등록
        Wearable.getCapabilityClient(this)
            .addListener(this, MOBILE_APP_CAPABILITY)
        Log.d(TAG, "⭐ Capability Listener 등록: $MOBILE_APP_CAPABILITY")
    }

    override fun onDestroy() {
        super.onDestroy()
        // ⭐ 서비스 종료 시 리스너 해제
        Wearable.getCapabilityClient(this)
            .removeListener(this, MOBILE_APP_CAPABILITY)
        Log.d(TAG, "⭐ Capability Listener 해제: $MOBILE_APP_CAPABILITY")
    }

    /**
     * ⭐ Capability 리스너 콜백: 폰 앱의 연결 상태가 변경될 때마다 호출됩니다.
     */
    override fun onCapabilityChanged(capabilityInfo: CapabilityInfo) {
        val isConnected = capabilityInfo.nodes.isNotEmpty()
        Log.i(TAG, "📞 폰 연결 상태 변경됨: $isConnected (노드 수: ${capabilityInfo.nodes.size})")

        // 💡 HealthDataViewModel을 사용하여 UI에 연결 상태를 업데이트합니다.
        healthDataViewModel.updatePhoneConnectionStatus(isConnected)

        if (isConnected) {
            // 연결 시: 필요한 경우 즉시 동기화를 시도하거나 HeartRateUploadService를 시작합니다.
        } else {
            // 연결 해제 시: HeartRateUploadService를 중지하거나 데이터 전송을 일시 중단합니다.
        }
    }
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)
        Log.i(TAG, "🔔 Data Layer Event Received!") // ✅ 이 로그가 뜨는지 확인

        dataEvents.forEach { event ->
            if (event.type != DataEvent.TYPE_CHANGED) return@forEach

            val item = event.dataItem
            val path = item.uri.path

            if (path == "/USER_INFO") {
                val dataMap = DataMapItem.fromDataItem(item).dataMap
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
        val prefs = getSharedPreferences("iseau_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("userNumber", userNumber)
            .apply()
    }

    companion object {
        /**
         * 워치 어디서나 userNumber가 필요할 때 호출하는 헬퍼
         */
        fun getSavedUserNumber(context: Context): Int {
            val prefs = context.getSharedPreferences("iseau_prefs", Context.MODE_PRIVATE)
            return prefs.getInt("userNumber", -1)
        }
    }
}
