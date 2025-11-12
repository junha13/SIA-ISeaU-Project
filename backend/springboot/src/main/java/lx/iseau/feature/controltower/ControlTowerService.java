package lx.iseau.feature.controltower;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ControlTowerService {

    private final ControlTowerDAO dao;

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

    // ====== 유틸 ======
    private String str(Object o) { return (o == null) ? null : String.valueOf(o); }
    private Integer intOrNull(Object o) {
        try { return (o == null) ? null : Integer.parseInt(String.valueOf(o)); }
        catch (Exception e) { return null; }
    }
}
