package lx.iseau.feature.controltower;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/controltower")
@RestController
@RequiredArgsConstructor
public class ControlTowerController {

    private final ControlTowerService service;

    // ============ 관제센터의 처리 리스트(지서) ============
    @GetMapping("/task/list/controltower")
    public ResponseEntity<?> getTaskListByControlTowerNumber(@RequestParam int controlTowerNumber) {
        List<TaskListDTO> result = service.getTaskListByControlTowerNumber(controlTowerNumber);
        return ResponseEntity
                .ok()
                .body(Map.of("result", result));
    }

    // =========================
    // 워치 심박수/긴급 상황 데이터 수신
    // Endpoint: POST /api/controltower/heart-rate (경로가 이상할 수 있음)
    // 혹은 @RequestMapping("/api/controltower")로 Controller 레벨 경로를 변경해야 함
    // =========================
    @PostMapping("/heart-rate")
    public ResponseEntity<?> receiveHeartRate(@RequestBody HeartRateRequest request) {
        // 필수 필드 검사
        if (request.getUserNumber() == null || request.getHeartRate() == null || request.getOccurredAt() == null) {
            System.err.println("❌ Watch API: 필수 필드 누락." + request);
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "Required fields missing."));
        }

        try {
            service.processHeartRateData(request);
            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            System.err.println("❌ Watch API: 데이터 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Server processing error."));
        }
    }

    // =========================
    // 매니저 기본정보 조회 
    // =========================
    @GetMapping("/manager/info")
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
    @GetMapping("/task/list")
    public ResponseEntity<?> getTaskListByManagerNumber(@RequestParam int managerNumber) {
        // TODO: 로그인 붙이면 여기서 인증값으로 교체 예정 (managerNumber 무시)
    	List<TaskListDTO> result = service.getTaskListByManagerNumber(managerNumber);
        return ResponseEntity
        		.ok()
        		.body(Map.of("result", result));
    }

    // =========================
    // 처리 상세정보 (taskNumber 기준 단건)
    // 리턴: user(location, birth_date, gender, watch(occurred_at, heart_rate), task(task_processed))
    // =========================
    @GetMapping("/task/detail")
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
    @GetMapping("/task/markProcessed")
    public ResponseEntity<?> markTaskProcessed(@RequestBody Map<String, Object> body) {
        int updated = service.updateTaskProcessed(body);
        return ResponseEntity
                .ok()
                .body(Map.of("result", Map.of("updated", updated)));
    }
}
