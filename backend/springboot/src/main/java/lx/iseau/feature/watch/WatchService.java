package lx.iseau.feature.watch;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WatchService {

    @Autowired
    private WatchDAO dao;

    // 임계치(예시): HR<55 또는 SpO2<90이면 위험으로 판단
    private static final int HR_DANGER = 55;
    private static final int SPO2_DANGER = 90;

    /**
     * 1) watch 단건 조회
     * 2) 임계치 평가(위험?) → 아니면 createdTask=false
     * 3) 유저→비치→관제→매니저(1:1) 찾기
     * 4) tb_task 생성(중복 방지: 동일 watch_number로는 1회 생성 권장)
     */
    public Map<String, Object> evaluateAndDispatch(Map<String, Object> body) {
        Map<String, Object> map = new HashMap<>();
        if (body == null || body.get("watchNumber") == null) {
            map.put("createdTask", false);
            map.put("reason", "missing_watchNumber");
            return map;
        }

        Integer watchNumber = (body.get("watchNumber") instanceof Number)
                ? ((Number) body.get("watchNumber")).intValue()
                : null;

        // 1) watch 조회
        Map<String, Object> w = dao.selectWatchByNumber(watchNumber);
        if (w == null) {
            map.put("createdTask", false);
            map.put("reason", "watch_not_found");
            return map;
        }

        Integer hr         = (Integer) w.get("heartRate");
        Integer spo2       = (Integer) w.get("spo2");
        Integer userNumber = (Integer) w.get("userNumber");

        // 2) 임계치 평가
        boolean danger = (hr != null && hr < HR_DANGER) || (spo2 != null && spo2 < SPO2_DANGER);
        if (!danger) {
            map.put("createdTask", false);
            map.put("reason", "normal");
            return map;
        }

        // 3) 매니저(1:1) 찾기
        Integer managerNumber = dao.findManagerByUser(userNumber);
        if (managerNumber == null) {
            map.put("createdTask", false);
            map.put("reason", "manager_not_found");
            return map;
        }

        // 4) 업무 생성
        //  - 중복 방지 권장: DB에 UNIQUE(watch_number) 추가 권장
        //    ALTER TABLE tb_task ADD CONSTRAINT uq_task_watch UNIQUE (watch_number);
        //    이미 존재 시 insert가 0건이거나 에러 → 아래 insertTask는 0/1로만 판단
        int rows = dao.insertTaskIfAbsent(managerNumber, watchNumber);

        map.put("createdTask", rows > 0);
        map.put("managerNumber", managerNumber);
        map.put("watchNumber", watchNumber);
        return map;
    }
}
