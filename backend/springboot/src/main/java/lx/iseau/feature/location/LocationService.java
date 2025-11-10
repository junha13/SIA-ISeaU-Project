package lx.iseau.feature.location;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lx.iseau.feature.group.GroupsDAO;

@Service
@RequiredArgsConstructor
public class LocationService {
	
    private final LocationDAO dao;
    
    private final HttpSession session;

	public Map<String, Object> boundaryCheck(RequestUserLocationDTO dto) {
    	Map<String, Object> map = new HashMap<>();
    	
    	ResponseUserLocationDTO data = dao.selectBoundaryCheck(dto);
    	
    	dto.setUserNumber((Integer) session.getAttribute("userNumber"));
    	dao.updateUserLocation(dto);
    	
    	Double distance = data.getDistance();
    	data.setInterval(5000);  // 30초
    	if(distance < 30) data.setInterval(5000); // 3초
    	
    	data.setGroupNumber(dto.getGroupNumber());
    	
		map.put("result", data);
		
		return map;
	}
	public Map<String, Object> testBoundaryCheck(RequestUserLocationDTO dto) {
		Map<String, Object> map = new HashMap<>();
		
		ResponseUserLocationDTO data = dao.selectTestCheck(dto);
    	
    	dto.setUserNumber((Integer) session.getAttribute("userNumber"));
    	dao.updateUserLocation(dto);
		
		Double distance = data.getDistance();
		data.setInterval(5000);  // 30초
		if(distance < 30) data.setInterval(5000); // 3초
		
		data.setGroupNumber(dto.getGroupNumber());
		
		map.put("result", data);
		
		return map;
	}
	public Map<String, Object> updateUserLocation(RequestUserLocationDTO dto) {
		Map<String, Object> map = new HashMap<>();
        
        // 1. 세션에서 현재 로그인된 사용자 번호(userNumber)를 가져와 DTO에 설정합니다.
        Integer userNumber = (Integer) session.getAttribute("userNumber");

        if (userNumber == null) {
            map.put("success", false);
            map.put("message", "로그인 세션이 만료되었습니다.");
            return map;
        }

        dto.setUserNumber(userNumber);

        // 2. LocationDAO를 호출하여 DB의 tb_user 위치 정보를 갱신합니다.
        int updatedRows = dao.updateUserLocation(dto);

        // 3. 결과 처리
        if (updatedRows > 0) {
            map.put("success", true);
            map.put("message", "위치 정보가 성공적으로 갱신되었습니다.");
        } else {
            map.put("success", false);
            map.put("message", "위치 정보 갱신에 실패했습니다. (사용자를 찾을 수 없음)");
        }
        
        return map;
	}
}
