package lx.iseau.feature.auth;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Map;

@Mapper
public interface AuthDAO {

    /**
     * 회원가입
     * @param registerDTO
     * @return int(성공하면 1)
     */
    int register(@Param("registerDTO") RegisterDTO dto);

    /**
     * 아이디 중복확인
     * @param id
     * @return 1이상: 중복됨 | 0: 중복안됨
     */
    int checkId(@Param("id") String id);

    /**
     * 로그인
     * @param LoginDTO
     * @return Map (user_number, id를 포함)
     */
    Map<String, Object> login(@Param("loginDTO") LoginDTO dto);

    /**
     * 회원탈퇴
     * @param userNumber
     * @return int(성공하면 1)
     */
    int withdraw(@Param("userNumber") int userNumber);

    /**
     * 아이디 찾기
     * @param findIdDTO
     * @return String (아이디)
     */
    String findId(@Param("findIdDTO") FindIdDTO dto);

    /**
     * 비밀번호 찾기(아이디+전화번호로 회원고유번호 조회)
     * @param findUserNumberDTO
     * @return int (회원고유번호)
     */
    int findUserNumber(@Param("findUserNumberDTO") FindUserNumberDTO dto);

    /**
     * 비밀번호 재설정
     * @param resetPasswordDTO
     * @return int (성공하면 1)
     */
    int resetPassword(@Param("resetPasswordDTO") ResetPasswordDTO dto);

}