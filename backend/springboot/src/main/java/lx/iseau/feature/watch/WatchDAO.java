package lx.iseau.feature.watch;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import lx.iseau.feature.watch.dto.WatchEventDTO;

@Mapper
public interface WatchDAO {
    // 워치 이벤트 저장
    int insertWatchEvent(WatchEventDTO dto);

    // user_number -> beach_number -> control_tower_number 조회
    Integer findControlTowerNumberByUser(@Param("userNumber") Integer userNumber);
}
