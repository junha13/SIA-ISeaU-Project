package lx.iseau.feature.forecast;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseWeatherDTO {

	private double temperature;
	private Integer humidity;
	private Integer rainProbability;
	private double rain;
	private double windGusts;
	private double windSpeed;
	private Integer windDirection;
	private double uvIndex;
	private double uvIndexClearSky;

	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm") // 응답을 분까지만 보이게
	private LocalDateTime forecastTime;
	
	private double lat;
	private double lon;
}

