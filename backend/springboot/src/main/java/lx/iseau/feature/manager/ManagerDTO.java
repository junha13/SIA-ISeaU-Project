package lx.iseau.feature.manager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 매니저(관제요원) DTO
 *
 * - DB: tb_manager
 *   - manager_number        (SERIAL PK)  ← 설계상 tb_user.user_number와 동일하게 사용
 *   - control_tower_number  (INT, NOT NULL)
 *
 * - 주의:
 *   manager_number는 사용자(user_number)를 그대로 재사용하는 구조입니다.
 *   (FK: tb_manager.manager_number → tb_user.user_number)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDTO {
    /** PK & FK to tb_user.user_number (tb_manager.manager_number) */
    private Integer managerNumber;

    /** 소속 관제센터 번호 (tb_manager.control_tower_number) */
    private Integer controlTowerNumber;
}
