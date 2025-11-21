package lx.iseau.feature.controltower;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;
import lx.iseau.feature.fcm.NotificationService; // 알림 발송 서비스
import lx.iseau.feature.group.GroupsDAO;         // 그룹 정보 조회용
import lx.iseau.feature.group.ResponseGroupMemberLocationDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ControlTowerService {

    private final ControlTowerDAO dao;
    private final GroupsDAO groupsDAO;             // 그룹원 조회를 위해 주입
    private final NotificationService notificationService; // FCM 발송을 위해 주입

    private final int DEFAULT_MANAGER_NUMBER = 1; // 임시 매니저 번호

    // ============ 관제센터의 처리 리스트(지서) ============
    public List<TaskListDTO> getTaskListByControlTowerNumber(int controlTowerNumber) {
        return dao.getTaskListByControlTowerNumber(controlTowerNumber);
    }

    // ============ 매니저 기본정보 조회 ============
    public Map<String, Object> selectManagerInfoByManagerNumber(int managerNumber) {
    	Map<String, Object> map = new HashMap<>();
        map.put("result", dao.selectManagerInfoByManagerNumber(managerNumber));
        return map;
    }

    // ============ 매니저 기본정보 수정 ============
    public Map<String, Object> updateManagerInfoByManagerNumber(ManagerInfoDTO dto) {
    	Map<String, Object> map = new HashMap<>();
        map.put("result", dao.updateManagerInfoByManagerNumber(dto));
        return map;
    }

    // ============ 처리 리스트 ============
    public List<TaskListDTO> getTaskListByManagerNumber(int managerNumber) {
        return dao.getTaskListByManagerNumber(managerNumber);
    }

    // ============ 처리 상세 ============
    public TaskDetailDTO getTaskDetailByTaskNumber(int taskNumber) {
        return dao.getTaskDetailByTaskNumber(taskNumber);
    }

    // ============ 처리완료 플래그 ============
    public int updateTaskProcessed(Map<String, Object> body) {
        Integer taskNumber = intOrNull(body.get("taskNumber"));
        Integer processed  = intOrNull(body.get("processed"));
        if (taskNumber == null) return 0;
        if (processed == null) processed = 0; // 기본값 0(미처리)
        return dao.updateTaskProcessed(taskNumber, processed);
    }

    // ============ 워치 데이터 처리 및 Task 생성 ============
    @Transactional // 두 개 이상의 DAO 호출이 있어 트랜잭션 처리
    public void insertWatchEvent(HeartRateRequest request) {

        // 1. DB 저장 (tb_watch에 기록)
        dao.insertWatchEvent(request);
        log.info("✅ [심박 데이터 저장] User: {}, HR: {} bpm", request.getUserNumber(), request.getHeartRate());

        // 2. 🚨 알림 발송 (워치에서 데이터를 보냈다는 것은 이미 위험 임계치를 넘은 상황임)
        handleEmergencyAlert(request.getUserNumber(), request.getHeartRate());
    }

    // 긴급 상황 처리 그룹원에게 FCM 전송
    private void handleEmergencyAlert(int userNumber, int heartRate) {
        try {
            // 1. 긴급 Task 생성 (관제용)
            dao.insertTaskOnEmergency(DEFAULT_MANAGER_NUMBER, userNumber);

            // 2. 유저가 속한 그룹 조회
            Integer groupId = groupsDAO.findGroupIdByUser(userNumber);
            if (groupId == null) {
                log.warn("⚠️ [알림 실패] 그룹에 속하지 않은 유저입니다. User: {}", userNumber);
                return;
            }

            // 3. 그룹 멤버(보호자) 조회
            ResponseGroupMemberLocationDTO searchDto = new ResponseGroupMemberLocationDTO();
            searchDto.setGroupNumber(groupId);
            List<ResponseGroupMemberLocationDTO> members = groupsDAO.findGroupMemberLocations(searchDto);

            // 4. 본인을 제외한 멤버들에게 알림 전송
            for (ResponseGroupMemberLocationDTO member : members) {
                // member.getId()가 userNumber(PK)라고 가정
                if (member.getId() != null && !member.getId().equals(userNumber)) {
                    String targetUserStr = String.valueOf(member.getId());
                    String title = "🚨 긴급: 심박수 위험 감지!";
                    String body = String.format("그룹원이 위험합니다! (심박수: %d bpm) 즉시 확인해주세요.", heartRate);

                    notificationService.sendNotificationToUser(targetUserStr, title, body);
                    log.info("🚀 [FCM 전송 완료] To: {}, Msg: 심박수 위험 알림", targetUserStr);
                }
            }

        } catch (Exception e) {
            log.error("❌ [FCM 전송 에러] 심박수 알림 실패", e);
        }
    }

    // ====== 유틸 ======
    private Integer intOrNull(Object o) {
        try {
            if (o instanceof Double) return ((Double) o).intValue();
            return (o == null) ? null : Integer.parseInt(String.valueOf(o));
        } catch (Exception e) { return null; }
    }
}