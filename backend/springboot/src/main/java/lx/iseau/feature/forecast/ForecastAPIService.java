package lx.iseau.feature.forecast;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;


@Service
public class ForecastAPIService {

	@Autowired
	private WebClient.Builder webClientBuilder;
	
	@Autowired
	private ForecastDAO dao;
	
	@Transactional
	public List<Map<String, Object>> getForecastData() {
		WebClient webClient = webClientBuilder.build();
		
		// 해수욕장에서 위경도 뽑아와서 넣기
		List<RequestLocation> list = dao.selectBeachLocation();
		
		StringBuilder latsb = new StringBuilder();
		StringBuilder lonsb = new StringBuilder();
		
		for (int i =0; i < list.size(); i++) {
			RequestLocation loc = list.get(i);
			if (i > 0) { latsb.append(","); lonsb.append(","); }
			latsb.append(loc.getLat());
			lonsb.append(loc.getLon());
		}
		
		String lat = latsb.toString();
		String lon = lonsb.toString();
		
		System.out.println(lat);
		
		// 다중 좌표 테스트
		List<Map<String, Object>> result = webClient.get()
					.uri(uri -> uri
							.scheme("https")
							.host("api.open-meteo.com")
							.path("/v1/forecast")
							.queryParam("latitude", lat)
							.queryParam("longitude", lon)
							.queryParam("hourly", "temperature_2m,wind_speed_10m,wind_direction_10m,wind_gusts_10m,precipitation,rain")
							.queryParam("models", "kma_seamless")
							.queryParam("timezone", "Asia/Seoul")
							.queryParam("forecast_days", 3)
							.build()
							)
					.retrieve()
					.bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {}) // 4) 정상일 땐 JSON을 Map으로 파싱해서 Mono로 받음              
					.block(Duration.ofSeconds(10)); // 5) 리액티브 Mono를 10초 안에 동기적으로 결과(Map)로 뽑아냄
		
		System.out.println(result);
			return result;
	}
	
	@Transactional
	public void insertDB(List<Map<String, Object>> result) {
		List<ResponseWeatherDTO> rows = new ArrayList<ResponseWeatherDTO>();
		
		// 각 장소별 기상정보 뽑기
		for(Map<String, Object> data : result) {
			double lat = (Double) data.get("latitude");
			double lon = (Double) data.get("longitude");
			
			Map<String, Object> hourly = (Map<String, Object>) data.get("hourly");
			List<String> forecastTime = (List<String>) hourly.get("time");
			List<Double> temperature = (List<Double>) hourly.get("temperature_2m");
			List<Double> windSpeed = (List<Double>) hourly.get("wind_speed_10m");
			List<Integer> windDirection = (List<Integer>) hourly.get("wind_direction_10m");
			List<Double> rainfall = (List<Double>) hourly.get("rain");
			
			for (int i=0; i<forecastTime.size(); i++) {
				ResponseWeatherDTO dto = new ResponseWeatherDTO();
				dto.setLat(lat);
				dto.setLon(lon);
				dto.setForecastTime( LocalDateTime.parse(forecastTime.get(i)));
				dto.setTemperature(temperature.get(i));
				dto.setWindSpeed(windSpeed.get(i));
				dto.setWindDirection(windDirection.get(i));
				dto.setRainfall(rainfall.get(i));
				
				rows.add(dto);
				
			}
			
			if (!rows.isEmpty()) dao.upsertForecastDB(rows);
		}
		
	}
}
