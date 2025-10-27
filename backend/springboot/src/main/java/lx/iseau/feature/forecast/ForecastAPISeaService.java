package lx.iseau.feature.forecast;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ForecastAPISeaService {

	private final WebClient.Builder webClientBuilder;
	
	private final ForecastDAO dao;
	
	public List<Map<String, Object>> getSecondForecastData() {
		WebClient webClient = webClientBuilder.build();
		
		// 해수욕장에서 위경도 뽑아와서 넣기
		List<RequestLocationDTO> list = dao.selectBeachLocation();
		
		StringBuilder latsb = new StringBuilder();
		StringBuilder lonsb = new StringBuilder();
		
		for (int i =0; i < list.size(); i++) {
			RequestLocationDTO loc = list.get(i);
			if (i > 0) { latsb.append(","); lonsb.append(","); }
			latsb.append(loc.getLat());
			lonsb.append(loc.getLon());
		}
		
		String lat = latsb.toString();
		String lon = lonsb.toString();
		
		System.out.println("Requesting latitudes: " + lat);
		
		// 다중 좌표 테스트
		List<Map<String, Object>> result = webClient.get()
				.uri(uri -> uri
						.scheme("https")
						.host("marine-api.open-meteo.com")
						.path("/v1/marine")
						.queryParam("latitude", lat)
						.queryParam("longitude", lon)
						.queryParam("hourly", "wave_height,sea_surface_temperature")
						//.queryParam("models", "kma_seamless")
						.queryParam("timezone", "Asia/Seoul")
						.queryParam("forecast_days", 3)
						//.queryParam("windspeed_unit", "ms")
						.build()
						)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {}) 
				.block(Duration.ofSeconds(10));
		
		System.out.println("API Response received.");
		return result;
	}
	
	@Transactional
	public void insertSecondDB(List<Map<String, Object>> result) {
		List<ResponseSeaWeatherDTO> allRows = new ArrayList<>();
		
		// 1. 모든 API 응답을 파싱하여 하나의 리스트로 통합
		for(Map<String, Object> data : result) {
			if (data == null || !data.containsKey("latitude") || !data.containsKey("longitude") || !data.containsKey("hourly")) {
				System.err.println("Warning: Skipping invalid data entry from API response.");
				continue;
			}
			
			double lat = (Double) data.get("latitude");
			double lon = (Double) data.get("longitude");
			
			Map<String, Object> hourly = (Map<String, Object>) data.get("hourly");
			
			if (hourly == null || !hourly.containsKey("time")) {
				System.err.println("Warning: Skipping location due to missing hourly data.");
				continue;
			}
			
			List<String> forecastTime = (List<String>) hourly.get("time");
			List<Double> waveHeight = (List<Double>) hourly.get("wave_height"); // 강수 확률
			List<Double> seaSurfaceTemperature = (List<Double>) hourly.get("sea_surface_temperature");
			
			// 데이터 배열들의 크기가 일치하는지 확인하여 IndexOutOfBoundsException 방지
			if (forecastTime == null || waveHeight == null || seaSurfaceTemperature == null  ||
					!(forecastTime.size() == waveHeight.size() && forecastTime.size() == seaSurfaceTemperature.size())) {
				System.err.println("Warning: Mismatched hourly data arrays for location " + lat + "," + lon);
				continue;
			}
			
			for (int i=0; i<forecastTime.size(); i++) {
				ResponseSeaWeatherDTO dto = new ResponseSeaWeatherDTO();
				dto.setLat(lat);
				dto.setLon(lon);
				dto.setForecastTime(LocalDateTime.parse(forecastTime.get(i)));
				dto.setWaveHeight(waveHeight.get(i));
				dto.setSeaSurfaceTemperature(seaSurfaceTemperature.get(i));
				
				allRows.add(dto);
			}
		}
		
        // 2. (위도 + 경도 + 예보 시간)을 키로 사용하여 리스트 전체의 중복을 제거
        List<ResponseSeaWeatherDTO> distinctRows = allRows.stream()
            .collect(Collectors.collectingAndThen(
                Collectors.toMap(
                    // 유니크 키 생성
                    dto -> dto.getLat() + ":" + dto.getLon() + ":" + dto.getForecastTime(),
                   Function.identity(),
                    // 키가 중복될 경우, 기존 값을 새 값으로 덮어씀
                    (existing, replacement) -> replacement
               ),
               map -> new ArrayList<>(map.values())
            ));
		
		// 3. 반복문이 끝난 후, 중복이 제거된 최종 리스트를 DB에 한 번만 저장
		if (!distinctRows.isEmpty()) {
            System.out.println("Upserting " + distinctRows.size() + " unique forecast rows to the database.");
			dao.upsertBeachWeatherDB(distinctRows);
		} else {
            System.out.println("No new forecast data to upsert.");
        }
	}
}
