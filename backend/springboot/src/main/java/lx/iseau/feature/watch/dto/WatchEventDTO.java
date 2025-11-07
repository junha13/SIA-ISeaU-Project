package lx.iseau.feature.watch.dto;

import java.sql.Timestamp;

/**
 * tb_watch 매핑용 DTO (map-underscore-to-camel-case ON 가정)
 */
public class WatchEventDTO {
    private Integer watchNumber;   // PK
    private Integer heartRate;     // 심박수
    private Integer spo2;          // SpO2
    private Timestamp occurredAt;  // 발생시각
    private Integer userNumber;    // 사용자 FK

    public Integer getWatchNumber() { return watchNumber; }
    public void setWatchNumber(Integer watchNumber) { this.watchNumber = watchNumber; }

    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }

    public Integer getSpo2() { return spo2; }
    public void setSpo2(Integer spo2) { this.spo2 = spo2; }

    public Timestamp getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Timestamp occurredAt) { this.occurredAt = occurredAt; }

    public Integer getUserNumber() { return userNumber; }
    public void setUserNumber(Integer userNumber) { this.userNumber = userNumber; }
}
