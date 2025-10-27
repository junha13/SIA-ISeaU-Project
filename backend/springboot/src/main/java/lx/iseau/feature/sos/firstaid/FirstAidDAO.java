package lx.iseau.feature.sos.firstaid;

import org.apache.ibatis.annotations.Mapper;

import java.util.List; // java.util.List 임포트 필요

@Mapper
public interface FirstAidDAO {
	//public ReportVO getFirstAid(String firstAidCase);
	
	public List<FirstAidDTO> getFirstAidByCaseNum(int firstAidCaseNum);

	public List<FirstAidCasesDTO> listFirstAidCases();
} 