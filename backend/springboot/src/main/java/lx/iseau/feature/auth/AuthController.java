package lx.iseau.feature.auth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/auth")
@RestController
public class AuthController {
	
	@Autowired
	AuthService service;

	/**
	 * 회원가입
	 * @param registerDTO
	 * @return ResponseEntity<?>(data가 1이면 성공, 0이면 실패)
	 */
	@RequestMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterDTO dto) {
		int result = service.register(dto);
		return ResponseEntity
				.ok()
				.header("api", "Auth/register")
				.body(Map.of("data", result));
	}

	/**
	 * 아이디 중복확인
	 * @param id
	 * @return ResponseEntity<?>(data가 0이면 중복안됨, 1이상이면 중복됨)
	 */
	@RequestMapping("/check-id")
	public ResponseEntity<?> checkId(@RequestBody String id) {
		int result = service.checkId(id);
		return ResponseEntity
				.ok()
				.header("api", "Auth/check-id")
				.body(Map.of("data", result));
	}

	/**
	 * 로그인
	 * @param LoginDTO
	 * @return ResponseEntity<?>(data가 Map(user_number, id)를 포함)
	 */
	@RequestMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
		Map<String, Object> result = service.login(dto);
		return ResponseEntity
				.ok()
				.header("api", "Auth/login")
				.body(Map.of("data", result));
	}
}
