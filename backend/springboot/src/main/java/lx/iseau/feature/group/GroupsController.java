package lx.iseau.feature.group;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/group")
@RestController
public class GroupsController {
	
	@Autowired
	GroupsService service;

	/*
	 * 설명
	 */
	@RequestMapping("/a")
	public ResponseEntity<?> a(@RequestBody int num) {
		Map<String, Object> result = service.a(num);
		return ResponseEntity
				.ok()
				.header("api", "Groups/a")
				.body(Map.of("data", result));
	}
}
