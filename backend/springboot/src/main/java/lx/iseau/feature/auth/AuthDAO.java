package lx.iseau.feature.auth;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List; // java.util.List 임포트 필요

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

}