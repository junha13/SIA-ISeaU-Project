package lx.iseau.feature.report;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/report")
@RestController
public class ReportController {
	
	@Autowired
	ReportService service;

	/*
	 * ===== 하나의 응급 대처법 =====
	 */
	@RequestMapping("/first-aid")
	public ResponseEntity<?> getFirstAidByCase(@RequestBody String firstAidCase) {
		Map<String, Object> result = service.getFirstAidByCase(firstAidCase);
		return ResponseEntity
				.ok()
				.header("api", "Report/first-aid")
				.body(Map.of("data", result));
	}
	
	/*
	 * ===== 상황목록 조회 =====
	 */ 
	@GetMapping("/first-aid/cases")
	public ResponseEntity<?> listFirstAidCases() {
		Map<String, Object> result = service.listFirstAidCases();
		return ResponseEntity
				.ok()
				.header("api", "Report/first-aid/cases")
				.body(Map.of("data", result));
	}
	
}
