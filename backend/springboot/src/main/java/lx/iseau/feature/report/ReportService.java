package lx.iseau.feature.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	@Transactional
	public Map<String, Object> getFirstAidByCase(String firstAidCase) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		ReportDTO firstAid = dao.getFirstAidByCase(firstAidCase);
		
		map.put("result", firstAid);
		
		return map;
	}
	
	/*
	 * ======= 상황목록 조회 =======
	 */
	@Transactional(readOnly = true)
	public Map<String, Object> listFirstAidCases() {
		Map<String, Object> map = new HashMap<>();
		
		// DAO에서 상황 목록 리스트 형태로 받아오기
		List<Map<String, Object>> cases = dao.listFirstAidCases();
		
		map.put("result", cases);
		return map;
	}
	
}
