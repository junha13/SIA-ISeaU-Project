package lx.iseau.feature.fcm;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final FirebaseMessaging firebaseMessaging; 
    private final FcmService fcmService;

    public NotificationService(FirebaseMessaging firebaseMessaging, FcmService fcmService) {
        this.firebaseMessaging = firebaseMessaging;
        this.fcmService = fcmService;
    }

    /**
     * 🔔 실시간 초대장 알림을 발송합니다. (순수 Data 메시지)
     * Service Worker의 수동 알림 처리 로직에 의존하기 위해 setNotification을 제거합니다.
     * * @param targetUserNumber 받는 사람 유저 번호 (예: "3")
     * @param inviterName 초대자 이름
     * @param groupId 그룹 ID
     * @param invitationId 초대장 고유 ID (PK) - 수락/거절 시 필요
     */
    public String sendInviteNotification(String targetUserNumber, String inviterName, Integer groupId, Integer invitationId) throws FirebaseMessagingException {
        // 1. FCM 토큰 조회
        String registrationToken = fcmService.getRegistrationToken(targetUserNumber);

        if (registrationToken == null || registrationToken.isEmpty()) {
            System.err.println("❌ Error: User token not found for user " + targetUserNumber);
            return "Error: User token not found";
        }
        
        // 2. 메시지 구성: 순수 Data Payload만 포함 (setNotification 제거)
        // Service Worker가 notificationTitle과 Body를 payload.data에서 추출하도록 데이터를 구성합니다.
        Message message = Message.builder()
                .setToken(registrationToken) 
                
                // --- 🚨 [핵심] 프론트엔드 트리거 및 SW 백그라운드 처리용 Data Payload ---
                .putData("type", "GROUP_INVITE_PENDING") // Foreground onMessage 트리거용
                .putData("group_id", String.valueOf(groupId))
                .putData("inviter_name", inviterName)
                
                // 🚨 [추가됨] 수락/거절 API 호출에 필요한 초대장 ID
                .putData("id", String.valueOf(invitationId != null ? invitationId : 0)) 
                
                // SW가 알림을 띄울 때 사용할 제목/본문
                .putData("title", "그룹 초대 도착") 
                .putData("body", inviterName + " 님께서 그룹에 초대했습니다.")
                
                .putData("timestamp", LocalDateTime.now().toString())
                .build();

        // 3. 메시지 전송
        String response = firebaseMessaging.send(message);

        System.out.println("✅ Successfully sent INVITE message (Data-Only) to user " + targetUserNumber + ": " + response);
        return response;
    }

    /**
     * 특정 사용자에게 일반 푸시 알림을 발송합니다. (Data-Only)
     */
    public String sendNotificationToUser(String targetUserNumber, String title, String body) throws FirebaseMessagingException {
        String registrationToken = fcmService.getRegistrationToken(targetUserNumber);

        // 토큰이 없으면 여기서 끝
        if (registrationToken == null || registrationToken.isEmpty()) {
            System.err.println("❌ 토큰 없음: 유저 " + targetUserNumber);
            return "Token not found";
        }

        // 🚨 FCM 메시지 구성 (Data-Only)
        Message message = Message.builder()
                .setToken(registrationToken)
                .putData("type", "GENERAL_ALERT") // 일반 알림 타입
                .putData("title", title)
                .putData("body", body)
                .putData("timestamp", LocalDateTime.now().toString())
                .build();

        // 🚨 메시지 전송
        String response = firebaseMessaging.send(message);

        System.out.println("Successfully sent message (Data-Only) to user " + targetUserNumber + ": " + response);
        return response;
    }
}