package lx.iseau.feature.group;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 그룹 알림 설정을 저장/조회하기 위한 DTO (tb_group_settings 매핑)
 * CHAR(1) 필드들은 'Y' 또는 'N'으로 전송됩니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestGroupSettingsDTO {

    // 💡 [추가] Service에서 세션의 userNumber를 저장하기 위한 필드 (DB 매핑 X)
    private Integer userNumber; 
    
    private Integer groupNumber; // 그룹 식별자 (PK)
    
    // 알림 설정 (CHAR(1) 필드, 'Y' 또는 'N')
    private String tideAlert;
    private String groupLeaveLevel1Alert;
    private String groupLeaveLevel2Alert;
    private String groupLeaveLevel3Alert;

    // 거리 설정 (Double Precision)
    private Double groupLeaveLevel1Distance;
    private Double groupLeaveLevel2Distance;
    private Double groupLeaveLevel3Distance;
}