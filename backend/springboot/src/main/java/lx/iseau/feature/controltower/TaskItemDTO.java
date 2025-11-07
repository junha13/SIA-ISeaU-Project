package lx.iseau.feature.controltower;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 매니저의 개별 처리 건(태스크) DTO
 * - tb_task + tb_watch + tb_user(location) 조인 결과 최소필드
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskItemDTO {
    // tb_task
    private Integer taskNumber;
    private Integer taskAccepted;   // 0/1
    private Integer taskProcessed;  // 0/1

    // tb_watch
    private LocalDateTime occurredAt;
    private Integer heartRate;
    private Integer spo2;

    // 신고자 위치(tb_user.location -> lat/lon)
    private Double userLat; // ST_Y
    private Double userLon; // ST_X
}
