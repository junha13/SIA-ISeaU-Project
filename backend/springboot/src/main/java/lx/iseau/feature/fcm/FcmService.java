package lx.iseau.feature.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmService {

    private final FcmDAO fcmDao;

    @Transactional
    public void saveToken(TokenRequest tokenRequest) {
        try {
            fcmDao.upsertToken(tokenRequest);
            System.out.println("FCM Token saved/updated for user: " + tokenRequest.getUserId());
        } catch (DataAccessException e) {
            // 🚨 DB 저장 실패 시 예외를 강제로 출력하여 오류를 확인합니다.
            System.err.println("🚨🚨🚨 DB 저장 실패 (DataAccessException): " + e.getMessage());
            e.printStackTrace(); // 스택 트레이스를 콘솔에 출력

            // 오류를 던져서 Spring Boot가 500 응답을 반환하고 로그를 남기도록 유도
            throw new RuntimeException("DB Save Failed due to DataAccessException", e);
        }
    }

    public String getRegistrationToken(String userId) {
        return fcmDao.getTokenByUserId(userId);
    }

    /**
     * 특정 사용자에게 FCM 알림을 발송합니다.
     * @param targetUserId 알림을 받을 사용자의 ID (FCM 토큰을 조회하기 위해 사용)
     * @param alertMessage 알림 내용
     */
    public void sendAlertNotification(String targetUserId, String alertMessage, long timestamp) {
        // 1. 알림을 받을 대상자의 FCM 토큰 조회
        String fcmToken = getRegistrationToken(targetUserId);

        if (fcmToken == null || fcmToken.isEmpty()) {
            System.err.println("❌ FCM Token not found for user: " + targetUserId);
            return;
        }

        try {
            // 2. FCM 메시지 구성
            Message message = Message.builder()
                    // 데이터 페이로드 (앱에서 추가 처리용)
                    .putData("type", "DANGER_ALERT")
                    .putData("timestamp", String.valueOf(timestamp))
                    .putData("sender_id", "WearOS_Client") // 경고 보낸 측 정보

                    // 알림 페이로드 (사용자에게 직접 표시되는 내용)
                    .setNotification(Notification.builder()
                            .setTitle("🚨 긴급 안전 경고 (Wear OS)")
                            .setBody(alertMessage)
                            .build())
                    .setToken(fcmToken) // 발송 대상 토큰
                    .build();

            // 3. FCM 발송
            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ FCM Alert sent successfully. Message ID: " + response);

        } catch (FirebaseMessagingException e) {
            System.err.println("❌ Error sending FCM: " + e.getMessage());
            // TODO: 실패 로그를 DB에 기록하는 로직 추가 가능
        }
    }
}