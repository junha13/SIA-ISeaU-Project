package lx.iseau.feature.user;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
				.header("api", "User/a")
				.body(Map.of("data", result));
	}
	
	/**
     * 아이디 중복확인 API
     * 사용 가능 시 'available', 중복 시 'duplicate' 문자열 응답
     */
    @GetMapping("/check-id")
    public ResponseEntity<String> checkDuplicate(@RequestParam("id") String id) {
        // 서비스 계층을 통해 아이디 사용 가능 여부 확인 (쿼리파라미터로 userId를 받음)
        boolean isAvailable = service.isIdAvailable(id);
        
        if (isAvailable) {
            return ResponseEntity.ok("available");
        } else {
            return ResponseEntity.ok("duplicate");
        }
    }
    
    /**
     * 회원가입 처리 API
     * 회원가입 성공 시 'success', 실패 시 'fail' 문자열 응답
     */
    @PostMapping("/register")
    public ResponseEntity<String> signup(@RequestBody RegisterDTO dto) {
        // 서비스 계층을 통해 회원가입 로직 수행
        boolean isSuccess = service.register(dto);
        
        if (isSuccess) {
            return ResponseEntity.ok("success");
        } else {
            // 중복된 아이디 또는 기타 이유로 실패 시
            return ResponseEntity.ok("fail");
        }
    }
    
    /*
     * 로그인 처리 API
     * 로그인 성공 시 userId, 실패 시 401 Unauthorized 응답
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        
		// 서비스 계층의 로그인 메서드를 호출하여 사용자 인증을 시도
		int id = service.login(dto);

		// 인증 성공 여부 확인
        if (id > 0) {
            // 성공 시 { userId: <id> } 형태로 응답
            java.util.Map<String, Integer> resp = new java.util.HashMap<>();
            resp.put("id", id);
            return ResponseEntity.ok().body(resp);
        } else {
            // 로그인 실패 시 401 Unauthorized
            return ResponseEntity.status(401).body("로그인 실패: 아이디 또는 비밀번호가 올바르지 않습니다.");
        }

	}
}
