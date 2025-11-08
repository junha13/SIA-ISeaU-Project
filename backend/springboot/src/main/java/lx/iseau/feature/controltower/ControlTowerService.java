package lx.iseau.feature.controltower;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ControlTowerService {

    @Autowired
    private ControlTowerDAO dao;

    // ============ 매니저 기본정보 조회 ============
    public ManagerInfoDTO getManagerInfoByManagerNumber(int managerNumber) {
        return dao.selectManagerInfoByManagerNumber(managerNumber);
    }


    // ============ 매니저 기본정보 수정 ============
    public int updateManagerInfoByManagerNumber(Map<String, Object> body) {
        // 기대 키: managerName(기존 식별), userName(새 이름), mobile, email
        String managerName = str(body.get("managerName"));
        String maName    = str(body.get("userName"));
        String mobile      = str(body.get("mobile"));
        String email       = str(body.get("email"));
        if (managerName == null || managerName.isBlank()) return 0;
        return dao.updateManagerInfoByManagerNumber(managerName, userName, mobile, email);
    }

    // ============ 처리 리스트 ============
    public List<TaskListDTO> getTaskListByManagerNumber(int managerNumber) {
        if (managerName == null || managerName.isBlank()) return List.of();
        return dao.getTaskListByManagerNumber(managerName);
    }

    // ============ 처리 상세 ============
    public TaskDetailDTO getTaskDetailByTaskNumber(int taskNumber) {
        return dao.getTaskDetailByTaskNumber(taskNumber);
    }

    // ============ 처리완료 플래그 ============
    public int markTaskProcessed(Map<String, Object> body) {
        Integer taskNumber = intOrNull(body.get("taskNumber"));
        Integer processed  = intOrNull(body.get("processed"));
        if (taskNumber == null) return 0;
        if (processed == null) processed = 1; // 기본값 1(처리완료)
        return dao.updateTaskProcessed(taskNumber, processed);
    }

    // ====== 유틸 ======
    private String str(Object o) { return (o == null) ? null : String.valueOf(o); }
    private Integer intOrNull(Object o) {
        try { return (o == null) ? null : Integer.parseInt(String.valueOf(o)); }
        catch (Exception e) { return null; }
    }
}
