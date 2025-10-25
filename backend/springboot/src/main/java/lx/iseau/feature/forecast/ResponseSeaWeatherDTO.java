package lx.iseau.feature.forecast;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSeaWeatherDTO {

	private double waveHeight;
	private double seaSurfaceTemperature;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm") // 응답을 분까지만 보이게
	private LocalDateTime forecastTime;
	
	private double lat;
	private double lon;
}

