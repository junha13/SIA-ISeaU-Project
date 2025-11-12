// lx.iseau.feature.watch.WatchService.java
package lx.iseau.feature.watch;

import lx.iseau.feature.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WatchService {

    private final WatchDAO watchDao;
    private final FcmService fcmService;

    @Transactional
    public void processHeartRateData(HeartRateRequest request) {

        // 1. DTO 변환 및 시간 파싱
        WatchDataDTO watchData = new WatchDataDTO();
        watchData.setHeartRate(request.getHeartRate());;
        watchData.setUserNumber(request.getUserNumber());

        try {
            // ISO 8601 문자열을 Instant로 변환 (Kotlin Ktor에서 보낸 형식)
            watchData.setOccurredAt(Instant.parse(request.getOccurredAt()));
        } catch (DateTimeParseException e) {
            System.err.println("시간 파싱 오류: " + request.getOccurredAt() + ". 현재 시간으로 대체.");
            watchData.setOccurredAt(Instant.now());
        }

        // 2. DB 저장 (tb_watch에 INSERT)
        watchDao.insertWatchEvent(watchData);
        System.out.println("✅ Watch Event DB 저장 완료. userNumber: " + watchData.getUserNumber() + ", HR: " + watchData.getHeartRate());

        // 3. 긴급 알림 처리 (FCM 발송)
        if (request.getIsEmergency() != null && request.getIsEmergency()) {
            handleEmergencyAlert(watchData);
        }
    }

    /**
     * 💡 [추가된 코드]: DB에 저장된 심박수 데이터를 조회합니다.
     * @param watchNumber 조회할 watchNumber (tb_watch의 PK)
     * @return 조회된 데이터 Map
     */
    public Map<String, Object> getWatchData(int watchNumber) {
        return watchDao.selectWatchByNumber(watchNumber);
    }

    private void handleEmergencyAlert(WatchDataDTO watchData) {
        // 3-1. userNumber를 통해 담당 매니저 찾기
        Integer managerNumber = watchDao.findManagerByUser(watchData.getUserNumber());

        if (managerNumber == null || managerNumber == 0) {
            System.err.println("❌ 긴급 알림: User " + watchData.getUserNumber() + "에 연결된 매니저를 찾을 수 없습니다.");
            return;
        }

        // 3-2. tb_task 생성 (최초 경고 시에만)
        Map<String, Integer> taskParams = Map.of(
                "managerNumber", managerNumber,
                "watchNumber", Math.toIntExact(watchData.getWatchNumber()) // INSERT 후 자동 생성된 번호 사용
        );
        watchDao.insertTaskIfAbsent(taskParams);

        // 3-3. 담당 매니저에게 FCM 알림 발송 (관제 페이지 푸시)
        // [중요] 매니저의 FCM 토큰 조회용 user_id를 'MANAGER_X'와 같이 규격화해야 합니다.
        String targetUserId = "MANAGER_" + managerNumber;

        String alertMessage = String.format(
                "사용자 %d 심박수 이상! %d BPM. 즉시 확인 필요.",
                watchData.getUserNumber(),
                watchData.getHeartRate()
        );

        // FcmService의 sendAlertNotification 함수를 사용하여 발송
        fcmService.sendAlertNotification(
                targetUserId,
                alertMessage,
                watchData.getOccurredAt().toEpochMilli()
        );
        System.out.println("✅ FCM Alert Sent to Manager " + managerNumber);
    }
}