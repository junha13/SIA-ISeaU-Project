package lx.iseau.feature.beach;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BeachDAO {
	List<BeachVO> findBeacheList(BeachListRequest request);

	public ResponseBeachDTO getBeachDetailInfo(int beachNumber);

	
	public List<ResponseBeachDangerDTO> getBeachDetailDanger(int beachNumber);
	
	
	//public ResponseBeachForecastDTO getBeachDetailForecast(int beachNumber);

	// 위경도 위치 보내주는걸로
	//public ResponseBeachDeptDTO getBeachDetailDept(int beachNumber);
	
	
	//public ResponseBeachPestDTO getBeachDetailPest(int beachNumber);
}