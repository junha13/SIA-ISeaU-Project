package lx.iseau.feature.controltower;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 관제센터(Control Tower) 최소 API
 * - 목록/단건/생성/수정/삭제
 * - 매니저 목록/추가/삭제
 *
 * URL 설계
 * GET    /api/control-towers
 * POST   /api/control-towers
 * GET    /api/control-towers/{id}
 * PATCH  /api/control-towers/{id}
 * DELETE /api/control-towers/{id}
 * GET    /api/control-towers/{id}/managers
 * POST   /api/control-towers/{id}/managers
 * DELETE /api/control-towers/{id}/managers/{managerId}
 */
@RestController
@RequestMapping("/api/control-towers")
public class ControlTowerController {

    private final ControlTowerService service;

    public ControlTowerController(ControlTowerService service) {
        this.service = service;
    }

    /** 목록 */
    @GetMapping
    public Map<String, Object> list() {
        List<ControlTowerDTO> data = service.list();
        return Map.of("data", data);
    }

    /** 단건 */
    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Integer id) {
        ControlTowerDTO data = service.get(id);
        return Map.of("data", data);
    }

    /** 생성 */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateControlTowerRequest req) {
        Integer id = service.create(req.getControlTowerName());
        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        body.put("controlTowerNumber", id);
        return ResponseEntity.ok(body);
    }

    /** 이름 수정 */
    @PatchMapping("/{id}")
    public Map<String, Object> patch(@PathVariable Integer id, @RequestBody UpdateControlTowerRequest req) {
        service.rename(id, req.getControlTowerName());
        return Map.of("ok", true);
    }

    /** 삭제 (참조 남아 있으면 400 반환) */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", ex.getMessage()));
        }
    }

    /** 매니저 목록 */
    @GetMapping("/{id}/managers")
    public Map<String, Object> listManagers(@PathVariable Integer id) {
        List<ManagerSummaryDTO> data = service.listManagers(id);
        return Map.of("data", data);
    }

    /**
     * 매니저 배정
     * - req.managerUserNumber는 tb_user.user_number 여야 함
     * - 설계상 tb_manager.manager_number = user_number (동일키) 이므로,
     *   INSERT 시 manager_number에 그대로 넣어야 FK가 성립.
     */
    @PostMapping("/{id}/managers")
    public Map<String, Object> addManager(@PathVariable Integer id,
                                          @RequestBody ManagerAssignmentRequest req) {
        service.addManager(id, req.getManagerUserNumber());
        return Map.of("ok", true);
    }

    /** 매니저 해제 */
    @DeleteMapping("/{id}/managers/{managerId}")
    public Map<String, Object> deleteManager(@PathVariable Integer id,
                                             @PathVariable Integer managerId) {
        service.removeManager(id, managerId);
        return Map.of("ok", true);
    }
}