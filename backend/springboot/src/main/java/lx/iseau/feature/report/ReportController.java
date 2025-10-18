package lx.iseau.feature.report;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/report")
@RestController
public class ReportController {
	
	@Autowired
	ReportService service;

	/*
	 * 설명
	 */
	@RequestMapping("/a")
	public ResponseEntity<?> a(@RequestBody int num) {
		Map<String, Object> result = service.a(num);
		return ResponseEntity
				.ok()
				.header("api", "Report/a")
				.body(Map.of("data", result));
	}
}
