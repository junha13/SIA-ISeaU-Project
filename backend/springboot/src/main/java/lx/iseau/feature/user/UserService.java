package lx.iseau.feature.user;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
	
	@Autowired
	UserDAO dao;
	
	/*
	 * 설명
	 */
	@Transactional
	public Map<String, Object> a(UserVO user) {
		int num = 3;
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("result", num);
		
		return map;
	}
	
}
