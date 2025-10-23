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
     * 로그아웃
     * @param userNumber
     * @return int(성공하면 1)
     */
    int logout(@Param("userNumber") int userNumber);

    /**
     * 아이디 찾기
     * @param findIdDTO
     * @return String (아이디)
     */
    String findId(@Param("findIdDTO") FindIdDTO dto);

}