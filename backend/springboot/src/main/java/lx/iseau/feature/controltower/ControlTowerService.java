package lx.iseau.feature.controltower;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ControlTowerService {

    private final ControlTowerDAO dao;
    private final int DEFAULT_MANAGER_NUMBER = 1; // 임시 매니저 번호

    // ============ 관제센터의 처리 리스트(지서) ============
    public List<TaskListDTO> getTaskListByControlTowerNumber(int controlTowerNumber) {
        return dao.getTaskListByControlTowerNumber(controlTowerNumber);
    }

    // ============ 매니저 기본정보 조회 ============
    public Map<String, Object> selectManagerInfoByManagerNumber(int managerNumber) {
    	Map<String, Object> map = new HashMap<String, Object>();
    	
    	ManagerInfoDTO dto = dao.selectManagerInfoByManagerNumber(managerNumber);
    	
    	map.put("result", dto);
        return map;
    }

    // ============ 매니저 기본정보 수정 ============
    public Map<String, Object> updateManagerInfoByManagerNumber(ManagerInfoDTO dto) {
    	Map<String, Object> map = new HashMap<>();
    	
    	map.put("result", dao.updateManagerInfoByManagerNumber(dto));
        return map;
    }

    // ============ 처리 리스트 ============
    public List<TaskListDTO> getTaskListByManagerNumber(int managerNumber) {
        return dao.getTaskListByManagerNumber(managerNumber);
    }

    // ============ 처리 상세 ============
    public TaskDetailDTO getTaskDetailByTaskNumber(int taskNumber)
    {
        return dao.getTaskDetailByTaskNumber(taskNumber);
    }

    // ============ 처리완료 플래그 ============
    public int updateTaskProcessed(Map<String, Object> body) {
        Integer taskNumber = intOrNull(body.get("taskNumber"));
        Integer processed  = intOrNull(body.get("processed"));
        if (taskNumber == null) return 0;
        if (processed == null) processed = 0; // 기본값 0(미처리)
        return dao.updateTaskProcessed(taskNumber, processed);
    }
    // ============ 워치 데이터 처리 및 Task 생성 ============
    @Transactional // 두 개 이상의 DAO 호출이 있어 트랜잭션 처리
    public void processHeartRateData(HeartRateRequest request) {

        // 1. DB 저장 (tb_watch에 Upsert)
        int updatedWatch = dao.insertWatchEvent(request);

        System.out.println("✅ Watch Event DB 저장 완료. userNumber: " + request.getUserNumber() + ", HR: " + request.getHeartRate());

        // 2. 긴급 알림 처리 (Task 생성)
        if (Boolean.TRUE.equals(request.getIsEmergency())) {
            // TODO: handleEmergencyAlert (FCM 발송 및 Task 생성) 로직 필요
            handleEmergencyAlert(request.getUserNumber());
        }
    }

    // 긴급 상황 처리 (Task 생성) - 분리된 비즈니스 로직
    private void handleEmergencyAlert(int userNumber) {
        // 긴급 상황 시 tb_task에 새 레코드 삽입
        int insertedTask = dao.insertTaskOnEmergency(DEFAULT_MANAGER_NUMBER, userNumber);

        if (insertedTask > 0) {
            System.out.println("🚨 긴급 Task 생성 완료. User: " + userNumber);
        }

        // TODO: 여기에 FCM 푸시 알림 로직 추가
    }

    // ====== 유틸 ======
    private String str(Object o) { return (o == null) ? null : String.valueOf(o); }
    private Integer intOrNull(Object o) {
        try {
            // Kotlin에서 전송된 Int가 Double로 인식될 수 있으므로, Double이면 int로 변환
            if (o instanceof Double) {
                return ((Double) o).intValue();
            }
            return (o == null) ? null : Integer.parseInt(String.valueOf(o));
        }
        catch (Exception e) { return null; }
    }
}