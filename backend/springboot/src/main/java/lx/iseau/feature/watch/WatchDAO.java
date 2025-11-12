package lx.iseau.feature.watch;

import org.apache.ibatis.annotations.Mapper;
import java.util.Map;

@Mapper
public interface WatchDAO {
    // 1. tb_watch에 심박수 데이터를 삽입 (mapper-watch.xml의 insertWatchEvent)
    void insertWatchEvent(WatchDataDTO watchData);

    // 2. userNumber를 통해 담당 매니저 번호를 조회 (mapper-watch.xml의 findManagerByUser)
    Integer findManagerByUser(int userNumber);

    // 3. tb_task에 작업 추가 (이미 watch_number가 존재하면 삽입 안 함) (mapper-watch.xml의 insertTaskIfAbsent)
    void insertTaskIfAbsent(Map<String, Integer> params);

    // 💡 [추가된 코드]: watch_number로 상세 데이터 조회 (mapper-watch.xml의 selectWatchByNumber)
    Map<String, Object> selectWatchByNumber(int watchNumber);
}