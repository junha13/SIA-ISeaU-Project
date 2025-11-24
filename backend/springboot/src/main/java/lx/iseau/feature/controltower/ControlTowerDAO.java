package lx.iseau.feature.controltower;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 관제/매니저 조회 DAO
 */
@Mapper
public interface ControlTowerDAO {
    // 관제센터의 처리 리스트(지서)
    List<TaskListDTO> getTaskListByControlTowerNumber(@Param("controlTowerNumber") int controlTowerNumber);

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
 // 🚨 [수정 및 재정렬] 수동 신고 목록 조회 메서드
    List<TaskListDTO> getTaskManualListByManagerNumber(@Param("managerNumber") int managerNumber);

    // 🚨 [수정 및 재정렬] 수동 신고 INSERT 메서드 (Service에서 호출)
    int insertTaskOnManualReport(@Param("managerNumber") int managerNumber,
            @Param("userNumber") Integer userNumber,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("reportType") String reportType);
}