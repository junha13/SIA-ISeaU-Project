package lx.iseau.feature.group;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDTO {

    private Integer invitationId;      // 초대 ID (PK)
    private Integer groupId;           // 그룹 ID (FK to tb_group)
    private Integer inviterUserNumber; // 초대한 사람 사용자 번호 (FK to tb_user)
    private Integer targetUserNumber;  // 초대받은 사람 사용자 번호 (FK to tb_user)
    private String status;            // 초대 상태 (예: 'PENDING', 'ACCEPTED', 'REJECTED')
    private String markerColor;       // 마커 색상
    private LocalDateTime createdAt;     // 초대 생성 시간
    // private LocalDateTime updatedAt;  // 상태 변경 시간 (필요시 추가)
}