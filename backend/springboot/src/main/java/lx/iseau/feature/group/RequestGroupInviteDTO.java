package lx.iseau.feature.group;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 그룹 멤버 초대 요청 DTO (POST /api/groups/invite)
 * DB: tb_group_member 테이블에 status='PENDING' 행을 삽입
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestGroupInviteDTO {

    private Integer groupId;
    private String targetUserId;
    private String markerColor;
    
    private Integer inviterUserNumber;       
    private Integer targetUserNumber; 
}