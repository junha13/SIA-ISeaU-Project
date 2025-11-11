package com.lx.iseau.presentation;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

// 서버로 전송할 데이터 모델
class AlertRequest {
    public String deviceId;
    public int heartRate;
    public long timestamp;

    public AlertRequest(String deviceId, int heartRate, long timestamp) {
        this.deviceId = deviceId;
        this.heartRate = heartRate;
        this.timestamp = timestamp;
    }
}

// 서버 응답 모델 (간단히 상태만 가정)
class AlertResponse {
    public String status;
}

public interface ApiService {
    // 💡 실제 서버 URL로 변경해야 합니다.
    String BASE_URL = "https://your-server-api.com/";

    @POST("api/emergency/alert")
    Call<AlertResponse> sendDangerAlert(@Body AlertRequest request);
}