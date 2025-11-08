package lx.iseau.feature.controltower;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 관제/매니저 조회 DAO
 */
@Mapper
public interface ControlTowerDAO {

    // 매니저 기본정보 조회
    ManagerInfoDTO selectManagerInfoByManagerNumber(@Param("managerNumber") int managerNumber);

    // 매니저 기본정보 수정
    int updateManagerInfoByManagerNumber(@Param("managerNumber") int managerNumber,
							             @Param("managerName") String managerName,
							             @Param("mobile") String mobile,
							             @Param("email") String email);

    // 처리 리스트
    List<TaskListDTO> getTaskListByManagerNumber(@Param("managerNumber") int managerNumber);
    
    // 처리 상세(taskNumber)
    TaskDetailDTO getTaskDetailByTaskNumber(@Param("taskNumber") int taskNumber);

    // 처리완료 플래그 업데이트
    int updateTaskProcessed(@Param("taskNumber") int taskNumber,
                            @Param("taskProcessed") int taskProcessed);
}
