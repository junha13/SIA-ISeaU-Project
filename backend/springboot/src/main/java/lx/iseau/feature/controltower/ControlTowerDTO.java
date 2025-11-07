package lx.iseau.feature.controltower;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관제센터(Control Tower) DTO
 *
 * - DB: tb_control_tower
 *   - control_tower_number (PK)
 *   - control_tower_name   (VARCHAR)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ControlTowerDTO {
    /** PK: 관제센터 고유 번호 (tb_control_tower.control_tower_number) */
    private Integer controlTowerNumber;

    /** 관제센터 이름 (tb_control_tower.control_tower_name) */
    private String controlTowerName;
}
