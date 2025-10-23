package lx.iseau.feature.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface UserDAO {
	/*
	 * 로그인
	 * id와 비밀번호가 모두 DB에 있다면 결과의 userId를 뱉는다.
	 */
	int login(@Param("id") String id, @Param("pW") String pW);
	
	/*
	 * 회원가입
	 */
    int register(RegisterDTO user);

    
    /*
     * 중복확인
     */
    int checkDuplicateId(@Param("id") String id);
    
}