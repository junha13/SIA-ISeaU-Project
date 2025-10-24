package lx.iseau.feature.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
