package lx.iseau.feature.controltower;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/controltower")
@RestController
public class ControlTowerController {

    @Autowired
    private ControlTowerService service;

    // =========================
    // 매니저 기본정보 조회 
    // =========================
    @RequestMapping("/manager/info")
    public ResponseEntity<?> selectManagerInfoByManagerNumber(@RequestParam int managerNumber) {
    	// TODO: 로그인 붙이면 여기서 인증값으로 교체 예정 (managerNumber 무시)
    	Map<String, Object> result = service.selectManagerInfoByManagerNumber(managerNumber);
        return ResponseEntity
        		.ok()
                .body(Map.of("result", result));
    }

    // =========================
    // 매니저 기본정보 수정 (이름, 전화, 이메일)
    // =========================
    @RequestMapping("/manager/info/update")
    public ResponseEntity<?> updateManagerInfo(@PathVariable int managerNumber,
    		@RequestBody ManagerInfoDTO dto) {
    	dto.setManagerNumber(managerNumber);
    	Map<String, Object> result = service.updateManagerInfoByManagerNumber(dto);
        return ResponseEntity
                .ok()
                .body(Map.of("result", result));
    }

    // =========================
    // 매니저 처리 리스트 (간단 목록)
    // =========================
    @RequestMapping("/task/list")
    public ResponseEntity<?> getTaskListByManagerNumber(@RequestParam int managerNumber) {
        // TODO: 로그인 붙이면 여기서 인증값으로 교체 예정 (managerNumber 무시)
    	List<TaskListDTO> result = service.getTaskListByManagerNumber(managerNumber);
        return ResponseEntity
        		.ok()
        		.body(Map.of("result", result));
    }

    // =========================
    // 처리 상세정보 (taskNumber 기준 단건)
    // 리턴: user(location, birth_date, gender, watch(occurred_at, heart_rate, spo2), task(task_processed))
    // =========================
    @RequestMapping("/task/detail")
    public ResponseEntity<?> getTaskDetailByTaskNumber(@RequestParam int taskNumber) {
        TaskDetailDTO map = service.getTaskDetailByTaskNumber(taskNumber);
        return ResponseEntity
                .ok()
                .body(Map.of("result", map));
    }

    // =========================
    // 처리완료/취소 플래그 업데이트
    // Body: { "taskNumber": 123, "processed": 0 }  // 기본 0
    // =========================
    @RequestMapping("/task/markProcessed")
    public ResponseEntity<?> markTaskProcessed(@RequestBody Map<String, Object> body) {
        int updated = service.updateTaskProcessed(body);
        return ResponseEntity
                .ok()
                .body(Map.of("result", Map.of("updated", updated)));
    }
}
