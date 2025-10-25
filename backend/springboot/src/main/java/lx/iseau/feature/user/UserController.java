package lx.iseau.feature.user;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/user")
@RestController
public class UserController {
	
	@Autowired
	UserService service;

	/**
	 * 사용자 설정 업데이트
	 * @param settingDTO
	 * @return ResponseEntity<?>(data가 1이면 성공, 0이면 실패)
	 */
	@PutMapping("/settings")
	public ResponseEntity<?> settings(@RequestBody SettingsDTO settingDTO) {
		int result = service.settings(settingDTO);
		return ResponseEntity
				.ok()
				.header("api", "User/settings")
				.body(Map.of("data", result));
	}
}
