package lx.iseau.feature.control;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

/**
 * 관제(컨트롤타워/매니저) 관련 API
 * - 로직은 Service로 위임
 * - 응답은 Map.of("result", ...) 형태로 통일
 */
@RestController
@RequestMapping("/api/control")
@RequiredArgsConstructor
public class ControlTowerController {

    private final ControlTowerService service;

    /** 관제타워 "이름"으로 매니저 요약 조회 (한 타워 = 한 명) */
    @RequestMapping("/manager/summary/by-tower-name/{controlTowerName}")
    public ResponseEntity<?> getManagerSummaryByTowerName(@PathVariable String controlTowerName) {
        Map<String, Object> result = service.getManagerSummaryByTowerName(controlTowerName);
        return ResponseEntity
                .ok()
                .header("api", "control/manager/summary/by-tower-name")
                .body(Map.of("result", result));
    }

    /** 매니저 "이름"으로 매니저 요약 조회 */
    @RequestMapping("/manager/summary/by-manager-name/{managerName}")
    public ResponseEntity<?> getManagerSummaryByManagerName(@PathVariable String managerName) {
        Map<String, Object> result = service.getManagerSummaryByManagerName(managerName);
        return ResponseEntity
                .ok()
                .header("api", "control/manager/summary/by-manager-name")
                .body(Map.of("result", result));
    }
}
