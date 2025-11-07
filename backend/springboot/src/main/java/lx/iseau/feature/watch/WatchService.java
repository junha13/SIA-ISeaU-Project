package lx.iseau.feature.watch;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import lx.iseau.feature.task.TaskDAO;
import lx.iseau.feature.task.TaskDTO;
import lx.iseau.feature.watch.dto.WatchEventDTO;

/**
 * 핵심 로직 (최소 기능)
 * 1) 워치 이벤트 저장(tb_watch)
 * 2) 사용자 -> 해수욕장 -> 관제센터 조회
 * 3) 가장 간단한 매칭 규칙으로 담당자 1명 선택 후 태스크 생성(tb_task)
 */
@Service
public class WatchService {

    @Autowired
    private HttpSession httpSession;

    @Autowired
    private WatchDAO watchDAO;

    @Autowired
    private lx.iseau.feature.manager.ManagerDAO managerDAO;

    @Autowired
    private TaskDAO taskDAO;

    // 세션에서 로그인 사용자 번호 꺼내기 (없으면 null)
    private Integer getLoggedInUserNumber() {
        Object val = httpSession.getAttribute("userNumber");
        return (val instanceof Integer) ? (Integer) val : null;
    }

    @Transactional
    public Map<String, Object> ingestAndDispatch(Integer heartRate, Integer spo2, String occurredAtIso) {
        Map<String, Object> result = new HashMap<>();

        // 0) 로그인 사용자 확인
        Integer userNumber = getLoggedInUserNumber();
        if (userNumber == null) {
            result.put("ok", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }

        // 1) 문자열 -> Timestamp 안전 변환 (실패 시 현재시각)
        Timestamp occurredAt;
        try {
            occurredAt = Timestamp.from(Instant.parse(occurredAtIso));
        } catch (DateTimeParseException e) {
            occurredAt = new Timestamp(System.currentTimeMillis());
        }

        // 2) 워치 이벤트 저장
        WatchEventDTO watch = new WatchEventDTO();
        watch.setHeartRate(heartRate);
        watch.setSpo2(spo2);
        watch.setOccurredAt(occurredAt);
        watch.setUserNumber(userNumber);
        watchDAO.insertWatchEvent(watch); // watch.watchNumber 채워짐(Generated Key)

        // 3) 사용자 -> 관제센터 찾기 (tb_user -> tb_beach -> tb_control_tower)
        Integer controlTowerNumber = watchDAO.findControlTowerNumberByUser(userNumber);
        if (controlTowerNumber == null) {
            result.put("ok", true);
            result.put("message", "관제센터가 지정되지 않아 태스크를 생성하지 않았습니다.");
            result.put("watchNumber", watch.getWatchNumber());
            return result;
        }

        // 4) 관제센터의 담당자 1명 할당 (가장 번호가 작은 담당자)
        Integer managerNumber = managerDAO.findAnyManagerByControlTower(controlTowerNumber);
        if (managerNumber == null) {
            result.put("ok", true);
            result.put("message", "해당 관제센터에 담당자가 없어 태스크를 생성하지 않았습니다.");
            result.put("watchNumber", watch.getWatchNumber());
            return result;
        }

        // 5) 태스크 생성 (accepted=0, processed=0 기본값)
        TaskDTO task = new TaskDTO();
        task.setManagerNumber(managerNumber);
        task.setWatchNumber(watch.getWatchNumber());
        taskDAO.insertTask(task);

        result.put("ok", true);
        result.put("watchNumber", watch.getWatchNumber());
        result.put("taskNumber", task.getTaskNumber());
        return result;
    }
}
