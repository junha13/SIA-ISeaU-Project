package com.lx.iseau;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

public class WearDataListenerService extends WearableListenerService {

    private static final String TAG = "ISeaU_MobileListener_Java";
    private static final String DANGER_ALERT_PATH = "/DANGER_ALERT";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();

                if (DANGER_ALERT_PATH.equals(path)) {
                    DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    String alertMessage = dataMap.getString("alert_message");
                    long timestamp = dataMap.getLong("timestamp");

                    Log.e(TAG, "!!! DANGER ALERT RECEIVED from Watch: " + alertMessage);

                    // FCM 발송 요청 (서버 통신 필요)
                    requestFCMAlertToServer(alertMessage, timestamp);
                }
            }
        }
    }

    private void requestFCMAlertToServer(String message, long timestamp) {
        Log.i(TAG, "Requesting server to send FCM for: " + message);

        // FirebaseMessaging.getInstance().getToken()은 비동기 작업입니다.
        Task<String> tokenTask = FirebaseMessaging.getInstance().getToken();

        tokenTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                Log.d(TAG, "Device FCM Token: " + token);

                try {
                    // 🚨 주의: 이 부분은 서버 API에 FCM 발송을 요청하는 실제 코드로 대체해야 합니다.
                    JSONObject apiCallData = new JSONObject();
                    apiCallData.put("target_fcm_token", token);
                    apiCallData.put("alert_message", message);
                    apiCallData.put("timestamp", timestamp);

                    // TODO: MyServerApi.sendAlert(apiCallData) 와 같은 실제 서버 API 호출 코드를 구현
                } catch (JSONException e) {
                    Log.e(TAG, "JSON Error: " + e.getMessage());
                }
            } else {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
            }
        });
    }
}