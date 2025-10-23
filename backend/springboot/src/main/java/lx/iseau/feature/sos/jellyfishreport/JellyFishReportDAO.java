package lx.iseau.feature.sos.jellyfishreport;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JellyFishReportDAO {
	int insertReport(JellyFishReportDTO dto);        // 저장 (reportNumber 세팅)
  
}