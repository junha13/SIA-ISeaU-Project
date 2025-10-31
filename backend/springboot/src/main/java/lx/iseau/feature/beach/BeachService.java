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
                vo.getReviewCount()
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
    // 방문자 리뷰 목록
    public List<ResponseBeachCommentDTO> getListBeachComments(int beachNumber) {
        return dao.listBeachComments(beachNumber);
    }
	// 방문자 리뷰 등록
    public int addBeachComment(ResponseBeachCommentDTO dto) {
        // 간단 검증
        if (dto.getRating() < 1 || dto.getRating() > 5) dto.setRating(5);
        return dao.insertBeachComment(dto);
    }
    // 방문자 리뷰 수정
    public int editBeachComment(ResponseBeachCommentDTO dto) {
        if (dto.getRating() < 1 || dto.getRating() > 5) dto.setRating(5);
        return dao.updateBeachComment(dto);
    }
    // 방문자 리뷰 삭제
    public int removeBeachComment(int beachCommentNumber) {
        return dao.deleteBeachComment(beachCommentNumber);
    }


}