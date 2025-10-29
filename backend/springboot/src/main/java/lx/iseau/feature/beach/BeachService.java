
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
                vo.getCloseDate(),
                vo.getReviewCount()
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
	@Transactional(readOnly = true)
	public Map<String, Object> getBeachFavorites(int userNumber) {
	    Map<String, Object> map = new HashMap<>();

	    // DB에서 즐겨찾기 목록 가져오기
	    List<ResponseFavoritesDTO> favoritesList = dao.getBeachFavorites(userNumber);

	    List<Integer> beachNumbers = new ArrayList<>();
	    favoritesList.forEach(fav -> beachNumbers.add(fav.getBeachNumber()));

	    map.put("result", beachNumbers);
	    return map;
	}

	@Transactional // 데이터를 변경하므로 readOnly=false (기본값)
    public int insertFavorite(int userNumber, int beachNumber) {
        // DB에 전달할 VO 객체 생성
        BeachFavoritesVO beachFavorite = new BeachFavoritesVO();
        beachFavorite.setUserNumber(userNumber);
        beachFavorite.setBeachNumber(beachNumber);
        
        // DAO 호출하여 INSERT 실행
        // (주의: 이미 즐겨찾기된 경우 DB 제약조건 위반 예외 발생 가능)
        return dao.insertFavorite(beachFavorite);
    }

    
    @Transactional
    public int removeFavorite(int userNumber, int beachNumber) {
        // DB에 전달할 VO 객체 생성
        BeachFavoritesVO beachFavorite = new BeachFavoritesVO();
        beachFavorite.setUserNumber(userNumber);
        beachFavorite.setBeachNumber(beachNumber);
        
        // DAO 호출하여 DELETE 실행
        return  dao.removeFavorite(beachFavorite);
    }
    
    @Transactional(readOnly = true)
    public boolean checkFavoriteExists(int userNumber, int beachNumber) {
        BeachFavoritesVO beachFavorite = new BeachFavoritesVO();
        beachFavorite.setUserNumber(userNumber);
        beachFavorite.setBeachNumber(beachNumber);
        
        // DAO 호출하여 COUNT 쿼리 실행
        int count = dao.checkFavoriteExists(beachFavorite);
        
        // 결과가 0보다 크면 true 반환
        return count > 0;
    }
    
    /*
     * 해수욕장 방문자 리뷰
     */
    
    public List<ResponseBeachCommentDTO> listBeachComments(int beachNumber) {
        return dao.listBeachComments(beachNumber);
    }

    public int addBeachComment(ResponseBeachCommentDTO dto) {
        // 간단 검증
        if (dto.getRating() < 1 || dto.getRating() > 5) dto.setRating(5);
        return dao.insertBeachComment(dto);
    }
    
    public int editBeachComment(ResponseBeachCommentDTO dto) {
        if (dto.getRating() < 1 || dto.getRating() > 5) dto.setRating(5);
        return dao.updateBeachComment(dto);
    }

    public int removeBeachComment(int beachCommentNumber) {
        return dao.deleteBeachComment(beachCommentNumber);
    }
    
    
}