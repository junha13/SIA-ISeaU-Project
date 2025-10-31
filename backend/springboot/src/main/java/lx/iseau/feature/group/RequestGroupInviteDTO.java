package lx.iseau.feature.group;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 그룹 초대 요청 (POST /groups/invite)에 사용되는 DTO
 */
@Data 
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 전체 필드 생성자
public class RequestGroupInviteDTO {

    private Integer groupId;
    private String targetUserId;
    private String markerColor;
    private Integer inviterUserNumber;
    private Integer targetUserNumber;  // 초대 대상자의 user_number (PK)
} 