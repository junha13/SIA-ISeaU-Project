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
     * 특정 사용자에게 푸시 알림을 발송합니다.
     */
    public String sendNotificationToUser(String targetUserNumber, String title, String body) throws FirebaseMessagingException {
        String registrationToken = fcmService.getRegistrationToken(targetUserNumber);

        // 토큰이 없으면 여기서 끝
        if (registrationToken == null || registrationToken.isEmpty()) {
            System.err.println("❌ 토큰 없음: 유저 " + targetUserNumber);
            return "Token not found";
        }

        // 🚨 FCM 메시지 구성 (Notification payload 사용)
        Message message = Message.builder()
                .setToken(registrationToken) // 저장된 토큰 사용
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .setImage("https://yourdomain.com/pwa-512x512.png") // 이미지 URL (선택 사항)  무슨 주소로 해야하지? ㅋㅋ..
                        .build())
                .putData("timestamp", LocalDateTime.now().toString()) // 추가 데이터
                .build();

        // 🚨 메시지 전송
        String response = firebaseMessaging.send(message);

        System.out.println("Successfully sent message to user " + targetUserNumber + ": " + response);
        return response;
    }
}