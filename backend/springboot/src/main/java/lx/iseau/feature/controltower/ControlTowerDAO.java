package lx.iseau.feature.controltower;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 관제/매니저 조회 DAO (XML 매퍼 사용)
 * - 이름 기반 조회 전용
 */
@Mapper
public interface ControlTowerDAO {

    /** 관제타워 이름으로 매니저 단건 조회 (타워당 매니저 1명 전제) */
    ManagerInfoDTO selectManagerByControlTowerName(@Param("controlTowerName") String controlTowerName);

    /** 매니저(사용자) 이름으로 매니저 단건 조회 */
    ManagerInfoDTO selectManagerByUserName(@Param("userName") String userName);

    /** 매니저번호로 처리 리스트 조회 (task + watch + user(location)) */
    List<TaskItemDTO> selectTasksByManager(@Param("managerNumber") Integer managerNumber);
}
