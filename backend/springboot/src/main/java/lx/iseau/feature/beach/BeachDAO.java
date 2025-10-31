package lx.iseau.feature.beach;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BeachDAO {
	List<BeachVO> findBeacheList(BeachListRequest request);

	public ResponseBeachDTO getBeachDetailInfo(int beachNumber);

	
	public List<ResponseBeachDangerDTO> getBeachDetailDanger(int beachNumber);
	
	
	public List<ResponseBeachWeatherDTO> getBeachDetailWeather(int beachNumber);
	// 즐겨찾기 리스트 조회
	public List<ResponseFavoritesDTO> getBeachFavorites(int userNumber);
	// 즐겨찾기 추가
	int insertFavorite(BeachFavoritesVO beachFavorite);
    //삭제
    int removeFavorite(BeachFavoritesVO beachFavorites);
    // 특정 사용자가 특정 해변을 즐겨찾기 했는지 확인 (중복 확인)
    int checkFavoriteExists(BeachFavoritesVO beachFavorites);
	// 위경도 위치 보내주는걸로
    // 위험 조회
	public ResponseBeachDeptDTO getBeachDetailDept(int beachNumber);
	/*
	 * 방문자 리뷰
	 */
	// 방문자 리뷰 목록 조회
    List<ResponseBeachCommentDTO> listBeachComments(@Param("beachNumber") int beachNumber);
    // 방문자 리뷰 등록
    int insertBeachComment(ResponseBeachCommentDTO dto);
    // 방문자 리뷰 수정
    int updateBeachComment(ResponseBeachCommentDTO dto);
    // 방문자 리뷰 삭제
    int deleteBeachComment(@Param("beachCommentNumber") int beachCommentNumber);

	
	//public ResponseBeachPestDTO getBeachDetailPest(int beachNumber);
}