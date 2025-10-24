
package lx.iseau.feature.beach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BeachService  { 

    @Autowired
    BeachDAO dao;
    
    @Transactional(readOnly = true)
    public List<ResponseBeachDTO> getBeachList(BeachListRequest request) {
        List<BeachVO> voList = dao.findBeacheList(request);
        List<ResponseBeachDTO> dtoList = new ArrayList<>();      
        for (BeachVO vo : voList) {
        	ResponseBeachDTO dto = new ResponseBeachDTO(
                vo.getBeachNumber(),
                vo.getBeachName(),
                vo.getBeachImage(),
                vo.getBeachInformation(),
                vo.getRating(),
                vo.isApprovedByMinistry(),
                vo.getAddress(),
                vo.getLatitude(),
                vo.getLongitude(), 
                vo.getMobile(),
                vo.getOpenDate(),
                vo.getCloseDate()
            );
            dtoList.add(dto);
        }
       
        Map<String, Object> map = new HashMap<String, Object>();
        
        
        map.put("result", dtoList);
        
      
        return dtoList;
    }

	/*
	 * ========= 하나의 해수욕장의 값을 보내주면 딤 =========
	 */
	@Transactional	public Map<String, Object> getBeachDetailInfo(int beachNumber) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		ResponseBeachDTO beach = dao.getBeachDetailInfo(beachNumber);
		
		map.put("result", beach);
		return map;
	}

	
	/*
	 * ========= 하나의 해수욕장에 대한 여러 시간대 danget 상황 보내주기 (이안류, 풍속, 파고) =========
	 */
	@Transactional
	public Map<String, Object> getBeachDetailDanger(int beachNumber) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		List<ResponseBeachDangerDTO> Danger = dao.getBeachDetailDanger(beachNumber);
		
		map.put("result", Danger);
		return map;
	}
	/*
	 * ========= 하나의 해수욕장에 대한 여러 시간대 Weather 상황 보내주기 (기온, 자외선, 강수량) =========
	 */
	@Transactional
	public Map<String, Object> getBeachDetailWeather(int beachNumber) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		List<ResponseBeachWeatherDTO> Weather = dao.getBeachDetailWeather(beachNumber);
		
		map.put("result", Weather);
		return map;
	}
    
}