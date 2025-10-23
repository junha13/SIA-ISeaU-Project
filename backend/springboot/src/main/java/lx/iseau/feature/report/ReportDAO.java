package lx.iseau.feature.report;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List; // java.util.List 임포트 필요
import java.util.Map;

@Mapper
public interface ReportDAO {
	//public ReportVO getFirstAid(String firstAidCase);
	
	public List<ReportDTO> getFirstAidByCaseNum(int firstAidCaseNum);

	public List<FirstAidCaseDTO> listFirstAidCases();
} 