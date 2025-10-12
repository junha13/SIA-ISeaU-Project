package lx.iseau.feature.map;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MapService {
	
	@Autowired
	MapDAO dao;
	
	/*
	 * 설명
	 */
	@Transactional
	public Map<String, Object> a(int num) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("result", num);
		
		return map;
	}
	
}
