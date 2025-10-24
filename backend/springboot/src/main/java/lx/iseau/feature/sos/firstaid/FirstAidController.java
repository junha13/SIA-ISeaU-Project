package lx.iseau.feature.sos.firstaid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/sos")
@RestController
public class FirstAidController {
	
	@Autowired
	FirstAidService service;

	/*
	 * ===== 하나의 응급 대처법 =====
	 */
	@GetMapping("/first-aid")
	public ResponseEntity<?> getFirstAidByCaseNum(@RequestParam int firstAidCaseNum) {
		return ResponseEntity.ok(service.getFirstAidByCaseNum(firstAidCaseNum));
	}
	
	/*
	 * ===== 상황목록 조회 =====
	 */ 
	@GetMapping("/first-aid/cases")
	public ResponseEntity<?> listFirstAidCases() {
		return ResponseEntity.ok(service.listFirstAidCases());
	}
	
}
