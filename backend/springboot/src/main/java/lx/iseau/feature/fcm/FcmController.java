package lx.iseau.feature.fcm;

import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
public class FcmController {

    // FCM 토큰 저장 및 조회 서비스
    private final FcmService fcmService;

    // FCM 알림 발송 서비스
    private final NotificationService notificationService;

    /**
     * 클라이언트(Vue App)에서 전송한 FCM 토큰을 수신하여 저장/업데이트합니다.
     * 엔드포인트: POST /api/fcm/save-token
     * 클라이언트 App.vue에서 이 엔드포인트를 호출합니다.
     */
    @PostMapping("/save-token")
    public ResponseEntity<Void> saveToken(@RequestBody TokenRequest tokenRequest) {
        System.out.println(tokenRequest.getToken());
        if (tokenRequest.getUserId() == null || tokenRequest.getToken() == null) {
            // 필수 필드가 누락된 경우 Bad Request 반환
            return ResponseEntity.badRequest().build();
        }
        // 🚨 수신된 데이터 로그 추가
        System.out.println("Received Token for User: " + tokenRequest.getUserId() +
                ", Token starts with: " + tokenRequest.getToken().substring(0, 10) + "...");
        // FcmService를 통해 DB에 토큰을 저장하거나 업데이트합니다.
        fcmService.saveToken(tokenRequest);
        return ResponseEntity.ok().build();
    }

    /**
     * 테스트용으로 특정 사용자에게 푸시 알림을 발송합니다. (개발/테스트용)
     * 엔드포인트: POST /api/fcm/send-test?userId={사용자ID}
     * 예: POST /api/fcm/send-test?userId=1
     */
    @PostMapping("/send-test")
    // 🚨 @RequestParam String UserNumber -> @RequestParam String userId로 변경
    public ResponseEntity<String> sendTestNotification(@RequestParam String userId) {
        try {
            // NotificationService를 호출하여 알림을 발송합니다.
            // 🚨 UserNumber -> userId로 변경
            String response = notificationService.sendNotificationToUser(
                    userId,
                    "그룹원1이 위험합니다.",
                    "어서 구조해주세요! " + LocalDateTime.now()
            );
            return ResponseEntity.ok("Notification sent: " + response);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error sending notification: " + e.getMessage());
        }
    }
}