package lx.iseau.feature.controltower;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lx.iseau.feature.task.TaskItemDTO;
import lx.iseau.feature.control.ManagerSummaryDTO;

/**
 * 관제 서비스
 * - DB 접근은 DAO에 위임
 * - 컨트롤러에선 Map과 헤더만 세팅, 여기서 결과 조립
 */
@Service
@RequiredArgsConstructor
public class ControlTowerService {

    private final ControlTowerDAO dao;

    /** 관제타워 이름으로 매니저 + 처리리스트 묶어서 반환 */
    public Map<String, Object> getManagerSummaryByTowerName(String controlTowerName) {
        // 1) 타워명으로 매니저 한 명 조회(요구사항: 한 타워당 한 명)
        ManagerSummaryDTO manager = dao.selectManagerByControlTowerName(controlTowerName);

        // 2) 없으면 빈 리스트로 응답
        if (manager == null) {
            return Map.of(
                "manager", null,
                "tasks", List.of()
            );
        }

        // 3) 매니저의 처리 목록(Task + Watch + User.location) 조회
        List<TaskListItemDTO> tasks = dao.selectTasksByManagerNumber(manager.getManagerNumber());

        // 4) 하나의 묶음(Map)으로 반환
        return Map.of(
            "manager", manager,
            "tasks", tasks
        );
    }

    /** 매니저 이름으로 매니저 + 처리리스트 묶어서 반환 */
    public Map<String, Object> getManagerSummaryByManagerName(String managerName) {
        ManagerSummaryDTO manager = dao.selectManagerByManagerName(managerName);
        if (manager == null) {
            return Map.of(
                "manager", null,
                "tasks", List.of()
            );
        }
        List<TaskListItemDTO> tasks = dao.selectTasksByManagerNumber(manager.getManagerNumber());
        return Map.of(
            "manager", manager,
            "tasks", tasks
        );
    }
}
