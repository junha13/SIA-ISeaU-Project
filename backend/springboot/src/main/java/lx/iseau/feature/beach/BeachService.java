
package lx.iseau.feature.beach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lx.iseau.feature.location.LocationDAO;

@Service
@RequiredArgsConstructor
public class BeachService  { 

    private final BeachDAO dao;
    
    private final HttpSession session;
    
    /* 공통: 로그인 사용자 번호 가져오기 (없으면 0 던짐) */
    private int requireLoginUserNumber() {
        Integer userNumber = (Integer) session.getAttribute("userNumber");
        return userNumber == null ? 0 : userNumber;
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getBeachList(BeachListRequest request) {
        // 1) 기본값 보정
        int page = (request.getPage() == null || request.getPage() < 1) ? 1 : request.getPage();
        int size = (request.getSize() == null || request.getSize() < 1) ? 10 : request.getSize();
        size = Math.min(size, 50); // 안전 상한

        // 2) 정렬 기본값
        if (request.getSort() == null || request.getSort().isEmpty()) {
            request.setSort("name_asc");
        }
        // (region/category는 null/"" 허용)

        // 3) 조회
        List<BeachVO> voList = dao.findBeacheList(request);

        // 4) 변환
        List<ResponseBeachDTO> dtoList = new ArrayList<>();
        for (BeachVO vo : voList) {
            dtoList.add(new ResponseBeachDTO(
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
                vo.getReviewCount(),
                vo.getTagsString()
            ));
        }

        // 5) hasMore 계산: 이번 페이지가 가득 찼으면 다음 페이지 있다고 가정
        boolean hasMore = dtoList.size() == size;

        // (옵션) 정확한 hasMore 원하면 count 사용
        // int total = dao.countBeaches(request);
        // boolean hasMore = page * size < total;

        // 6) 응답 패키징
        Map<String, Object> res = new HashMap<>();
        res.put("result", dtoList);
        res.put("page", page);
        res.put("size", size);
        res.put("hasMore", hasMore);
        res.put("nextPage", hasMore ? page + 1 : null);
        return res;
    }

	/*
	 * ========= 하나의 해수욕장의 값을 보내주면 딤 =========
	 */
	@Transactional	
	public Map<String, Object> getBeachDetailInfo(int beachNumber) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		ResponseBeachDTO beach = dao.getBeachDetailInfo(beachNumber);
		
		map.put("result", beach);
		return map;
	}

	
	/*
	 * ========= 하나의 해수욕장에 대한 여러 시간대 danger 상황 보내주기 (이안류, 풍속, 파고) =========
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
	public Map<String, Object> getBeachFavorites() {
		
	    Map<String, Object> map = new HashMap<>();

	    // DB에서 즐겨찾기 목록 가져오기
	    List<ResponseFavoritesDTO> favoritesList = 
	    		dao.getBeachFavorites((Integer) session.getAttribute("userNumber"));

	    List<Integer> beachNumbers = new ArrayList<>();
	    favoritesList.forEach(fav -> beachNumbers.add(fav.getBeachNumber()));

	    map.put("result", beachNumbers);
	    return map;
	}

	

	@Transactional
	public int insertFavorite(int beachNumber) {
	    
	    // 1. 세션에서 userNumber 획득 (인증 체크)
	    int userNumber = requireLoginUserNumber();
	    // 💡 인증 실패 처리: userNumber가 0이면, 등록 불가 (Controller에서 401 처리)
	    if (userNumber == 0) return 0; 	    
	    // 2. DB에 전달할 VO 객체 생성
	    BeachFavoritesVO beachFavorite = new BeachFavoritesVO();
	    beachFavorite.setUserNumber(userNumber); 
	    beachFavorite.setBeachNumber(beachNumber);	    
	    try {
	        // 3. DAO 호출하여 INSERT 실행
	        // (DB 제약 조건 위반 예외가 발생하면 Controller로 전달됨)
	        return dao.insertFavorite(beachFavorite);
	        
	    } catch (Exception e) {
	        
	        if (e.getMessage() != null && (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("ConstraintViolation"))) {
	       
	             return -1;
	        }
	        
	        // 그 외 예상치 못한 DB 오류는 다시 던지거나 0을 반환합니다.
	        throw new RuntimeException("즐겨찾기 저장 중 예상치 못한 오류 발생", e);
	    }
	}
	// BeachService.java 내 removeFavorite 메소드

	@Transactional
	public int removeFavorite(int beachNumber) {	    
	    // 1. 세션에서 userNumber 획득 (인증 체크)
	    int userNumber = requireLoginUserNumber();	    
	    // 2. 인증 실패 처리: userNumber가 0이면, 제거 불가
	    if (userNumber == 0) return 0; 	    
	    BeachFavoritesVO beachFavorite = new BeachFavoritesVO();
	    beachFavorite.setUserNumber(userNumber);
	    beachFavorite.setBeachNumber(beachNumber);	    	   
	    return dao.removeFavorite(beachFavorite);
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
    // 방문자 리뷰 목록
    public Map<String, Object> getBeachComments(int beachNumber) {
    	Map<String, Object> map = new HashMap<>();
    	map.put("result", dao.getBeachComments(beachNumber));
        return map;
    }
    
    
    
    
	// 방문자 리뷰 등록
    public Map<String, Object> insertBeachComment(ResponseBeachCommentDTO dto) {
    	Map<String, Object> map = new HashMap<>();
    	
    	if (requireLoginUserNumber() == 0) return Map.of("result", "login");
    	
    	dto.setUserNumber(requireLoginUserNumber());
    	
        map.put("result", dao.insertBeachComment(dto));
        return map;
    }
    
    
    
    
    
    // 방문자 리뷰 수정    
    public Map<String, Object> updateBeachComment(ResponseBeachCommentDTO dto) {	
    	Map<String, Object> map = new HashMap<>();
    	
    	dto.setUserNumber(requireLoginUserNumber());
    	
        map.put("result", dao.updateBeachComment(dto));
        return map;
    }
    
    
    
    // 방문자 리뷰 삭제
    public Map<String, Object> deleteBeachComment(ResponseBeachCommentDTO dto) {
    	Map<String, Object> map = new HashMap<>();
    	
    	dto.setUserNumber(requireLoginUserNumber());
    	
        map.put("result", dao.deleteBeachComment(dto));
        return map;
    }
    
    
    
}