package lx.iseau.feature.forecast;

import org.apache.ibatis.annotations.Mapper;

import lx.iseau.feature.beach.ResponseBeachDTO;

import java.util.List; // java.util.List 임포트 필요

@Mapper
public interface ForecastDAO {

	List<RequestLocationDTO> selectBeachLocation();

	void upsertWeatherDB(List<ResponseWeatherDTO> rows);
	void upsertBeachWeatherDB(List<ResponseSeaWeatherDTO> rows);
	void upsertUVDB(List<ResponseUVDTO> rows);
}