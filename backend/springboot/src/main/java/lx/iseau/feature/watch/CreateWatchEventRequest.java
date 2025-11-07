package lx.iseau.feature.watch;

/**
 * 워치에서 전송하는 최소 요청 바디
 * - 심박수/산소포화도/발생시각만 받습니다.
 * - userNumber는 세션에서 주입(로그인 사용자)합니다.
 */
public class CreateWatchEventRequest {
    // 심박수 (bpm)
    private Integer heartRate;
    // 산소포화도 (SpO2, %)
    private Integer spo2;
    // 이벤트 발생 시각 (ISO8601 문자열로 받되, 컨트롤러에서 Timestamp로 변환)
    private String occurredAt;

    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }

    public Integer getSpo2() { return spo2; }
    public void setSpo2(Integer spo2) { this.spo2 = spo2; }

    public String getOccurredAt() { return occurredAt; }
    public void setOccurredAt(String occurredAt) { this.occurredAt = occurredAt; }
}
