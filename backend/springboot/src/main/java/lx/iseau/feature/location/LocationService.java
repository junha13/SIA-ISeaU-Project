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

	public Map<String, Object> boundaryCheck(RequestUserLocationDTO dto) {
    	Map<String, Object> map = new HashMap<>();
    	
    	ResponseUserLocationDTO data = dao.selectBoundaryCheck(dto);
    	
    	Double distance = data.getDistance();
    	data.setInterval(30000);  // 30초
    	if(distance < 30) data.setInterval(3000); // 3초
    	
		map.put("result", data);
		
		return map;
	}
	public Map<String, Object> testBoundaryCheck(RequestUserLocationDTO dto) {
		Map<String, Object> map = new HashMap<>();
		
		ResponseUserLocationDTO data = dao.selectTestCheck(dto);
		
		Double distance = data.getDistance();
		data.setInterval(30000);  // 30초
		if(distance < 30) data.setInterval(3000); // 3초
		
		map.put("result", data);
		
		return map;
	}

}
