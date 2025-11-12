package lx.iseau.feature.controltower;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 처리 상세 화면용 DTO
 * - user: location(WKT), birth_date, gender
 * - watch: occurred_at, heart_rate, spo2
 * - task: task_processed
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailDTO {
    private Integer taskNumber;
    private Integer taskProcessed;

    private String  occurredAt;     // timestamp → 문자열 매핑(기본)
    private Integer heartRate;

    private String  birthDate;      // DATE → 문자열 매핑(기본)
    private String  gender;         // CHAR(1)
    
    // 신고자 위치(tb_user.location -> lat/lon)
    private Double userLat; // ST_Y
    private Double userLon; // ST_X
}
