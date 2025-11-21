package lx.iseau.feature.cctv;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CctvDAO {
	
	// 오늘 날짜 기준 CCTV 위험 로그들 조회
	int insertDangerLog(CctvDangerLogDto logDto);
	
	// 오늘 날짜 기준 CCTV 위험 로그들 조회
    List<CctvDangerLogDto> selectDangerLogList(CctvDangerLogDto logDto);
    // 오늘 날짜 기준 CCTV 위험 로그들 조회
    List<CctvDangerLogDto> selectDangerLogListByBeachNumber(CctvDangerLogDto logDto);

	
}
