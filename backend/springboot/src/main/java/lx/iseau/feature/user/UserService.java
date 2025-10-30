package lx.iseau.feature.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
	
	@Autowired
	UserDAO dao;

	/**
	 * 사용자 설정 업데이트
	 * @param settingDTO
	 * @return int (성공하면 1)
	 */
	public int settings(SettingsDTO dto) {
		return dao.settings(dto);
	}
	
	
	@Transactional(readOnly = true)
    public Map<String, Object> searchUser(Map<String, Object> searchParams) {
        
        String id = (String) searchParams.get("id");
        String name = (String) searchParams.get("name");
        String mobile = (String) searchParams.get("mobile");
        
        RequestUserDTO user = null;
        
        // --- 1. DAO 호출 분기 (유효성 검사 및 메시지 설정 로직 제거) ---
        if (id != null && !id.trim().isEmpty()) {
            user = dao.findUserById(id);     
            
        } else if (name != null && !name.trim().isEmpty() && mobile != null && !mobile.trim().isEmpty()) {    
            user = dao.findUserByNameAndPhone(name, mobile);            
        } 
        // --- 2. 결과 처리 ---
        Map<String, Object> map = new HashMap<>();
        // 💡 주의: user가 null인 경우는 '검색 조건이 없었을 때'와 '검색 결과가 없었을 때' 모두 포함
        if (user != null) {
            // 사용자 검색 성공 시
            map.put("found", true);
            map.put("success", true);
            map.put("user", user); 
            
        } else {
            // 사용자를 찾지 못했을 때 (또는 검색 조건이 없었을 때)
            map.put("found", false);
            map.put("success", true); 
            // 메시지를 분리하지 않고 하나의 메시지로 처리
            map.put("message", "일치하는 사용자를 찾을 수 없습니다.");
        }
        
        return map;
    }
	
}
