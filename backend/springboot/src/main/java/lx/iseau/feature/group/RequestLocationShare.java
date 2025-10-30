package lx.iseau.feature.group;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

// --- 공통 응답 ---
@Data
@NoArgsConstructor
@AllArgsConstructor
class RequestLocationShare {
    private Integer invitationId; // 초대 식별 ID (DB 설계 필요)
    private Integer groupId;
}