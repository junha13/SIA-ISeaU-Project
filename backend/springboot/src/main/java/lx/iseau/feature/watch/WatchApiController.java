// lx.iseau.feature.watch.WatchApiController.java
package lx.iseau.feature.watch;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/watch")
@RequiredArgsConstructor // Autowired 대신 final 필드를 생성자 주입
public class WatchApiController {

    // WatchService는 final로 선언하고 RequiredArgsConstructor로 주입받습니다.
    private final WatchService watchService;

    /**
     * Wear OS에서 휴대폰을 거쳐 전송된 심박수 데이터를 수신합니다.
     * 엔드포인트: POST /api/watch/heart-rate
     *
     * @param request HeartRateRequest DTO (userNumber, heartRate, occurredAt, isEmergency 포함)
     * @return 성공/실패 응답
     */
    @PostMapping("/heart-rate")
    public ResponseEntity<?> receiveHeartRate(@RequestBody HeartRateRequest request) {
        // 필수 필드 검사
        if (request.getUserNumber() == null || request.getHeartRate() == null || request.getOccurredAt() == null) {
            System.err.println("❌ Watch API: 필수 필드 누락.");
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Required fields missing."));
        }

        try {
            // Service 호출 (DB 저장 및 FCM 발송 처리)
            watchService.processHeartRateData(request);
            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            System.err.println("❌ Watch API: 데이터 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "Server processing error."));
        }
    }

    /**
     * DB에 저장된 특정 Watch 데이터를 조회합니다.
     * 엔드포인트: GET /api/watch/data/{watchNumber}
     */
    @GetMapping("/data/{watchNumber}")
    public ResponseEntity<?> getWatchData(@PathVariable int watchNumber) {
        // 1. Service 호출하여 DB에서 데이터 조회
        Map<String, Object> data = watchService.getWatchData(watchNumber);

        if (data != null && !data.isEmpty()) {
            System.out.println("✅ Watch Data Retrieved: " + data);
            return ResponseEntity.ok(data); // 200 OK와 함께 데이터 반환
        } else {
            System.out.println("❌ Watch Data Not Found for ID: " + watchNumber);
            return ResponseEntity.notFound().build(); // 404 Not Found 반환
        }
    }
}