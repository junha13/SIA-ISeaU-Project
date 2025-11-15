package lx.iseau.feature.user;

import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserController {
	
    private final UserService service;

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
	
	@PostMapping("/search")
	public ResponseEntity<Map<String, Object>> searchUsers(@RequestBody RequestUserDTO request) {	   
	    Map<String, Object> map = new HashMap<>(); 
	    
	    String id = request.getId();
	    String name = request.getUserName();
	    String mobile = request.getMobile();	
	    
	   map.put("id", id != null ? id : "");
	   map.put("name", name != null ? name : "");
	   map.put("mobile", mobile != null ? mobile : "");
	   
	    Map<String, Object> result = service.searchUser(map);
	    return ResponseEntity
	            .ok()
	            .header("api", "User/search")	          
	            .body(Map.of("data", result)); 
	}
	
    @PostMapping("/select-beach")
    public ResponseEntity<?> selectBeach(@RequestBody Map<String, Object> body) {
        Object val = body.get("beachNumber");
        if (val == null) return ResponseEntity.badRequest().body(Map.of("success", false, "message", "beachNumber is required"));
        int userNumber = 1; // TODO: 로그인 붙이면 교체
        int beachNumber = ((Number) val).intValue();
        if (beachNumber <= 0) { // 0이면 해제로 처리
            return ResponseEntity.ok(Map.of("success", service.unselectBeach(userNumber)));
        }
        return ResponseEntity.ok(Map.of("success", service.selectBeach(userNumber, beachNumber)));
    }

    @PostMapping("/unselect-beach")
    public ResponseEntity<?> unselectBeach() {
        int userNumber = 1;
        return ResponseEntity.ok(Map.of("success", service.unselectBeach(userNumber)));
    }
}