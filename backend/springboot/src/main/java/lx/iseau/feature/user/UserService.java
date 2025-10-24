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
	
	/*
	 * 설명
	 */
	@Transactional
	public Map<String, Object> a(UserVO user) {
		int num = 3;
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("result", num);
		
		return map;
	}
	
	/*
	 * 로그인
	 * id와 비밀번호가 모두 DB에 있으면 userId를 반환
	 */
	@Transactional
	public int login(LoginDTO dto) {
		return dao.login(dto.getId(), dto.getpW());
	}
	
	/*
	 * 아이디 중복확인
	 * true: 사용가능한 아이디
	 */
	public boolean isIdAvailable(String id) {
		return dao.checkDuplicateId(id) == 0;
	}
	
	/*
	 * 회원가입
	 * true: 회원가입 성공
	 */
	public boolean register(RegisterDTO dto) {
		if (!isIdAvailable(dto.getId())) return false;
		return dao.register(dto) == 1;
	}
}
