package lx.iseau.feature.cctv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lx.iseau.feature.controltower.ManagerInfoDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CctvService {

    private final CctvDAO dao;
    
    // ============ 로그추가 ============
    public Map<String, Object> insertDangerLog(CctvDangerLogDto logDto) {
    	Map<String, Object> map = new HashMap<String, Object>();
    	
    	int result = dao.insertDangerLog(logDto);
    	
    	map.put("result", result);
    	return map;
    }
    
    // ============ 일일로그보기 ============
    public Map<String, Object> selectDangerLogList(CctvDangerLogDto logDto) {
    	Map<String, Object> map = new HashMap<String, Object>();
    	List<CctvDangerLogDto> dto = null;
    	if (logDto.getBeachNumber() == 0 ) { // 전체인 경우
    		dto = dao.selectDangerLogList(logDto);
    	} 
    	if (logDto.getBeachNumber() >=1 ) {
    		dto = dao.selectDangerLogListByBeachNumber(logDto);
    	}
    	map.put("result", dto);
        return map;
    }
}