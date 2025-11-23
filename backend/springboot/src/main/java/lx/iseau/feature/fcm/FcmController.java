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

    private final FcmService fcmService;
    private final NotificationService notificationService;

    @PostMapping("/save-token")
    public ResponseEntity<Void> saveToken(@RequestBody TokenRequest tokenRequest) {
        if (tokenRequest.getUserId() == null || tokenRequest.getToken() == null) {
            return ResponseEntity.badRequest().build();
        }
        System.out.println("Received Token for User: " + tokenRequest.getUserId());
        fcmService.saveToken(tokenRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-test")
    public ResponseEntity<String> sendTestNotification(@RequestParam String userId) {
        try {
            String response = notificationService.sendNotificationToUser(
                    userId,
                    "테스트 알림",
                    "테스트 메시지입니다. " + LocalDateTime.now()
            );
            return ResponseEntity.ok("Notification sent: " + response);
        } catch (FirebaseMessagingException e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * 🔔 [수정 완료] sendInviteNotification 파라미터 개수 맞춤 (4개)
     */
    @PostMapping("/send-invite-test")
    public ResponseEntity<String> sendInviteTestNotification(
            @RequestParam String userId,
            @RequestParam String inviterName,
            @RequestParam Integer groupId,
            @RequestParam Integer invitationId) {
        try {
            // 🚨 [수정] 4번째 파라미터(invitationId)에 0 전달
            String response = notificationService.sendInviteNotification(
                    userId,
                    inviterName,
                    groupId,
                    invitationId// 임시 ID
            );
            return ResponseEntity.ok("Invite test sent: " + response);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error sending invite notification: " + e.getMessage());
        }
    }
}