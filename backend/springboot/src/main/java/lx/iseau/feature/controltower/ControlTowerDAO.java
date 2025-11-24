package lx.iseau.feature.controltower;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 관제/매니저 조회 DAO
 */
@Mapper
public interface ControlTowerDAO {
    // 로그 데이터 조회(지서)
    List<TaskLogDTO> getTaskLogByUserNumber(@Param("userNumber") int userNumber);
    
    // 관제센터의 처리 리스트(지서)
    List<ControlTowerTaskListDTO> getTaskListByControlTowerNumber(@Param("controlTowerNumber") int controlTowerNumber);

    // 매니저 기본정보 조회
    ManagerInfoDTO selectManagerInfoByManagerNumber(@Param("managerNumber") int managerNumber);

    // 매니저 기본정보 수정
    int updateManagerInfoByManagerNumber(ManagerInfoDTO dto);

    //user_number 기준으로 tb_watch 데이터 저장
    int insertWatchEvent(HeartRateRequest request); // HeartRateRequest DTO를 직접 받도록 변경

    //긴급 상황 발생 시 tb_task에 새 레코드 삽입
    int insertTaskOnEmergency(@Param("managerNumber") int managerNumber,
                              @Param("userNumber") int userNumber);

    // 처리 리스트
    List<TaskListDTO> getTaskListByManagerNumber(@Param("managerNumber") int managerNumber);
    
    // 처리 상세(taskNumber)
    TaskDetailDTO getTaskDetailByTaskNumber(@Param("taskNumber") int taskNumber);

    // 처리완료 플래그 업데이트
    int updateTaskProcessed(@Param("taskNumber") int taskNumber,
                            @Param("taskProcessed") int taskProcessed);
}
