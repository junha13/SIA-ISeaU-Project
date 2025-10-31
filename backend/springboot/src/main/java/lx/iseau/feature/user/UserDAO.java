package lx.iseau.feature.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface UserDAO {
    /**
     * 사용자 설정 업데이트
     * @param settingDTO
     * @return int (성공하면 1)
     */
    int settings(@Param("dto") SettingsDTO dto);

    RequestUserDTO findUserById(String id);
    RequestUserDTO findUserByNameAndPhone(@Param("name") String name, @Param("mobile") String mobile);

    // beach_number = #{beachNumber}
    int setActiveBeach(@Param("userNumber") int userNumber,
                       @Param("beachNumber") int beachNumber);

    // beach_number = NULL
    int clearActiveBeach(@Param("userNumber") int userNumber);

    // 현재 beach_number (null 가능)
    Integer findActiveBeach(@Param("userNumber") int userNumber);

    // (선택) tb_beach 존재 확인: 존재하면 1, 없으면 0
    int existsBeach(@Param("beachNumber") int beachNumber);

}