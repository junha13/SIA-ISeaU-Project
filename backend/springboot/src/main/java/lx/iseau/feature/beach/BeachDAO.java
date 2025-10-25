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
	//public ResponseBeachDeptDTO getBeachDetailDept(int beachNumber);

	
	
	//public ResponseBeachPestDTO getBeachDetailPest(int beachNumber);
}