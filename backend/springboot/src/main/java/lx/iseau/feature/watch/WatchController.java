package lx.iseau.feature.watch;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/watch")
@RestController
public class WatchController {

    @Autowired
    private WatchService service;

    // ========== [워치] 위험 평가 → 매니저(1:1) 배정 → 업무(tb_task) 생성 ==========
    // body 예) { "watchNumber": 123 }
    @RequestMapping("/evaluateAndDispatch")
    public ResponseEntity<?> evaluateAndDispatch(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = service.evaluateAndDispatch(body);
        return ResponseEntity
                .ok()
                .header("api", "watch/evaluateAndDispatch")
                .body(Map.of("result", result));
    }
}
