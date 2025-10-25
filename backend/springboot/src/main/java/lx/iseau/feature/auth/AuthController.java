package lx.iseau.feature.auth;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
	@PostMapping("/register")
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
	@PostMapping("/check-id")
	public ResponseEntity<?> checkId(@RequestBody String id) {
		// 디버깅용 로그
		System.out.println("받은 아이디 원본: [" + id + "]");
		System.out.println("아이디 길이: " + id.length());
		
		// JSON.stringify()로 전송된 경우 따옴표가 포함되므로 제거
		// 예: "testid" → testid
		String cleanId = id.replace("\"", "").trim();
		System.out.println("정제된 아이디: [" + cleanId + "]");
		
		// DB에서 중복 확인
		int result = service.checkId(cleanId);
		System.out.println("중복확인 결과: " + result);
		
		return ResponseEntity
				.ok()
				.header("api", "Auth/check-id")
				.body(Map.of("data", result));
	}

	/**
	 * 로그인
	 * @param LoginDTO
	 * @return ResponseEntity<?>(data가 Map(userNumber, id, userName)를 포함)
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
		try {
			Map<String, Object> result = service.login(dto);
			return ResponseEntity
					.ok()
					.header("api", "Auth/login")
					.body(Map.of("data", result));
		} catch (RuntimeException e) {
			return ResponseEntity
					.status(401)
					.body(Map.of("message", e.getMessage()));
		}
	}

	/**
	 * 회원탈퇴
	 * @param userNumber
	 * @return ResponseEntity<?>(data가 1이면 성공, 0이면 실패)
	 */
	@PostMapping("/withdraw")
	public ResponseEntity<?> withdraw(@RequestBody int userNumber) {
		int result = service.withdraw(userNumber);
		return ResponseEntity
				.ok()
				.header("api", "Auth/withdraw")
				.body(Map.of("data", result));
	}

	/**
	 * 아이디 찾기
	 * @param findIdDTO
	 * @return ResponseEntity<?>(data가 아이디)
	 */
	@PostMapping("/find-id")
	public ResponseEntity<?> findId(@RequestBody FindIdDTO dto) {
		String result = service.findId(dto);
		return ResponseEntity
				.ok()
				.header("api", "Auth/find-id")
				.body(Map.of("data", result));
	}

	/**
	 * 비밀번호 찾기(아이디+전화번호로 회원고유번호 조회)
	 * @param findUserNumberDTO
	 * @return ResponseEntity<?>(data가 회원고유번호)
	 */
	@PostMapping("/find-userNumber")
	public ResponseEntity<?> findUserNumber(@RequestBody FindUserNumberDTO dto) {
		int result = service.findUserNumber(dto);
		return ResponseEntity
				.ok()
				.header("api", "Auth/find-userNumber")
				.body(Map.of("data", result));
	}

	/**
	 * 비밀번호 재설정
	 * @param resetPasswordDTO
	 * @return ResponseEntity<?>(data가 1이면 성공, 0이면 실패)
	 */
	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO dto) {
		int result = service.resetPassword(dto);
		return ResponseEntity
				.ok()
				.header("api", "Auth/reset-password")
				.body(Map.of("data", result));
	}
}
