package lx.iseau.feature.controltower;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 매니저 기본정보 조회/수정용 DTO
 * - 매니저 이름/전화/이메일만 노출/수정
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerInfoDTO {
	// tb_user.user_number (식별 키)
	private int managerNumber;
	
    // tb_user.user_name
    private String managerName;

    // tb_user.mobile
    private String mobile;

    // tb_user.email
    private String email;
}
