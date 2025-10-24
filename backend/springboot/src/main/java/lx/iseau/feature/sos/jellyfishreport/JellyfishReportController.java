package lx.iseau.feature.sos.jellyfishreport;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/sos")
@RestController
public class JellyfishReportController {
	
	@Autowired
	JellyfishReportService service;
	
	@PostMapping("/jellyfish-report")
    public ResponseEntity<?> create(@RequestBody JellyfishReportDTO body) {
        int id = service.createReport(body);
        return ResponseEntity
            .created(URI.create("/jellyfish-report/" + id))
            .body(Map.of("reportNumber", id));
    }
	
	// 매핑 확인용
    @GetMapping("/jellyfish-report/_ping")
    public String ping() { return "ok"; }
}
