package lx.iseau.feature.forecast;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List; // java.util.List 임포트 필요

@Mapper
public interface ForecastDAO {

	List<RequestLocation> selectBeachLocation();

	void upsertForecastDB(List<ResponseWeatherDTO> rows);
}