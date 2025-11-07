package lx.iseau.feature.controltower;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MyBatis Mapper(=DAO). XML은 resources/mapper/mapper-control-tower.xml
 */
@Mapper
public interface ControlTowerDAO {
    // Control Tower CRUD
    int insertControlTower(@Param("name") String name);
    ControlTowerDTO selectControlTowerById(@Param("id") Integer id);
    List<ControlTowerDTO> selectControlTowers();
    int updateControlTowerName(@Param("id") Integer id, @Param("name") String name);
    int deleteControlTower(@Param("id") Integer id);

    // 참조 체크(삭제 보호)
    int countBeachesUsingTower(@Param("id") Integer id);
    int countManagersUsingTower(@Param("id") Integer id);

    // Managers under a tower
    List<ManagerSummaryDTO> selectManagersByTower(@Param("id") Integer controlTowerNumber);
    int insertManagerToTower(@Param("controlTowerNumber") Integer controlTowerNumber,
                             @Param("managerUserNumber") Integer managerUserNumber);
    int deleteManagerFromTower(@Param("controlTowerNumber") Integer controlTowerNumber,
                               @Param("managerUserNumber") Integer managerUserNumber);
}