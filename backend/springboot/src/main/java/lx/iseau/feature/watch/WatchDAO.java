package lx.iseau.feature.watch;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WatchDAO {

    // 워치 단건 조회(발생시각, HR, Spo02 보여주기)
    Map<String, Object> selectWatchByNumber(@Param("watchNumber") Integer watchNumber);

    // 유저→비치→관제→매니저(1:1) 찾기
    Integer findManagerByUser(@Param("userNumber") Integer userNumber);

    // tb_task 생성 (동일 watch_number 재생성 방지 처리 포함)
    int insertTaskIfAbsent(@Param("managerNumber") Integer managerNumber,
                           @Param("watchNumber") Integer watchNumber);
}
