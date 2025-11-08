package lx.iseau.feature.controltower;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 매니저 요약 정보 DTO
 * - 매니저 프로필 + 관제타워명 + 처리건수 요약
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerSummaryDTO {
    // u.user_number (매니저 = 사용자 FK)
    private Integer managerNumber;

    // u.user_name
    private String managerName;

    // u.mobile
    private String mobile;

    // u.email
    private String email;

    // ct.control_tower_name
    private String controlTowerName;

    // 총 처리건수, 처리완료/대기 카운트
    private Integer totalTasks;
    private Integer processedTasks;
    private Integer pendingTasks;
}
