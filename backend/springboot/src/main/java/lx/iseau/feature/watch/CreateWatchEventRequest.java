package lx.iseau.feature.watch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 워치에서 전송하는 최소 요청 바디
 * - 심박수/산소포화도/발생시각만 받습니다.
 * - userNumber는 세션에서 주입(로그인 사용자)합니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWatchEventRequest {
    // 심박수 (bpm)
    private Integer heartRate;
    // 산소포화도 (SpO2, %)
    private Integer spo2;
    // 이벤트 발생 시각 (ISO8601 문자열로 받되, 컨트롤러에서 Timestamp로 변환)
    private String occurredAt;
}
