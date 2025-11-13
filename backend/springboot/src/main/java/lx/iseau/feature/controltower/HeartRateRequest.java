// lx.iseau.feature.watch.HeartRateRequest.java
package lx.iseau.feature.controltower;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeartRateRequest {

    private Integer userNumber;  // 워치 사용자의 고유 번호
    @JsonProperty("heart_rate")
    private Integer heartRate;   // 심박수 (BPM)
    @JsonProperty("occurred_at")
    private String occurredAt;   // ISO 8601 시간 문자열 (예: "2025-11-11T22:15:00Z")
    private Boolean isEmergency; // 긴급 알림 여부 (true/false)
}