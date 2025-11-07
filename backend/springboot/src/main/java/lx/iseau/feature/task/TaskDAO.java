package lx.iseau.feature.task;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TaskDAO {
    // 태스크 생성
    int insertTask(TaskDTO dto);

    // 관제센터별 대기 태스크 조회 (accepted=0, processed=0)
    List<TaskDTO> selectPendingTasksByControlTower(@Param("controlTowerNumber") Integer controlTowerNumber);

    // 매니저 본인에게 배정된 진행중 태스크(accepted=1, processed=0)
    List<TaskDTO> selectOpenTasksByManager(@Param("managerNumber") Integer managerNumber);

    // 태스크 수락(accepted=1로 업데이트, 본인 소유만)
    int acceptTask(@Param("taskNumber") Integer taskNumber, @Param("managerNumber") Integer managerNumber);

    // 태스크 완료(processed=1)
    int completeTask(@Param("taskNumber") Integer taskNumber, @Param("managerNumber") Integer managerNumber);
}
