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
    @RequestMapping("/manager/info/get")
    public ResponseEntity<?> getManagerInfoByManagerNumber(@PathVariable int managerNumber) {
    	// TODO: 로그인 붙이면 여기서 인증값으로 교체 예정 (managerNumber 무시)
    	Map<String, Object> map = service.getManagerInfoByManagerNumber(managerNumber);
        return ResponseEntity
        		.ok()
                .body(Map.of("result", map));
    }

    // =========================
    // 매니저 기본정보 수정 (이름, 전화, 이메일)
    // Body: { "managerName":"홍길동", "userName":"새이름", "mobile":"010-...", "email":"..." }
    // =========================
    @RequestMapping("/manager/info/update")
    public ResponseEntity<?> updateManagerInfo(@RequestBody Map<String, Object> body) {
        int updated = service.updateManagerInfoByManagerName(body);
        return ResponseEntity
                .ok()
                .header("api", "controltower/manager/info/update")
                .body(Map.of("result", updated));
    }

    // =========================
    // 매니저 처리 리스트 (간단 목록)
    // =========================
    @RequestMapping("/tasks/list")
    public ResponseEntity<?> getTaskListByManagerNumber(@PathVariable int managerNumber) {
        // TODO: 로그인 붙이면 여기서 인증값으로 교체 예정 (managerNumber 무시)
    	List<TaskListDTO> list = service.getTaskListByManagerNumber(managerNumber);
        return ResponseEntity
        		.ok()
        		.body(Map.of("result", list));
    }

    // =========================
    // 처리 상세정보 (taskNumber 기준 단건)
    // Path: /tasks/detail/{taskNumber}
    // 리턴: user(location, birth_date, gender, watch(occurred_at, heart_rate, spo2), task(task_processed))
    // =========================
    @RequestMapping("/tasks/detail/{taskNumber}")
    public ResponseEntity<?> getTaskDetailByTaskNumber(@PathVariable int taskNumber) {
        TaskDetailDTO map = service.getTaskDetailByTaskNumber(taskNumber);
        return ResponseEntity
                .ok()
                .body(Map.of("result", map));
    }

    // =========================
    // 처리완료/취소 플래그 업데이트
    // Body: { "taskNumber": 123, "processed": 1 }  // 기본 1
    // =========================
    @RequestMapping("/tasks/markProcessed")
    public ResponseEntity<?> markTaskProcessed(@RequestBody Map<String, Object> body) {
        int updated = service.markTaskProcessed(body);
        return ResponseEntity
                .ok()
                .header("api", "controltower/tasks/markProcessed")
                .body(Map.of("result", Map.of("updated", updated)));
    }
}
