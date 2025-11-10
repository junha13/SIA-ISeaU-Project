package lx.iseau.feature.task;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  Task DTO
 * - tb_task 컬럼 + 목록용 조인 필드를 한 곳에 모음
 * - heartRate, spo2, occurredAt 는 조인(LEFT JOIN) 조회에서만 채워짐 → INSERT/UPDATE에는 사용 안 함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Integer taskNumber;
    private Integer taskAccepted;
    private Integer taskProcessed;
    private Integer managerNumber;
    private Integer watchNumber;
    // watch 테이블 조인으로 땡겨오는 애들
    private Integer heartRate;
    private Integer spo2; 
    private LocalDateTime occurredAt;
}
