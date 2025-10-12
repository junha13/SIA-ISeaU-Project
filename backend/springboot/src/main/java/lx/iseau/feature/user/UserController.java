package lx.iseau.feature.user;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/user")
@RestController
public class UserController {
	
	@Autowired
	UserService service;

	/*
	 * 설명
	 */
	@RequestMapping("/a")
	public ResponseEntity<?> a(@RequestBody UserVO user) {
		Map<String, Object> result = service.a(user);
		return ResponseEntity
				.ok()
				.header("api", "safety/a")
				.body(Map.of("data", result));
	}
}
