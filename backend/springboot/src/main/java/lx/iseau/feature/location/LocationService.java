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

}
