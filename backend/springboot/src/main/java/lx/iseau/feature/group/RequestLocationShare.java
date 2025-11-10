package lx.iseau.feature.group;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 초대 수락/거절 요청 DTO (POST /groups/invitations/accept, /reject)
 * DB: tb_group_member 테이블의 status 컬럼을 업데이트/삭제하는 데 사용됨.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestLocationShare {

	private Integer invitationId;
    
  
}