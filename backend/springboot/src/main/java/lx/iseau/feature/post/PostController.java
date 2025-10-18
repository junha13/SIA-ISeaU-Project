package lx.iseau.feature.post;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/post")
@RestController
public class PostController {
	
	@Autowired
	PostService service;

	/*
	 * 설명
	 */
	@RequestMapping("/a")
	public ResponseEntity<?> a(@RequestBody int num) {
		Map<String, Object> result = service.a(num);
		return ResponseEntity
				.ok()
				.header("api", "Post/a")
				.body(Map.of("data", result));
	}
}
