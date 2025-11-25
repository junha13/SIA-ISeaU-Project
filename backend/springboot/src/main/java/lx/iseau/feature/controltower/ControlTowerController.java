package lx.iseau.feature.controltower;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lx.iseau.feature.voice.VoiceWebSocketHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/api/controltower")
@RestController
@RequiredArgsConstructor
public class ControlTowerController {

    private final ControlTowerService service;
    private final TtsService ttsService;
	private final VoiceWebSocketHandler voiceWebSocketHandler;

    // ============ 로그 데이터 조회(지서) ============
    @GetMapping("/task/log")
    public ResponseEntity<?> getTaskLogByUserNumber(@RequestParam int userNumber) {
        List<TaskLogDTO> result = service.getTaskLogByUserNumber(userNumber);
        return ResponseEntity
                .ok()
                .body(Map.of("result", result));
    }

    // ============ 관제센터의 처리 리스트(지서) ============
    @GetMapping("/task/list/controltower")
    public ResponseEntity<?> getTaskListByControlTowerNumber(@RequestParam int controlTowerNumber) {
        List<ControlTowerTaskListDTO> result = service.getTaskListByControlTowerNumber(controlTowerNumber);
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
        log.info("📥 /api/controltower/heart-rate 요청 수신: {}", request);

        try { 
            // 1) 기본 유효성 검사
            if (request.getUserNumber() == null || request.getUserNumber() <= 0) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "userNumber is required")
                );
            }
            if (request.getHeartRate() == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("success", false, "message", "heartRate is required")
                );
            }

            // 2) (문제 원인 찾으려고) 일단 DB 저장은 잠깐 막고, 로그만 찍게 해도 됨
            service.insertWatchEvent(request);

            log.info("✅ 심박 데이터 처리 완료: {}", request);

            return ResponseEntity.ok(Map.of("success", true));

        } catch (Exception e) {
            log.error("❌ 심박 데이터 처리 or 위치(고도 포함) 처리 중 에러 발생", e);
            return ResponseEntity
                    .status(500)
                    .body(Map.of(
                            "success", false,
                            "message", "Server processing error.",
                            "detail", e.getClass().getSimpleName() + ": " + e.getMessage()
                    ));
        }
    }
 // ============= 🚨 [추가] 수동 신고 데이터 수신 및 Task 생성 ===============//
    	@PostMapping("/manual-report")
        public ResponseEntity<?> handleManualReport(@RequestBody ManualReportRequest request) {
            log.info("📥 /api/controltower/manual-report 요청 수신: {}", request);

            try {
                // 1) 필수 유효성 검사
                if (request.getUserNumber() == null || request.getUserNumber() <= 0 ||
                    request.getLatitude() == null || request.getLongitude() == null) {
                     return ResponseEntity.badRequest().body(
                             Map.of("success", false, "message", "User/Location data is required for manual report.")
                     );
                }

                // 2) DB 저장 및 Task 생성/FCM 알림 발송
                service.handleManualReport(request);

                log.info("✅ 수동 신고 처리 완료: {}", request.getUserNumber());

                return ResponseEntity.ok(Map.of("success", true, "message", "긴급 신고가 관제소에 접수되었습니다."));

            } catch (Exception e) {
                log.error("❌ 수동 신고 처리 중 에러 발생", e);
                return ResponseEntity
                        .status(500)
                        .body(Map.of(
                                "success", false,
                                "message", "Server processing error during manual report.",
                                "detail", e.getClass().getSimpleName() + ": " + e.getMessage()
                        ));
            }
        }
    	// =========================
        // 🚨 [추가] 수동 신고 전용 목록 조회
        // Endpoint: GET /api/controltower/task/manual-list
        // =========================
        @GetMapping("/task/manual-list")
        public ResponseEntity<?> getTaskManualList(@RequestParam int managerNumber) {


            // Service의 새로운 전용 메서드 호출 (수동 신고 Task만 조회)
            List<TaskListDTO> result = service.getTaskManualListByManagerNumber(managerNumber);

            return ResponseEntity
                    .ok()
                    .body(Map.of("result", result));
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
 // 🚨 [수정]: 경로에 managerNumber를 포함하고 @PutMapping을 사용합니다.
    @PutMapping("/manager/info/{managerNumber}")
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

    @PostMapping("/tts")
    public ResponseEntity<?> sendTtsBroadcast(@RequestBody TtsBroadcastRequest req) {
	    try {
	        System.out.println("[TTS 방송 요청] beachNumber=" + req.getBeachNumber()
	                + ", cctvName=" + req.getCctvName()
	                + ", message=" + req.getMessage());

	        // 🔹 실제 TTS 호출 → Base64 MP3 생성
	        String audioBase64 = ttsService.synthesizeToBase64(req.getMessage());

	        // 🔊🔊🔊 여기 추가: WebSocket으로 /voice 접속한 단말들에게 뿌리기
	        voiceWebSocketHandler.broadcastTts(
	                req.getCctvName(),   // 어떤 CCTV에서 나온 방송인지
	                req.getMessage(),    // 안내 문구
	                audioBase64          // Base64 MP3 데이터
	        );

	        // 🔁 원래대로 프론트(관제 화면)에도 응답 리턴
	        return ResponseEntity.ok().body(
	                Map.of(
	                        "success", true,
	                        "audioContent", audioBase64
	                )
	        );
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.internalServerError().body(
	                Map.of(
	                        "success", false,
	                        "message", "TTS 처리 중 오류가 발생했습니다.",
	                        "detail", e.getMessage()
	                )
	        );
	    }
	}

}