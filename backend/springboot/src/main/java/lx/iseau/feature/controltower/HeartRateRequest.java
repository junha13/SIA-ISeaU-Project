// lx.iseau.feature.watch.HeartRateRequest.java
package lx.iseau.feature.controltower;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeartRateRequest {

    private Integer userNumber;  // 워치 사용자의 고유 번호
    private Integer heartRate;   // 심박수 (BPM)
    private String occurredAt;   // ISO 8601 시간 문자열 (예: "2025-11-11T22:15:00Z")
    private Boolean isEmergency; // 긴급 알림 여부 (true/false)
}