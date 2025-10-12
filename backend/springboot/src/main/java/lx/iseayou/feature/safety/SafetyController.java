package lx.iseayou.feature.safety;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/safety")
@RestController
public class SafetyController {
	
	@Autowired
	SafetyService service;

	/*
	 * 설명
	 */
	@RequestMapping("/a")
	public ResponseEntity<?> a(@RequestBody int num) {
		Map<String, Object> result = service.a(num);
		return ResponseEntity
				.ok()
				.header("api", "safety/a")
				.body(Map.of("data", result));
	}
}
