package lx.iseau.feature.group;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 위험 알림 요청 시 사용되는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestDangerAlert {
    private Double latitude;
    private Double longitude;
    // 이외의 추가 정보 (예: 상황 설명 등)를 필요에 따라 추가할 수 있습니다.
}