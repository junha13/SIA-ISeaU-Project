package lx.iseau.feature.user;

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
}
