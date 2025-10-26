package lx.iseau.feature.report;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
	
	@Autowired
	ReportDAO dao;
	
	/*
	 * ======== 하나의 응급 대처법 =========
	 */
	@Transactional(readOnly = true)
	public List<ReportDTO> getFirstAidByCaseNum(int firstAidCaseNum) {
		return dao.getFirstAidByCaseNum(firstAidCaseNum);
	}
	
	/*
	 * ======= 상황목록 조회 =======
	 */
	@Transactional(readOnly = true)
	public List<FirstAidCaseDTO> listFirstAidCases() {
		return dao.listFirstAidCases();
	}
	
}
