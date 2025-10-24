package lx.iseau.feature.user;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDAO {

    /**
     * 사용자 설정 업데이트
     * @param settingDTO
     * @return int (성공하면 1)
     */
    int settings(SettingsDTO dto);
}