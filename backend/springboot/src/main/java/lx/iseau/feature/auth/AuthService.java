package lx.iseau.feature.auth;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
	

    private final AuthDAO dao;

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
	 * @return Map (user_number, id, user_name을 포함)
	 * @throws RuntimeException 아이디 또는 비밀번호가 일치하지 않을 때
	 */
	public Map<String, Object> login(LoginDTO dto) {
		Map<String, Object> user = dao.login(dto);
		if (user == null || user.isEmpty()) {
			throw new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다.");
		}
		return user;
	}

	/**
	 * 회원탈퇴
	 * @param userNumber
	 * @return int(성공하면 1)
	 */
	public int withdraw(int userNumber) {
		return dao.withdraw(userNumber);
	}

	/**
	 * 아이디 찾기
	 * @param findIdDTO
	 * @return String (아이디)
	 */
	public String findId(FindIdDTO dto) {
		return dao.findId(dto);
	}

	/**
	 * 비밀번호 찾기(아이디+전화번호로 회원고유번호 조회)
	 * @param findUserNumberDTO
	 * @return int (회원고유번호)
	 */
	public int findUserNumber(FindUserNumberDTO dto) {
		return dao.findUserNumber(dto);
	}

	/**
	 * 비밀번호 재설정
	 * @param resetPasswordDTO
	 * @return int (성공하면 1)
	 */
	public int resetPassword(ResetPasswordDTO dto) {
		return dao.resetPassword(dto);
	}

}
