package lx.iseau.feature.watch;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lx.iseau.feature.watch.dto.CreateWatchEventRequest;

/**
 * 최소 기능의 워치 이벤트 입력 + 자동 태스크 생성 컨트롤러
 */
@RestController
@RequestMapping("/api/watch")
public class WatchController {

    @Autowired
    private WatchService watchService;

    /**
     * [POST] /api/watch/events
     * - 워치 이벤트 저장 + 관제센터 담당자에게 태스크 자동 생성(가장 단순한 규칙)
     */
    @RequestMapping("/events")
    public Map<String, Object> ingest(@RequestBody CreateWatchEventRequest req) {
        return watchService.ingestAndDispatch(req.getHeartRate(), req.getSpo2(), req.getOccurredAt());
    }
}
