package lx.iseau.feature.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class TaskService {

    @Autowired
    private TaskDAO taskDAO;

    // 관제센터 대기 목록
    @Transactional(readOnly = true)
    public List<TaskDTO> getPendingByControlTower(Integer controlTowerNumber) {
        return taskDAO.selectPendingTasksByControlTower(controlTowerNumber);
    }

    // 매니저 내 태스크
    @Transactional(readOnly = true)
    public List<TaskDTO> getOpenByManager(Integer managerNumber) {
        return taskDAO.selectOpenTasksByManager(managerNumber);
    }

    // 수락
    @Transactional
    public Map<String, Object> accept(Integer taskNumber, Integer managerNumber) {
        Map<String, Object> res = new HashMap<>();
        int updated = taskDAO.acceptTask(taskNumber, managerNumber);
        res.put("ok", updated > 0);
        return res;
    }

    // 완료
    @Transactional
    public Map<String, Object> complete(Integer taskNumber, Integer managerNumber) {
        Map<String, Object> res = new HashMap<>();
        int updated = taskDAO.completeTask(taskNumber, managerNumber);
        res.put("ok", updated > 0);
        return res;
    }
}
