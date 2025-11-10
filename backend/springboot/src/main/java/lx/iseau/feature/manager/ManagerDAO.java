package lx.iseau.feature.manager;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ManagerDAO {
    // 해당 관제센터에서 사용할 기본(최소) 담당자 - 가장 번호가 작은 사람 1명
    Integer findAnyManagerByControlTower(@Param("controlTowerNumber") Integer controlTowerNumber);
}
