package lx.iseau.feature.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
	
	@Autowired
	AuthDAO dao;

	/**
	 * 회원가입
	 * @param registerDTO
	 * @return int(성공하면 1)
	 */
	public int register(RegisterDTO dto) {
		return dao.register(dto);
	}

	/**
	 * 아이디 중복확인
	 * @param id
	 * @return 1이상: 중복됨 | 0: 중복안됨
	 */
	public int checkId(String id) {
		return dao.checkId(id);
	}

	/**
	 * 로그인
	 * @param LoginDTO
	 * @return Map (user_number, id를 포함)
	 */
	public Map<String, Object> login(LoginDTO dto) {
		return dao.login(dto);
	}

}
