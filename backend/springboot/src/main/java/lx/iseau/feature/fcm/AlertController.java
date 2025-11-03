// AlertController.java
package lx.iseau.feature.fcm;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alert") // 💡 [핵심] 모바일 앱에서 요청하는 엔드포인트와 일치시킵니다.
@RequiredArgsConstructor
public class AlertController {

    private final FcmService fcmService;

    /**
     * Wear OS 클라이언트 (휴대폰 앱)로부터 긴급 경고를 수신하고 FCM을 발송합니다.
     * 최종 엔드포인트: POST /api/alert/wear
     */
    @PostMapping("/wear")
    public ResponseEntity<String> handleWearAlert(@RequestBody WearAlertRequest request) {

        // 1. 유효성 검사
        if (request.getTargetUserId() == null || request.getAlertMessage() == null) {
            return ResponseEntity.badRequest().body("TargetUserId or AlertMessage missing.");
        }

        System.out.println("🚨 Wear Alert Received: " + request.toString());

        // 2. FCM 발송 서비스 호출
        fcmService.sendAlertNotification(
                request.getTargetUserId(),
                request.getAlertMessage(),
                request.getTimestamp()
        );

        return ResponseEntity.ok("Wear alert received and FCM processing started.");
    }
}