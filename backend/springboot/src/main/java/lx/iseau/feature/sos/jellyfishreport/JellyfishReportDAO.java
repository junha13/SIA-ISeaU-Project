package lx.iseau.feature.sos.jellyfishreport;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JellyfishReportDAO {
	public int insertReport(JellyfishReportDTO dto);        // 저장 (reportNumber 세팅)
}