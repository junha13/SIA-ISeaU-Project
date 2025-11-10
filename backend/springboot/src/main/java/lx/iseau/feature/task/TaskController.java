package lx.iseau.feature.task;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
/**
 * 관제/담당자용 최소 태스크 API
 */
@RestController
@RequestMapping("/api")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * [GET] /api/control-towers/{id}/tasks/pending
     * - 해당 관제센터의 '대기중' 태스크(accepted=0 & processed=0)
     */
    @RequestMapping("/control-towers/{id}/tasks/pending")
    public List<TaskDTO> listPending(@PathVariable("id") Integer controlTowerNumber) {
        return taskService.getPendingByControlTower(controlTowerNumber);
    }

    /**
     * [GET] /api/managers/me/tasks
     * - 간단화를 위해 헤더 X-Manager-Id로 본인 번호를 받습니다.
     * - 진행중(accepted=1 & processed=0)만 조회
     */
    @RequestMapping("/managers/me/tasks")
    public List<TaskDTO> myOpenTasks(@RequestParam("managerId") Integer managerNumber) {
        return taskService.getOpenByManager(managerNumber);
    }

    /**
     * [POST] /api/tasks/{taskNumber}/accept?managerId=1
     * - 본인 매니저 번호로 태스크 수락
     */
    @RequestMapping("/tasks/{taskNumber}/accept")
    public Map<String, Object> accept(@PathVariable Integer taskNumber, @RequestParam("managerId") Integer managerId) {
        return taskService.accept(taskNumber, managerId);
    }

    /**
     * [POST] /api/tasks/{taskNumber}/complete?managerId=1
     * - 본인 매니저 번호로 태스크 완료
     */
    @RequestMapping("/tasks/{taskNumber}/complete")
    public Map<String, Object> complete(@PathVariable Integer taskNumber, @RequestParam("managerId") Integer managerId) {
        return taskService.complete(taskNumber, managerId);
    }
}
