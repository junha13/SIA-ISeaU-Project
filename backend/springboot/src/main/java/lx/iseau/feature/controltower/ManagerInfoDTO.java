package lx.iseau.feature.controltower;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 매니저 기본정보 DTO
 * - tb_manager.manager_number(=tb_user.user_number FK) 기준으로 사용자 정보 묶음
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerInfoDTO {
    private Integer managerNumber; // 매니저(=user) 번호
    private String  userName;      // 매니저 이름(tb_user.user_name)
    private String  mobile;        // 전화번호(tb_user.mobile)
    private String  email;         // 이메일(tb_user.email)
}
