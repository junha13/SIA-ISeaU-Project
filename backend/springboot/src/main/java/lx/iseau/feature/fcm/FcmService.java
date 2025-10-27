package lx.iseau.feature.fcm;

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
}