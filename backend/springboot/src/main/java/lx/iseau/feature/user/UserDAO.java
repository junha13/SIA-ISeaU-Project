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
}