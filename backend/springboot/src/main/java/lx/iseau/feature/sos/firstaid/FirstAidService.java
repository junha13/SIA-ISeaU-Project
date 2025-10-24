package lx.iseau.feature.sos.firstaid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FirstAidService {
	
	@Autowired
	FirstAidDAO dao;
	
	/*
	 * ======== 하나의 응급 대처법 =========
	 */
	@Transactional(readOnly = true)
	public List<FirstAidDTO> getFirstAidByCaseNum(int firstAidCaseNum) {
		return dao.getFirstAidByCaseNum(firstAidCaseNum);
	}
	
	/*
	 * ======= 상황목록 조회 =======
	 */
	@Transactional(readOnly = true)
	public List<FirstAidCasesDTO> listFirstAidCases() {
		return dao.listFirstAidCases();
	}
	
}
